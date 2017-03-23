package ufrn.dimap.lets.metric.model;

import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.ITypeBinding;

public class CatchEntry extends AbstractEntry
{
	public ITypeBinding catchedException;
	
	public CatchEntry( CatchClause node )
	{
		super(node);
	
		this.catchedException = node.getException().getType().resolveBinding();
	}

}
