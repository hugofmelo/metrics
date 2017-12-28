package ufrn.dimap.lets.callgraphvisitor;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.internal.corext.callhierarchy.CallHierarchy;

@SuppressWarnings("restriction")
public class CallgraphVisitor
{	
	private CallHierarchy hierarchy;
	
	public CallgraphVisitor(CallHierarchy callHierarchy, Orientation orientation)
	{
		this.hierarchy = callHierarchy;
	}
	
	public void accept (IMethod method)
	{
		
	}

	public boolean hasNext ()
	{
		
	}
	
	public IMethod next()
	{
		
	}
	
}
