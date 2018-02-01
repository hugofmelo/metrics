package ufrn.dimap.lets.exceptionalinterface;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;

/**
 * O MethodFinder possui métodos estáticos que procuram por determinado método em um compilationUnit. */
public class MethodFinder extends ASTVisitor
{
	private static MethodFinder finder = new MethodFinder();
	private IMethod targetMethod;
	private MethodDeclaration targetDeclaration;
	
	private MethodFinder () {}
	
	public static MethodDeclaration find(IMethod method, CompilationUnit compilationUnit)
	{
		finder.setTargetMethod (method);
		finder.setTargetDeclaration(null);
		
		compilationUnit.accept(finder);		
		
		return finder.getTargetDeclaration();
	}
	
	@Override
	public boolean preVisit2 (ASTNode node)
	{
		// Visit qualquer coisa enquanto declaration for nulo
		return this.targetDeclaration == null;
	}
	
	@Override
	public boolean visit ( MethodDeclaration method )
	{
		if (((IMethod)method.resolveBinding().getJavaElement()).getHandleIdentifier().equals(targetMethod.getHandleIdentifier()))
		{
			targetDeclaration = method;
			return false;
		}
		
		return true;	
	}

	

	private MethodDeclaration getTargetDeclaration()
	{
		if ( this.targetDeclaration == null )
		{
			throw new ShouldNotHappenException("MethodDeclaration não encontrado em CompilationUnit.");
		}
		else
		{
			return this.targetDeclaration;
		}
	}

	private void setTargetMethod(IMethod method)
	{
		this.targetMethod = method;
	}
	
	private void setTargetDeclaration(MethodDeclaration methodDeclaration)
	{
		this.targetDeclaration = methodDeclaration;
	}
}
