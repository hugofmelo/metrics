package ufrn.dimap.lets.exceptionalinterface;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import ufrn.dimap.lets.metric.handlers.HandlerUtil;

/**
 * Representa um nó no grafo de chamadas usado pela ferramenta.
 * */
public class MethodNode
{
	private MethodNode parent;
	private List<MethodNode> children;
	
	private IMethod iMethod;
	
	private ExceptionalInterface exceptionalInterface;
	private boolean recursive;
	
	public MethodNode(IMethod iMethod, MethodNode parent)
	{
		this.iMethod = iMethod;
		this.parent = parent;
		this.children = new ArrayList<>();
		this.exceptionalInterface = new ExceptionalInterface();
		this.recursive = false;
	}

	public void computeExceptionalInterface() throws JavaModelException
	{
		// Calcula a interface excepcional de todos os métodos em DFS
		for ( MethodNode child : this.getChildren() )
		{
			child.computeExceptionalInterface();
		}
		
		if ( isParseable (this.iMethod) )
		{
			CompilationUnit compilationUnit = HandlerUtil.parse(this.iMethod);
			MethodDeclaration methodDeclaration = MethodFinder.find ( this.iMethod, compilationUnit );
			
			
			EIVisitor eiVisitor = new EIVisitor (this, methodDeclaration);
			methodDeclaration.accept(eiVisitor);
			
			this.exceptionalInterface = eiVisitor.getExceptionalInterface();
			
			//if (!MethodValidator.hasNestedTryStatement(methodDeclaration))
			//{
			//	MethodVisitor methodVisitor = new MethodVisitor(this);
			//	methodDeclaration.accept(methodVisitor);
				
//				this.thrown.addAll(methodVisitor.thrownTypes);
//				
//				// Adicionar a interface excepcional dos métodos chamados no método atual
//				for (MethodNode callee : this.children)
//				{
//					this.rethrown.addAll(callee.getThrown());
//					this.rethrown.addAll(callee.getRethrown());
//				}
			//}
		//	else
		//	{
		//		addDeclaredExceptions();
		//	}
		}
		else
		{
			addDeclaredExceptions();
		}
	}
	
	/**
	 * As exceções declaradas pelo metodo na cláusula "throws". Para cada tipo, ele é procurado no JavaModel. Senão existir, ele simplesmente é ignorado.
	 * @throws JavaModelException
	 */
	private void addDeclaredExceptions() throws JavaModelException
	{
		// TODO Implementar de verdade
		for ( String exception : this.iMethod.getExceptionTypes() )
		{
			String qualifiedName = exception.substring(1, exception.length() - 1);
			
			IType type = this.iMethod.getJavaProject().findType(qualifiedName);
			
			if (type != null)
			{
				//Signaler signaler = new Signaler (new EIType(type), null, null, null);
				//this.exceptionalInterface.addSignaler(signaler);
			}
		}
	}

	/**
	 * O IMethod é parseable se possível código-fonte.
	 * 
	 * Ele possui código se é um CompilationUnit ou se é um ClassFile com código-fonte linkado. Por 
	 * vezes (acho que ocorre com construtores implícitos) a classe possui código-fonte,
	 * mas o método não.
	 * @param	method	O método a ser testado.
	 * */
	private boolean isParseable(IMethod method) throws JavaModelException
	{		
		if ( method.getSource() != null )
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
	
	public ExceptionalInterface getExceptionalInterface() {
		return exceptionalInterface;
	}

	public String toString ()
	{
		if ( this.iMethod != null )
		{
			StringBuilder result = new StringBuilder();
			
			result.append (this.iMethod.getDeclaringType().getFullyQualifiedName());
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
			
			return result.toString();
		}
		else
		{
			return "Fake MethodNode";
		}
	}
	
	public String printGraph ()
	{
		return this.printGraphR(0);
	}
	
	private String printGraphR (int tabs)
	{
		StringBuilder result = new StringBuilder();
		
		for ( int i = 0 ; i < tabs ; i++ )
			result.append("  "); 
		
		result.append (this.toString());
		result.append("\n");
			
		for ( MethodNode n : this.children )
		{
			result.append (n.printGraphR(tabs+1));
		}
		
		return result.toString();
	}

	
	
}
