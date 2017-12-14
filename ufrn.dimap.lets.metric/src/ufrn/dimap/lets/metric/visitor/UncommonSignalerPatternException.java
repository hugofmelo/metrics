package ufrn.dimap.lets.metric.visitor;

import org.eclipse.jdt.core.dom.ASTNode;

import ufrn.dimap.lets.metric.visitor.UncommonCodePatternException;

public class UncommonSignalerPatternException extends UncommonCodePatternException
{
	
	public UncommonSignalerPatternException(String msg, ASTNode node)
	{
		super(msg, node);
	}

}
