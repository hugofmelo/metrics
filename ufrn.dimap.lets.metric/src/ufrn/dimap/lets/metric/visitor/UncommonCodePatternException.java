package ufrn.dimap.lets.metric.visitor;

import org.eclipse.jdt.core.dom.ASTNode;

public abstract class UncommonCodePatternException extends RuntimeException
{
	private ASTNode astNode;
	
	public UncommonCodePatternException (String msg, ASTNode node)
	{
		super (msg);
		astNode = node;
	}
	
	public ASTNode getASTNode ()
	{
		return astNode;
	}
}
