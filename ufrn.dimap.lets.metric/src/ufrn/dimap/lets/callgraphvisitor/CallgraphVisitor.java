package ufrn.dimap.lets.callgraphvisitor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.internal.corext.callhierarchy.CallHierarchy;
import org.eclipse.jdt.internal.corext.callhierarchy.MethodWrapper;

@SuppressWarnings("restriction")
public abstract class CallgraphVisitor
{	
	private CallHierarchy hierarchy;
	private Orientation orientation;
	
	private MethodWrapper root;
	private Set<String> discovered; // Armazena todos os métodos já descobertos. Usado para podar a arvore ou skippar o processamento inteiro. Não reseta entre diferentes chamadas de accept.
	private Stack<String> processing; // Armazena a pilha de chamadas que está sendo processada no momento. Auxilia na identificação de recursão.
	
	
	public CallgraphVisitor(CallHierarchy callHierarchy, Orientation orientation)
	{
		this.hierarchy = callHierarchy;
		this.orientation = orientation;
		this.discovered = new HashSet<>(); 
	}
	
	public void accept (IMethod method)
	{	
		if ( this.orientation == Orientation.CALLEES )
		{
			this.root = hierarchy.getCalleeRoots(new IMethod[]{method})[0];
			this.processing = new Stack<>();
			this.depthFirstSearch (this.root);
		}
		else
		{
			throw new UnsupportedOperationException();
			//methodWrapper = hierarchy.getCallerRoots(new IMethod[]{method})[0];
		}
	}

	private void depthFirstSearch(MethodWrapper wrapper)
	{
		String identifier = wrapper.getMember().getHandleIdentifier();
		
		if ( !discovered.contains(identifier) )
		{
			discovered.add(identifier);
			
			if ( isMethod(wrapper) )
			{
				this.processing.push(identifier);
				this.preVisit((IMethod)wrapper.getMember());
			}
			
			for (MethodWrapper w : wrapper.getCalls(new NullProgressMonitor()))
			{
				if ( !isRecursive(w) )
				{
					depthFirstSearch(w);
				}
					
			}
			
			if ( isMethod(wrapper) )
			{
				this.postVisit((IMethod) wrapper.getMember());
				this.processing.pop();
			}
		}	
	}

	public abstract void preVisit(IMethod method);
	
	public abstract void postVisit(IMethod method);	
	
	
	
	// Utility methods
	
	public List<IMethod> getChildren (IMethod method)
	{
		List<IMethod> children = new ArrayList<>();
		
		MethodWrapper wrapper = this.hierarchy.getCalleeRoots(new IMethod[]{method})[0];
		
		for (MethodWrapper w : wrapper.getCalls(new NullProgressMonitor()))
		{
			if ( w.getMember().getElementType() == IJavaElement.METHOD )
			{
				if (!this.isRecursive(w))
				{
					children.add((IMethod) w.getMember());
				}
			}
		}
		
		return children;
	}

	
	// Auxiliar methods
	
	// Um methodWrapper as vezes é do tipo Type, e não Method. Ocorre com classes anonimas. Esse teste parece uma gambiarra, mas é necessário.
	private boolean isMethod(MethodWrapper wrapper)
	{
		return wrapper.getMember().getElementType() == IJavaElement.METHOD; 
	}
	
	private boolean isRecursive(MethodWrapper wrapper)
	{
		return processing.contains(wrapper.getMember().getHandleIdentifier());
	}
}
