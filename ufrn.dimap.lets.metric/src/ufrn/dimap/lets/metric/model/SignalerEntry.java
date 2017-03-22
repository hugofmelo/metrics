package ufrn.dimap.lets.metric.model;

import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.ThrowStatement;

public class SignalerEntry extends AbstractViewEntry
{
	public ITypeBinding signaledException;
	public boolean rethrow;
	public boolean wrapping;

	public boolean regularPattern;
	
	
	public SignalerEntry( ThrowStatement node )
	{
		super(node);
		
		this.signaledException = node.getExpression().resolveTypeBinding();
		this.rethrow = false;
		this.wrapping = false;
		this.regularPattern = false;
	}

}
