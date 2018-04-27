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
 * GallgraphGenerator converte o callgraph do eclipse, cujos n�s s�o MethodWrappers, em um novo modelo, que � baseado em MethodNodes, que � mais f�cil de navegar.
 * 
 * O callgraph do eclipse, implementado pela classe CallHierarchy, n�o possui formas built-in pr�ticas para se navegar, n�o reconhece recurs�o, e n�o funciona como esperado em alguns casos.
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
	 * Gera um grafo de chamadas podado. M�todos j� descobertos n�o s�o re-visitados.
	 * 
	 * Usando o grafo podado, n�o existem n�s para chamadas recursivas.
	 * 
	 * @param method Metodo que � ponto de entrada do call graph.
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
	 * Itera sobre os MethodWrappers, que implementam o call graph do eclipse, gerando o grafo cujos n�s s�o MethodNodes.
	 * 
	 * A vers�o podada ignora novas ocorrencias de um mesmo m�todo, mesmo que n�o seja na mesma pilha de execu��o.
	 * 
	 * @param wrapper MethodWrapper que est� sendo processado.
	 * @param node Parent usado para facilitar as cria��o de conex�es.
	 * @param discovered N�s que j� foram descobertos. Usado para podar o callgraph.
	 */
	private static void prunedDepthFirstSearch(MethodWrapper wrapper, MethodNode parent, HashMap<String, MethodNode> discovered)
	{
		// Alguns MethodWrappers n�o s�o metodos de verdade.. � preciso verificar..
		if ( isMethod(wrapper) )
		{
			String identifier = wrapper.getMember().getHandleIdentifier();
			
			MethodNode child = discovered.get(identifier);
		 
			// O m�todo nunca foi descoberto
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
}