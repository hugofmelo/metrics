package ufrn.dimap.lets.exceptionalinterface;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.internal.core.JavaModel;

import ufrn.dimap.lets.metric.handlers.HandlerUtil;

public class MethodNode
{
	private IMethod iMethod;
	private MethodNode parent;
	private List<MethodNode> children;
	private boolean recursive;
	
	private Map<ITypeBinding, Set<ITypeBinding>> caught;
	private Set<ITypeBinding> propagated;
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
			
			if (!MethodValidator.hasNestedTryStatement(methodDeclaration))
			{
				MethodVisitor methodVisitor = new MethodVisitor(this);
				methodDeclaration.accept(methodVisitor);
				
//				this.thrown.addAll(methodVisitor.thrownTypes);
//				
//				// Adicionar a interface excepcional dos m�todos chamados no m�todo atual
//				for (MethodNode callee : this.children)
//				{
//					this.rethrown.addAll(callee.getThrown());
//					this.rethrown.addAll(callee.getRethrown());
//				}
			}
			else
			{
				addDeclaredExceptions();
			}
		}
		else
		{
			addDeclaredExceptions();
		}
	}
	
	/**
	 * As exce��es declaradas pelo metodo na cl�usula "throws". Para cada tipo, ele � procurado no JavaModel. Sen�o existir, ele simplesmente � ignorado.
	 * @throws JavaModelException
	 */
	private void addDeclaredExceptions() throws JavaModelException
	{
		// TODO
		for ( String exception : this.iMethod.getExceptionTypes() )
		{
			String qualifiedName = exception.substring(1, exception.length() - 1);
			
			IType type = this.iMethod.getJavaProject().findType(qualifiedName);
			
			if (type != null)
			{
				//this.propagated.add(type);
			}
		}
	}

	/**
	 * O IMethod � parseable se n�o � nativo e possui c�digo-fonte (� um CompilationUnit ou um ClassFile com c�digo-fonte linkado).
	 * @param	method	O m�todo a ser testado.
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
	 * Dado uma string que representa o c�digo-fonte de um m�todo (podendo incluir Javadoc),
	 * indica se este m�todo � abstrato (definido em uma classe abstrata ou em uma interface).
	 * @param	source	c�digo-fonte do m�todo 
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
	
	public Map<ITypeBinding, Set<ITypeBinding>> getCaught() {
		return caught;
	}

	public Set<ITypeBinding> getPropagated() {
		return propagated;
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

	public Set<ITypeBinding> getExternalExceptions()
	{
		Set<ITypeBinding> exceptions = new HashSet<>();
		
		exceptions.addAll(this.thrown);
		exceptions.addAll(this.rethrown);
		exceptions.addAll(this.propagated);
		exceptions.addAll(this.wrapped.keySet());
		
		return exceptions;
	}

	public void addCaught(ITypeBinding realCaughtType, ITypeBinding catchType)
	{
		Set<ITypeBinding> caughtAs = this.caught.get(realCaughtType);
		if ( caughtAs == null )
		{
			caughtAs = new HashSet<>();
		}
		
		caughtAs.add(catchType);
		
		this.caught.put(realCaughtType, caughtAs);
	}

	public void addPropagated(ITypeBinding type)
	{
		this.propagated.add(type);
	}

	public void addThrown(ITypeBinding type)
	{
		this.thrown.add(type);
	}

	public void addRethrown(ITypeBinding type)
	{
		this.rethrown.add(type);
	}

	public void addWrapped(ITypeBinding wrapperType, ITypeBinding wrappedType)
	{
		Set<ITypeBinding> wrappedAs = this.caught.get(wrapperType);
		if ( wrappedAs == null )
		{
			wrappedAs = new HashSet<>();
		}
		
		wrappedAs.add(wrappedType);
		
		this.caught.put(wrapperType, wrappedAs);
	}
}
