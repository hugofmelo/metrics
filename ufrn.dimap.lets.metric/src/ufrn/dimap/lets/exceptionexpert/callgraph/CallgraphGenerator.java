package ufrn.dimap.lets.exceptionexpert.callgraph;

import java.util.HashMap;
import java.util.Stack;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.internal.corext.callhierarchy.CallHierarchy;
import org.eclipse.jdt.internal.corext.callhierarchy.MethodWrapper;

import ufrn.dimap.lets.exceptionexpert.model.MethodNode;

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
	
	private CallgraphGenerator ()
	{
		
	}

	/**
	 * Gera um grafo de chamadas podado. Métodos já descobertos não são re-visitados.
	 * 
	 * Usando o grafo podado, não existem nós para chamadas recursivas.
	 * 
	 * @param method Metodo que é ponto de entrada do call graph.
	 */
	public static MethodNode generateGraphFrom (IMethod method)
	{
		MethodWrapper rootMethodWrapper = initGraph(method);
		
		prunedDepthFirstSearch (rootMethodWrapper, fakeGraphRoot, new HashMap<>());
		
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
		//fakeGraphRoot = new MethodNode ((IMethod) wrapper.getMember(), null);
		fakeGraphRoot = new MethodNode (null, null);
		
		return wrapper;
	}

	/**
	 * Itera sobre os MethodWrappers, que implementam o call graph do eclipse, gerando o grafo cujos nós são MethodNodes.
	 * 
	 * A versão podada ignora novas ocorrencias de um mesmo método, mesmo que não seja na mesma pilha de execução.
	 * 
	 * @param wrapper MethodWrapper que está sendo processado.
	 * @param node Parent usado para facilitar as criação de conexões.
	 * @param discovered Nós que já foram descobertos. Usado para podar o callgraph.
	 */
	private static void prunedDepthFirstSearch(MethodWrapper wrapper, MethodNode parent, HashMap<String, MethodNode> discovered)
	{
		// Alguns MethodWrappers não são metodos de verdade.. É preciso verificar..
		if ( isMethod(wrapper) )
		{
			String identifier = wrapper.getMember().getHandleIdentifier();
			
			MethodNode child = discovered.get(identifier);
		 
			// O método nunca foi descoberto
			if (child == null)
			{
				child = new MethodNode((IMethod) wrapper.getMember(), parent);
				parent.getChildren().add(child);
				
				discovered.put(identifier, child);
				
				for (MethodWrapper w : wrapper.getCalls(new NullProgressMonitor()))
				{
					prunedDepthFirstSearch(w, child, discovered);
				}
			}
			else
			{
				parent.getChildren().add(child);
			}
		}
	}
	
	// Auxiliar methods
	
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
}