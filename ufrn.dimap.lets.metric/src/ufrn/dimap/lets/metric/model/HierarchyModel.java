package ufrn.dimap.lets.metric.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import ufrn.dimap.lets.metric.visitor.UnresolvedBindingException;

public class HierarchyModel
{
	private static HierarchyModel instance = null;
	
	private TypeEntry typeRoot;
	
	private List<SignalerEntry> signalers;
	private List<TryEntry> tries;
	private List<CatchEntry> catches;
	private List<FinallyEntry> finallies;
	private int rethrows;
	private int wrappings;
	
	private HierarchyModel ()
	{
		this.typeRoot = null;
		
		signalers = new ArrayList<SignalerEntry> ();
		tries = new ArrayList<TryEntry> ();
		catches = new ArrayList<CatchEntry> ();
		finallies = new ArrayList<FinallyEntry> ();
		
		rethrows = 0;
		wrappings = 0;
	}
	
	public static HierarchyModel getInstance ()
	{
		if ( instance == null )
		{
			instance = new HierarchyModel();
		}
		
		return instance;
	}
	
	public static void clearInstance ()
	{
		instance = new HierarchyModel(); 
	}
	
	// Os tipos são armazenados em um estrutura hierarquica, tendo java.lang.Object como raiz.
	public TypeEntry getRoot()
	{
		return this.typeRoot;
	}	
	
	// Procura este tipo no modelo. Se não houver, cria. Retorna o tipo criado.
	public TypeEntry findOrCreateType (ITypeBinding typeBinding)
	{
		Stack<ITypeBinding> typesStack = HierarchyModel.createTypeHierarchy(typeBinding);
		
		if ( this.typeRoot == null)
		{
			this.typeRoot = new TypeEntry ( typesStack.peek() );
		}
		
		TypeEntry typeEntry = this.findOrCreateTypeAux(typesStack);
		
		return typeEntry;
	}
	
	// Chama o método findOrCreateType (ITypeBinding) e em seguida seta o node.
	public TypeEntry findOrCreateType (TypeDeclaration typeNode)
	{
		TypeEntry typeEntry = this.findOrCreateType(typeNode.resolveBinding());
		
		typeEntry.setNode (typeNode);
		
		return typeEntry;
	}
	
	public TypeEntry findOrCreateTypeAux (Stack<ITypeBinding> typesStack )
	{
		TypeEntry parentNode = this.typeRoot;
		boolean found;
		
		typesStack.pop(); // remover Object
		while ( !typesStack.empty() )
		{
			ITypeBinding peekType = typesStack.pop();
					
			// Procura o topo na lista de subtipos...
			found = false;
			for ( TypeEntry subNode : parentNode.subtypes )
			{
				// Se encontrar, desce um nível e interrompe o laço.
				if ( peekType.getQualifiedName().equals(((ITypeBinding)subNode.getBinding()).getQualifiedName() ))
				{
					found = true;
					parentNode = subNode;
					break;
				}
			}
			
			// Se não encontrar, o tipo não existia na hierarquia. Cria-se um novo nó e continua a busca descendo um nível.			
			if (!found)
			{
				TypeEntry newNode = new TypeEntry(peekType);
				parentNode.subtypes.add(newNode);
				newNode.superType = parentNode;
				parentNode = newNode;
			}
		}
		
		return parentNode;
	}
	
	public SignalerEntry addSignaler (ThrowStatement throwNode)
	{
		SignalerEntry signaler = new SignalerEntry (throwNode);
		this.signalers.add(signaler);
		
		// Associação as entidades entre si
		//TypeEntry signaledType = this.findOrCreateType(signaler.signaledException);
		//signaledType.signalers.add(signaler);
		
		return signaler;
	}

	public TryEntry addTry(TryStatement tryNode)
	{
		TryEntry tryEntry = new TryEntry(tryNode);
		this.tries.add(tryEntry);
		return tryEntry;
	}
	
	public CatchEntry addCatch(CatchClause catchNode)
	{
		CatchEntry catchEntry = new CatchEntry (catchNode);
		this.catches.add(catchEntry);
		
		// Associação as entidades entre si
		//TypeEntry catchedType = this.findOrCreateType(catchNode.getException().getType().resolveBinding());
		//catchedType.catches.add(catchEntry);
		
		return catchEntry;
	}
	
	public FinallyEntry addFinally(Block finallyBlock)
	{
		FinallyEntry finallyEntry = new FinallyEntry (finallyBlock);
		this.finallies.add(finallyEntry);
		return finallyEntry;
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
	

	// Esse método lança IllegalArgumentException se type não for uma classe.
	private static Stack<ITypeBinding> createTypeHierarchy (ITypeBinding type)
	{		
		if ( type.isClass() )
		{
			Stack<ITypeBinding> typesStack = new Stack <ITypeBinding> ();
			
			while ( !type.getQualifiedName().equals("java.lang.Object") )
			{
				typesStack.push(type);
				type = type.getSuperclass();
			}
			typesStack.push(type);
			
			return typesStack;
		}
		else
		{
			throw new IllegalArgumentException ("É esperada uma classe.\n\nARGUMENT: type\nVALUE: " + type.toString());
		}
	}
	
	
	
	
}
