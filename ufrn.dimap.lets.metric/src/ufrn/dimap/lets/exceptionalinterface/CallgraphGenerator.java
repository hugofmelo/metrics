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
	private static Stack<MethodNode> processing; // Pilha auxiliar de MethodNode que est�o em processamento. Usado para verificar recurs�o. Usado somente no grafo completo. Na vers�o podada, a poda ocorre antes de haver recurs�o.
	
	private CallgraphGenerator ()
	{
		
	}

	/**
	 * Gera um grafo de chamadas completo.
	 * 
	 * @param method Metodo que � ponto de entrada do call graph.
	 */
	public static MethodNode generateCompleteGraphFrom (IMethod method)
	{
		MethodWrapper rootMethodWrapper = initGraph(method);
		
		processing = new Stack<>();
		
		completeDepthFirstSearch (rootMethodWrapper, fakeGraphRoot);
		
		return fakeGraphRoot.getChildren().get(0);
	}

	/**
	 * Gera um grafo de chamadas podado. M�todos j� descobertos n�o s�o re-visitados.
	 * 
	 * Usando o grafo podado, n�o existem n�s para chamadas recursivas.
	 * 
	 * @param method Metodo que � ponto de entrada do call graph.
	 */
	public static MethodNode generatePrunedGraphFrom (IMethod method)
	{
		MethodWrapper rootMethodWrapper = initGraph(method);
		
		prunedDepthFirstSearch (rootMethodWrapper, fakeGraphRoot, new HashSet<>());
		
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
	 * @param wrapper 
	 * @param node
	 */
	private static void completeDepthFirstSearch(MethodWrapper wrapper, MethodNode parent)
	{
		// Alguns MethodWrappers n�o s�o metodos de verdade.. � preciso verificar..
		if ( isMethod(wrapper) )
		{
			// � criado um novo n� para o m�todo
			MethodNode child = new MethodNode ((IMethod) wrapper.getMember(), parent);
			parent.getChildren().add(child);
			
			// Se este m�todo faz parte de uma recurs�o, ele � marcado como recursivo.
			if ( isRecursive (wrapper) )
			{
				child.setRecursive(true);
			}
			else // sen�o � recursivo, a busca continua com os callees do m�todo.
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
	 * Itera sobre os MethodWrappers, que implementam o call graph do eclipse, gerando o grafo cujos n�s s�o MethodNodes.
	 * 
	 * A vers�o podada ignora novas ocorrencias de um mesmo m�todo, mesmo que n�o seja na mesma pilha de execu��o.
	 * 
	 * @param wrapper MethodWrapper que est� sendo processado.
	 * @param node Parent usado para facilitar as cria��o de conex�es.
	 * @param discovered N�s que j� foram descobertos. Usado para podar o callgraph.
	 */
	private static void prunedDepthFirstSearch(MethodWrapper wrapper, MethodNode parent, HashSet<String> discovered)
	{
		// Alguns MethodWrappers n�o s�o metodos de verdade.. � preciso verificar..
		if ( isMethod(wrapper) )
		{
			if (!discovered.contains(wrapper.getMember().getHandleIdentifier()))
			{
				discovered.add(wrapper.getMember().getHandleIdentifier());
				
				// � criado um novo n� para o m�todo
				MethodNode child = new MethodNode((IMethod) wrapper.getMember(), parent);
				parent.getChildren().add(child);

				for (MethodWrapper w : wrapper.getCalls(new NullProgressMonitor()))
				{
					prunedDepthFirstSearch(w, child, discovered);
				}
			}
		}
		else
		{
			for (MethodWrapper w : wrapper.getCalls(new NullProgressMonitor()))
			{	
				prunedDepthFirstSearch(w, parent, discovered);
			}
		}
	}
	
	// Auxiliar methods
	
	/**
	 * Um MethodWrapper as vezes � do tipo Type, e n�o Method. Ocorre com classes anonimas. Esse teste parece uma gambiarra, mas � necess�rio.
	 * 
	 * @param wrapper o MethodWrapper a ser testado.
	 */
	private static boolean isMethod(MethodWrapper wrapper)
	{
		return wrapper.getMember().getElementType() == IJavaElement.METHOD; 
	}
	
	/**
	 * Verifica se o m�todo chamado j� est� na pilha de processamento e, portanto, gera uma recurs�o. 
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
