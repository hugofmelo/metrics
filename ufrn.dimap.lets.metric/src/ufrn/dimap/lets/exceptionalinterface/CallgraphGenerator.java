package ufrn.dimap.lets.exceptionalinterface;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.internal.corext.callhierarchy.CallHierarchy;
import org.eclipse.jdt.internal.corext.callhierarchy.MethodWrapper;

/**
 * GallgraphGenerator converte o callgraph do eclipse, cujos nós são MethodWrappers, em um novo modelo, que é baseado em MethodNodes, que é mais fácil de navegar.
 * 
 * O callgraph do eclipse, implementado pela classe CallHierarchy, não possui formas built-in práticas para se navegar, não reconhece recursão, e não funciona como esperado em alguns casos.
 *   
 * @author Hugo Melo
 *
 */
@SuppressWarnings("restriction")
public class CallgraphGenerator
{	
	private static MethodNode fakeGraphRoot; // Raiz do grafo de chamadas
	private static Set <String> discovered; // List de nós que já foram descobertos. Usado para podar o callgraph.
	private static Stack<MethodNode> processing; // Pilha auxiliar de MethodNode que estão em processamento. Usado para verificar recursão. Usado somente no grafo completo. Na versão podada, a poda ocorre antes de haver recursão.
	
	private CallgraphGenerator ()
	{
		
	}

	/**
	 * Gera um grafo de chamadas completo.
	 * 
	 * @param method Metodo que é ponto de entrada do call graph.
	 */
	public static MethodNode generateCompleteGraphFrom (IMethod method)
	{
		MethodWrapper rootMethodWrapper = initGraph(method);
		
		processing = new Stack<>();
		
		completeDepthFirstSearch (rootMethodWrapper, fakeGraphRoot);
		
		return fakeGraphRoot.getChildren().get(0);
	}

	/**
	 * Gera um grafo de chamadas podado. Métodos já descobertos não são re-visitados.
	 * 
	 * Usando o grafo podado, não existem nós para chamadas recursivas.
	 * 
	 * @param method Metodo que é ponto de entrada do call graph.
	 */
	public static MethodNode generatePrunedGraphFrom (IMethod method)
	{
		MethodWrapper rootMethodWrapper = initGraph(method);
		
		discovered = new HashSet<>();
		
		prunedDepthFirstSearch (rootMethodWrapper, fakeGraphRoot);
		
		return fakeGraphRoot.getChildren().get(0);
	}

	private static MethodWrapper initGraph (IMethod method)
	{
		// Gerando CallHierarchy baseado no estado atual do eclipse, acredito eu
		CallHierarchy hierarchy = new CallHierarchy();
		IJavaSearchScope searchScope = SearchEngine.createWorkspaceScope();
		hierarchy.setSearchScope(searchScope); 
		
		// Acessa o call graph do eclipse e retorna sua raiz
		MethodWrapper wrapper = hierarchy.getCalleeRoots(new IMethod[]{method})[0];
		
		// Cria MethodNode para ser a raiz falsa
		fakeGraphRoot = new MethodNode ((IMethod) wrapper.getMember(), null);
		//fakeGraphRoot = new MethodNode (null, null);
		
		return wrapper;
	}

	/**
	 * Itera sobre os MethodWrappers, que implementam o call graph do eclipse, gerando o grafo cujos nós são MethodNodes.
	 * 
	 * @param wrapper 
	 * @param node
	 */
	private static void completeDepthFirstSearch(MethodWrapper wrapper, MethodNode parent)
	{
		if ( isMethod(wrapper) )
		{
			// É criado um novo nó para o método
			MethodNode child = new MethodNode ((IMethod) wrapper.getMember(), parent);
			parent.getChildren().add(child);
			
			// Se este método faz parte de uma recursão, ele é marcado como recursivo e seus filhos não são colocados novamente na busca
			if ( isRecursive (wrapper) )
			{
				child.setRecursive(true);
			}
			else
			{
				processing.push(child);
				
				for (MethodWrapper w : wrapper.getCalls(new NullProgressMonitor()))
				{	
					completeDepthFirstSearch(w, child);
				}
				
				processing.pop();
			}
		}
		else
		{
			for (MethodWrapper w : wrapper.getCalls(new NullProgressMonitor()))
			{	
				completeDepthFirstSearch(w, parent);
			}
		}
	}
	
	/**
	 * Itera sobre os MethodWrappers, que implementam o call graph do eclipse, gerando o grafo cujos nós são MethodNodes.
	 * 
	 * @param wrapper 
	 * @param node
	 */
	private static void prunedDepthFirstSearch(MethodWrapper wrapper, MethodNode parent)
	{
		if ( isMethod(wrapper) )
		{
			if (!discovered.contains(wrapper.getMember().getHandleIdentifier()))
			{
				discovered.add(wrapper.getMember().getHandleIdentifier());
				
				// É criado um novo nó para o método
				MethodNode child = new MethodNode((IMethod) wrapper.getMember(), parent);
				parent.getChildren().add(child);

				for (MethodWrapper w : wrapper.getCalls(new NullProgressMonitor()))
				{
					prunedDepthFirstSearch(w, child);
				}
			}
		}
		else
		{
			for (MethodWrapper w : wrapper.getCalls(new NullProgressMonitor()))
			{	
				prunedDepthFirstSearch(w, parent);
			}
		}
	}
	
	// Auxiliar methods
	
	/**
	 * Um MethodWrapper as vezes é do tipo Type, e não Method. Ocorre com classes anonimas. Esse teste parece uma gambiarra, mas é necessário.
	 * 
	 * @param wrapper o MethodWrapper a ser testado.
	 */
	private static boolean isMethod(MethodWrapper wrapper)
	{
		return wrapper.getMember().getElementType() == IJavaElement.METHOD; 
	}
	
	/**
	 * Verifica se o método chamado já está na pilha de processamento e, portanto, gera uma recursão. 
	 * @param wrapper
	 */
	private static boolean isRecursive (MethodWrapper wrapper)
	{
		for ( MethodNode node : processing )
		{
			if ( node.getIdentifier().equals(wrapper.getMember().getHandleIdentifier() ))
			{
				return true;
			}
		}
		
		return false;
	}
}
