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
 * GallgraphVisitor visita o callgraph do eclipse, cujos nós são MethodWrappers, e gera um novo modelo, que é baseado em MethodNodes.
 * 
 * O callgraph gerado é completo. Mesmo nós que já apareceram são re-visitados.   
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
    	// Alguns MethodWrappers não são metodos de verdade.. É preciso verificar..
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
		
		throw new ShouldNotHappenException("MethodNode não existe na stack, mas método é recursivo.");
	}

	/**
	 * Um MethodWrapper as vezes é do tipo Type, e não Method.
	 * 
	 * Ocorre com classes anonimas. Esse teste parece uma gambiarra, mas é necessário.
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
