package ufrn.dimap.lets.callgraphvisitor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import ufrn.dimap.lets.metric.handlers.HandlerUtil;
import ufrn.dimap.lets.metric.model.exceptionalinterface.Method;
import ufrn.dimap.lets.metric.visitor.exceptionalinterface.MethodFinder;
import ufrn.dimap.lets.metric.visitor.exceptionalinterface.MethodVisitor;
import ufrn.dimap.lets.metric.visitor.exceptionalinterface.MethodVisitor2;

public class MethodNode
{
	private IMethod iMethod;
	private MethodNode parent;
	private List<MethodNode> children;
	private boolean recursive;
	
	private Set<String> thrownTypes;
	private Set<String> rethrownTypes;
	
	public MethodNode(IMethod iMethod, MethodNode parent)
	{
		this.iMethod = iMethod;
		this.parent = parent;
		this.children = new ArrayList<>();
		recursive = false;
		
		this.thrownTypes = new HashSet<>();
		this.rethrownTypes = new HashSet<>();
	}

	public void computeExceptionalInterface() throws JavaModelException
	{
		for ( MethodNode child : this.getChildren() )
		{
			child.computeExceptionalInterface();
		}
		
		if ( isParseable (this.iMethod) )
		{
			CompilationUnit compilationUnit = HandlerUtil.parse(this.iMethod);
			MethodDeclaration methodDeclaration = MethodFinder.find ( this.iMethod, compilationUnit );
			
			MethodVisitor2 methodVisitor = new MethodVisitor2(this);
			methodDeclaration.accept(methodVisitor);

			this.thrownTypes.addAll(methodVisitor.thrownTypes);

			// Adicionar a interface excepcional dos métodos chamados no método atual
			for (MethodNode callee : this.children)
			{
				this.rethrownTypes.addAll(callee.getThrownTypes());
				this.rethrownTypes.addAll(callee.getRethrownTypes());
			}
		}
		else
		{
			for ( String exception : getDeclaredException(this.iMethod) )
			{
				this.rethrownTypes.add( exception );
			}
		}
	}
	
	/**
	 * O IMethod é parseable se não é nativo e possui código-fonte (é um CompilationUnit ou um ClassFile com código-fonte linkado).
	 * @param	method	O método a ser testado.
	 * */
	private boolean isParseable(IMethod method) throws JavaModelException
	{		
		if ( method.getCompilationUnit() != null ||
			(method.getClassFile() != null && method.getClassFile().getSource() != null))
		{
			if ( !isAbstract (method.getSource()) )
			{
				return true;
			}
			else
			{
				return false;
			}
		}
		else
		{
			return false;
		}
	}
	
	/**
	 * Dado uma string que representa o código-fonte de um método (podendo incluir Javadoc),
	 * indica se este método é abstrato (definido em uma classe abstrata ou em uma interface).
	 * @param	source	código-fonte do método 
	 * */
	private boolean isAbstract(String source)
	{
		Pattern pattern = Pattern.compile(".*\\{.*\\}$", Pattern.DOTALL);
		Matcher matcher = pattern.matcher(source);
		boolean isAbstract = !matcher.matches();
		
		return isAbstract;
	}
	
	// O IMethod possui um array de strings que representam as exceções da interface excepcional declarada. As strings estão num formato diferente. Este método convete o array com nomes estranhos em uma lista de tamanhos normais.
	private List<String> getDeclaredException(IMethod method) throws JavaModelException
	{
		List<String> exceptions = new ArrayList<>();

		for ( String exception : method.getExceptionTypes() )
		{
			exceptions.add(exception.substring(1, exception.length() - 1));
		}

		return exceptions;
	}
	
	public boolean isRecursive ()
	{
		return this.recursive;
	}
	
	public void setRecursive (boolean recursive)
	{
		this.recursive = recursive;
	}
	
	public Set<String> getThrownTypes()
	{
		return this.thrownTypes;
	}
	
	public Set<String> getRethrownTypes()
	{
		return this.rethrownTypes;
	}
	
	public IMethod getIMethod() {
		return iMethod;
	}

	public void setIMethod(IMethod iMethod) {
		this.iMethod = iMethod;
	}

	public MethodNode getParent() {
		return parent;
	}

	public void setParent(MethodNode parent) {
		this.parent = parent;
	}

	public List<MethodNode> getChildren() {
		return children;
	}

	public void setChildren(List<MethodNode> children) {
		this.children = children;
	}

	public String toString ()
	{
		return this.iMethod.getHandleIdentifier();
	}
	
	public String printGraph ()
	{
		return this.printGraphR(0);
	}
	
	private String printGraphR (int tabs)
	{
		StringBuilder result = new StringBuilder();
		
		for ( int i = 0 ; i < tabs ; i++ )
			result.append("\t"); 
		
		result.append (this.iMethod.getHandleIdentifier() + "\n");
		
		for ( MethodNode n : this.children )
		{
			result.append (n.printGraphR(tabs+1));
		}
		
		return result.toString();
	}

	public Set<String> getExternalExceptions()
	{
		Set<String> exceptions = new HashSet<>();
		
		exceptions.addAll(this.thrownTypes);
		exceptions.addAll(this.rethrownTypes);
		
		return exceptions;
	}



	
}
