package ufrn.dimap.lets.exceptionexpert.model;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import ufrn.dimap.lets.exceptionexpert.exceptionalinterface.MethodFinder;
import ufrn.dimap.lets.exceptionexpert.visitor.ExceptionalInterfaceVisitor;
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
		this.exceptionalInterface = null;
		// TODO processar callgraph e interface junto 
		//this.exceptionalInterface = new ExceptionalInterface();
		this.recursive = false;
	}

	public void computeExceptionalInterface() throws JavaModelException
	{
		// TODO processar callgraph e interface junto
		// É possível que este MethodNode já tenha sua interface calculada (caso ele apareça várias vezes no grafo de chamadas) ou carregada
		if ( this.exceptionalInterface != null )
		{
			return;
		}
		
		// Calcula a interface excepcional de todos os métodos em DFS
		this.exceptionalInterface = new ExceptionalInterface();
		
		for ( MethodNode child : this.getChildren() )
		{
			child.computeExceptionalInterface();
		}
		
		if ( isParseable (this.iMethod) )
		{
			CompilationUnit compilationUnit = HandlerUtil.parse(this.iMethod);
			MethodDeclaration methodDeclaration = MethodFinder.find ( this.iMethod, compilationUnit );
			
			
			ExceptionalInterfaceVisitor eiVisitor = new ExceptionalInterfaceVisitor (this, methodDeclaration);
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
	private void addDeclaredExceptions()
	{
		// TODO Implementar de verdade
//		for ( String exception : this.iMethod.getExceptionTypes() )
//		{
//			String qualifiedName = exception.substring(1, exception.length() - 1);
//			
//			IType type = this.iMethod.getJavaProject().findType(qualifiedName);
//			
//			if (type != null)
//			{
//				//Signaler signaler = new Signaler (new EIType(type), null, null, null);
//				//this.exceptionalInterface.addSignaler(signaler);
//			}
//		}
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
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	private boolean isParseable2(IMethod method) throws JavaModelException
	{		
		return method.getSource() != null && !isAbstract(method.getSource());
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
	
	
	
	
}
