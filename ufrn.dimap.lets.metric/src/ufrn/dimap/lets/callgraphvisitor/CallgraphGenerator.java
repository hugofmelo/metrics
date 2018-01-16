package ufrn.dimap.lets.callgraphvisitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.internal.corext.callhierarchy.CallHierarchy;
import org.eclipse.jdt.internal.corext.callhierarchy.MethodWrapper;

@SuppressWarnings("restriction")
public abstract class CallgraphGenerator
{	
	private CallHierarchy hierarchy;
	
	
	
	public static MethodNode generatePruned (CallHierarchy callHierarchy, IMethod method)
	{
		Set<String> discovered = new HashSet<>(); // Armazena todos os métodos já descobertos. Usado para podar a arvore ou skippar o processamento inteiro.
		Stack<MethodNode> processing = new Stack<>(); // Armazena a pilha de chamadas que está sendo processada no momento. Auxilia na identificação de recursão.
		
		MethodWrapper wrapper = callHierarchy.getCalleeRoots(new IMethod[]{method})[0];
		MethodNode graphRoot = new MethodNode ((IMethod) wrapper.getMember(), null);
		
		prunedDepthFirstSearch (wrapper, graphRoot, discovered, processing);
		
		return graphRoot;
	}

	private static void prunedDepthFirstSearch(MethodWrapper wrapper, MethodNode node, Set<String> discovered, Stack<MethodNode> processing)
	{
		String identifier = wrapper.getMember().getHandleIdentifier();
		
		if ( !discovered.contains(identifier))
		{
			discovered.add(identifier);
			
			if ( isMethod(wrapper) )
			{
				processing.push(node);
			}
			
			for (MethodWrapper w : wrapper.getCalls(new NullProgressMonitor()))
			{	
				if ( isRecursive(w, processing) )
				{
					MethodNode recursiveNode = findMethodNodeByIdentifier (w.getMember().getHandleIdentifier(), processing);
					recursiveNode.setRecursive(true);
				}
				else
				{
					MethodNode child = new MethodNode ((IMethod) w.getMember(), node);
					node.getChildren().add(child);
					prunedDepthFirstSearch(w, child, discovered, processing);
				}	
			}
			
			if ( isMethod(wrapper) )
			{
				processing.pop();
			}
		}	
	}
	
	public static MethodNode generateComplete (CallHierarchy callHierarchy, IMethod method)
	{
		Stack<MethodNode> processing = new Stack<>(); // Armazena a pilha de chamadas que está sendo processada no momento. Auxilia na identificação de recursão.
		
		MethodWrapper wrapper = callHierarchy.getCalleeRoots(new IMethod[]{method})[0];
		MethodNode graphRoot = new MethodNode ((IMethod) wrapper.getMember(), null);
		
		completeDepthFirstSearch (wrapper, graphRoot, processing);
		
		return graphRoot;
	}
	
	private static void completeDepthFirstSearch(MethodWrapper wrapper, MethodNode node, Stack<MethodNode> processing)
	{
		if ( isMethod(wrapper) )
		{
			processing.push(node);
		}
		
		for (MethodWrapper w : wrapper.getCalls(new NullProgressMonitor()))
		{
			if ( isRecursive(w, processing) )
			{
				MethodNode recursiveNode = findMethodNodeByIdentifier (w.getMember().getHandleIdentifier(), processing);
				recursiveNode.setRecursive(true);
			}
			else
			{
				MethodNode child = new MethodNode ((IMethod) w.getMember(), node);
				node.getChildren().add(child);
				completeDepthFirstSearch(w, child, processing);
			}
		}
		
		if ( isMethod(wrapper) )
		{
			processing.pop();
		}
	}

	
	// Auxiliar methods
	
	// Um methodWrapper as vezes é do tipo Type, e não Method. Ocorre com classes anonimas. Esse teste parece uma gambiarra, mas é necessário.
	private static boolean isMethod(MethodWrapper wrapper)
	{
		return wrapper.getMember().getElementType() == IJavaElement.METHOD; 
	}
	
	private static boolean isRecursive(MethodWrapper wrapper, Stack<MethodNode> processing)
	{
		for ( MethodNode node : processing )
		{
			if ( node.getIMethod().getHandleIdentifier().equals(wrapper.getMember().getHandleIdentifier()) )
				return true;
		}
		
		return false;
	}
	
	private static MethodNode findMethodNodeByIdentifier (String identifier, Stack<MethodNode> processing)
	{
		for ( MethodNode node : processing )
		{
			if ( node.getIMethod().getHandleIdentifier().equals(identifier))
				return node;
		}
		
		throw new IllegalStateException( "Método não existe na lista!" );
	}
}
