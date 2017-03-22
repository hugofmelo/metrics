package ufrn.dimap.lets.metric.model.hierarchy;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.eclipse.jdt.core.dom.ITypeBinding;

import ufrn.dimap.lets.metric.model.CatchEntry;
import ufrn.dimap.lets.metric.model.FinallyEntry;
import ufrn.dimap.lets.metric.model.SignalerEntry;
import ufrn.dimap.lets.metric.model.TryEntry;

public class HierarchyModel
{
	private TypeEntry root;
	
	private List<SignalerEntry> signalers;
	private List<TryEntry> tries;
	private List<CatchEntry> catches;
	private List<FinallyEntry> finallies;
	private int rethrows;
	private int wrappings;
	
	public HierarchyModel ()
	{
		this.root = null;
		
		signalers = new ArrayList<SignalerEntry> ();
		tries = new ArrayList<TryEntry> ();
		catches = new ArrayList<CatchEntry> ();
		finallies = new ArrayList<FinallyEntry> ();
		
		rethrows = 0;
		wrappings = 0;
	}
	
	public void addType (Stack<ITypeBinding> types)
	{
		if ( this.root == null)
		{
			this.root = new TypeEntry ( types.peek() );
		}
		
		types.pop(); // retirar Object	
		this.addTypeR(types, this.root);
				
	}
	
	
	public void addTypeR (Stack<ITypeBinding> typesStack, TypeEntry parentNode )
	{
				
		// Para cada tipo da pilha....
		if ( typesStack.empty() == false )
		{
			ITypeBinding peekType = typesStack.pop();
			List <TypeEntry> subNodes = parentNode.subtypes;
			
			
			// Procura ela na lista...
			for ( TypeEntry subNode : subNodes )
			{
				// Se encontrar, desce um nível e interrompe o laço.
				if ( peekType.getQualifiedName().equals(((ITypeBinding)subNode.getBinding()).getQualifiedName() ))
				{
					addTypeR(typesStack, subNode);
					return;
				}
			}
			
			// Se não encontrar, o tipo não existia na hierarquia. Cria-se um novo nó e continua a busca descendo um nível.
			TypeEntry newNode = new TypeEntry(peekType); 
			parentNode.subtypes.add(newNode);
			newNode.superType = parentNode;
			addTypeR(typesStack, newNode);
		}
	}
	

	public String toString()
	{
		return this.toStringR(this.root, "");
	}
	
	public String toStringR(TypeEntry node, String tabs)
	{
		String result = "";
			
		result = tabs + ((ITypeBinding)node.getBinding()).getQualifiedName() + "\n";
		
		for ( TypeEntry n : node.subtypes )
		{
			result += this.toStringR(n, tabs+"\t");
		}
		
		return result;
	}
	
	public List<ITypeBinding> getAllTypes()
	{
		List <ITypeBinding> types = new ArrayList<ITypeBinding>();
		
		types.addAll(HierarchyModel.getAllTypesR(this.root));
		
		return types;
	}
	
	public static List<ITypeBinding> getAllTypesR(TypeEntry node)
	{
		List <ITypeBinding> types = new ArrayList<ITypeBinding>();
		
		types.add((ITypeBinding) node.getBinding());
		
		for ( TypeEntry n : node.subtypes )
		{
			types.addAll(HierarchyModel.getAllTypesR(n));
		}
		
		return types;
	}

	public TypeEntry getRoot() {
		return this.root;
	}
	
	/*
	private static boolean compareTypes (IType typeA, IType typeB)
	{
		return typeA.getFullyQualifiedName().equals(typeB.getFullyQualifiedName());
	}
	*/
	
	public void addSignalerEntry (SignalerEntry entry)
	{
		this.signalers.add(entry);
	}

	public void addTryEntry(TryEntry entry)
	{
		this.tries.add(entry);
	}
	
	public void addCatchEntry(CatchEntry entry)
	{
		this.catches.add(entry);
	}
	
	public void addFinallyEntry(FinallyEntry entry)
	{
		this.finallies.add(entry);
	}
	
	public List<SignalerEntry> getSignalers()
	{
		return this.signalers;
	}
	
	public List<TryEntry> getTries()
	{
		return this.tries;
	}
	
	public List<CatchEntry> getCatches()
	{
		return this.catches;
	}
	
	public List<FinallyEntry> getFinallies()
	{
		return this.finallies;
	}

	public int getRethrows ()
	{
		return this.rethrows;
	}
	
	public void incrementRethrows ()
	{
		this.rethrows++;
	}

	public int getWrappings ()
	{
		return this.wrappings;
	}
	
	public void incrementWrappings ()
	{
		this.wrappings++;
	}
}
