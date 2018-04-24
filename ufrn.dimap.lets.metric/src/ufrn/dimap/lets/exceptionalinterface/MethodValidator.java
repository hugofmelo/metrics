package ufrn.dimap.lets.exceptionalinterface;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TryStatement;

/**
 * O MethodValidator possui métodos estáticos que determinam se o código-fonte utiliza padrões suportados pela ferramenta..
 * */
public class MethodValidator extends ASTVisitor
{
	private static MethodValidator validator = new MethodValidator();
	private boolean onTryStatement;
	private boolean hasNestedStatement;
	
	private MethodValidator () {}
	
	public static boolean hasNestedTryStatement(MethodDeclaration methodDeclaration)
	{
		validator.hasNestedStatement = false;
		validator.onTryStatement = false;
		
		methodDeclaration.accept(validator);
		
		return validator.hasNestedStatement;
	}
	
	@Override
	public boolean preVisit2 ( ASTNode node )
	{
		return !validator.hasNestedStatement;	
	}
	
	@Override
	public boolean visit ( TryStatement tryStatement )
	{
		if ( validator.onTryStatement )
		{
			validator.hasNestedStatement = true;
		}
		else
		{
			validator.onTryStatement = true;
		}
		
		return true;	
	}
	
	@Override
	public void endVisit ( TryStatement tryStatement )
	{
		if ( validator.onTryStatement )
		{
			validator.onTryStatement = false;
		}
	}
}
