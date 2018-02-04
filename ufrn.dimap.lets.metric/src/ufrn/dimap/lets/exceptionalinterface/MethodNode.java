package ufrn.dimap.lets.exceptionalinterface;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import ufrn.dimap.lets.metric.handlers.HandlerUtil;

public class MethodNode
{
	private IMethod iMethod;
	private MethodNode parent;
	private List<MethodNode> children;
	private boolean recursive;
	
	private Set<ITypeBinding> propagated;
	private Map<ITypeBinding, Set<ITypeBinding>> caught;
	private Set<ITypeBinding> thrown;
	private Set<ITypeBinding> rethrown;
	private Map<ITypeBinding, Set<ITypeBinding>> wrapped;
	
	public MethodNode(IMethod iMethod, MethodNode parent)
	{
		this.iMethod = iMethod;
		this.parent = parent;
		this.children = new ArrayList<>();
		this.recursive = false;
		
		this.propagated = new HashSet<>();
		this.caught = new HashMap<>();
		this.thrown = new HashSet<>();
		this.rethrown = new HashSet<>();
		this.wrapped = new HashMap<>();
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
			
			MethodVisitor methodVisitor = new MethodVisitor(this);
			methodDeclaration.accept(methodVisitor);

//			this.thrown.addAll(methodVisitor.thrownTypes);
//
//			// Adicionar a interface excepcional dos métodos chamados no método atual
//			for (MethodNode callee : this.children)
//			{
//				this.rethrown.addAll(callee.getThrown());
//				this.rethrown.addAll(callee.getRethrown());
//			}
		}
		else
		{
			for ( String exception : getDeclaredException(this.iMethod) )
			{
				//this.rethrown.add( exception );
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
	
	public String getIdentifier()
	{
		if ( this.iMethod != null )
		{
			return this.iMethod.getHandleIdentifier();
		}
		else
		{
			return "Fake MethodNode";
		}
	}
	
	public boolean isRecursive ()
	{
		return this.recursive;
	}
	
	public void setRecursive (boolean recursive)
	{
		this.recursive = recursive;
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
	
	public Set<ITypeBinding> getPropagated() {
		return propagated;
	}

	public Map<ITypeBinding, Set<ITypeBinding>> getCaught() {
		return caught;
	}

	public Set<ITypeBinding> getThrown() {
		return thrown;
	}

	public Set<ITypeBinding> getRethrown() {
		return rethrown;
	}

	public Map<ITypeBinding, Set<ITypeBinding>> getWrapped() {
		return wrapped;
	}

	public String toString ()
	{
		return this.getIdentifier();
	}
	
	public String printGraph ()
	{
		return this.printGraphR(0);
	}
	
	private String printGraphR (int tabs)
	{
		if ( this.iMethod != null )
		{
			StringBuilder result = new StringBuilder();
			
			for ( int i = 0 ; i < tabs ; i++ )
				result.append("  "); 
			
			result.append (this.iMethod.getDeclaringType().getTypeQualifiedName());
			result.append(":");
			result.append(this.iMethod.getElementName());
			result.append("(");
			
			String delimiter = "";
			for (String parameterType : this.iMethod.getParameterTypes()) {
				result.append(delimiter);
				result.append(parameterType);
				delimiter = ",";
			}
			result.append(")");
			result.append("\n");
				
			for ( MethodNode n : this.children )
			{
				result.append (n.printGraphR(tabs+1));
			}
			
			return result.toString();
		}
		else
		{
			return "Fake MethodNode";
		}
	}

	public Set<ITypeBinding> getExternalExceptions()
	{
		Set<ITypeBinding> exceptions = new HashSet<>();
		
		exceptions.addAll(this.thrown);
		exceptions.addAll(this.rethrown);
		exceptions.addAll(this.propagated);
		exceptions.addAll(this.wrapped.keySet());
		
		return exceptions;
	}



	
}
