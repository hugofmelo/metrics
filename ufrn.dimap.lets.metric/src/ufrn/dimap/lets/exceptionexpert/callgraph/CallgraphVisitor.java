package ufrn.dimap.lets.exceptionexpert.callgraph;

import java.util.Stack;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.internal.corext.callhierarchy.CallHierarchy;
import org.eclipse.jdt.internal.corext.callhierarchy.CallHierarchyVisitor;
import org.eclipse.jdt.internal.corext.callhierarchy.MethodWrapper;

import ufrn.dimap.lets.exceptionexpert.exceptionalinterface.ShouldNotHappenException;
import ufrn.dimap.lets.exceptionexpert.model.MethodNode;

/**
 * GallgraphVisitor visita o callgraph do eclipse, cujos n�s s�o MethodWrappers, e gera um novo modelo, que � baseado em MethodNodes.
 * 
 * O callgraph gerado � completo. Mesmo n�s que j� apareceram s�o re-visitados.   
 * @author Hugo Melo
 *
 */
@SuppressWarnings("restriction")
public class CallgraphVisitor extends CallHierarchyVisitor
{	
	private MethodNode fakeGraphRoot; // Raiz do grafo de chamadas
	private Stack<MethodNode> processing;
	
	public CallgraphVisitor ()
	{
		fakeGraphRoot = new MethodNode (null, null);
		processing = new Stack<MethodNode>();
		processing.push(fakeGraphRoot);
	}

	public static MethodWrapper convertToWrapper (IMethod method)
	{
		CallHierarchy hierarchy = new CallHierarchy();
		IJavaSearchScope searchScope = SearchEngine.createWorkspaceScope();
		
		hierarchy.setSearchScope(searchScope);
		MethodWrapper wrapper = hierarchy.getCalleeRoots(new IMethod[]{method})[0];
		
		return wrapper;
	}
	
    public boolean visit(MethodWrapper wrapper)
    {
    	// Alguns MethodWrappers n�o s�o metodos de verdade.. � preciso verificar..
		if ( isMethod(wrapper) )
		{
			String identifier = wrapper.getMember().getHandleIdentifier();
			MethodNode parent = processing.peek(); 
			
			if (!wrapper.isRecursive())
			{
				MethodNode child = new MethodNode((IMethod) wrapper.getMember(), parent);
				parent.getChildren().add(child);
				
				processing.push(child);
			}
			else
			{
				MethodNode child = findOnStack(identifier);
				parent.getChildren().add(child);
			}
			
			return true;
		}
		else
		{
			return false;
		}        
    }
    
    public void postVisit (MethodWrapper wrapper)
    {
    	if ( isMethod(wrapper) && !wrapper.isRecursive() )
		{
    		this.processing.pop();
		}
    }
	
	// Auxiliar methods
	
	private MethodNode findOnStack(String handleIdentifier)
	{
		for ( MethodNode method : this.processing )
		{
			if ( method.getIdentifier().equals(handleIdentifier) )
			{
				return method;
			}
		}
		
		throw new ShouldNotHappenException("MethodNode n�o existe na stack, mas m�todo � recursivo.");
	}

	/**
	 * Um MethodWrapper as vezes � do tipo Type, e n�o Method.
	 * 
	 * Ocorre com classes anonimas. Esse teste parece uma gambiarra, mas � necess�rio.
	 * 
	 * @param wrapper o MethodWrapper a ser testado.
	 */
	private static boolean isMethod(MethodWrapper wrapper)
	{
		return wrapper.getMember().getElementType() == IJavaElement.METHOD; 
	}
	

	public MethodNode getRoot() {
		return fakeGraphRoot.getChildren().get(0);
	}
}
