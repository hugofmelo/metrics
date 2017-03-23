package ufrn.dimap.lets.metric.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.TypeDeclaration;

public class TypeEntry extends AbstractEntry
{
	public TypeEntry superType;
	public List<TypeEntry> subtypes;
	
	//public List<SignalerEntry> signalers;
	//public List<CatchEntry> catches;
	
	public TypeEntry (TypeDeclaration node)
	{
		super(node, node.resolveBinding());
		
		this.superType = null;
		this.subtypes = new ArrayList<TypeEntry>();
		
		//this.signalers = new ArrayList<SignalerEntry>();
		//this.catches = new ArrayList<CatchEntry>();
	}
	
	public TypeEntry (ITypeBinding binding)
	{
		super(binding);
		
		this.superType = null;
		this.subtypes = new ArrayList<TypeEntry>();
		
		//this.signalers = new ArrayList<SignalerEntry>();
		//this.catches = new ArrayList<CatchEntry>();
	}
	
	public IJavaElement getJavaElement()
	{
		return this.getBinding().getJavaElement();
	}
	

	public List<ITypeBinding> getAllTypes()
	{
		List <ITypeBinding> types = new ArrayList<ITypeBinding>();
		
		types.addAll(getAllTypesR(this));
		
		return types;
	}
	
	public static List<ITypeBinding> getAllTypesR(TypeEntry node)
	{
		List <ITypeBinding> types = new ArrayList<ITypeBinding>();
		
		types.add((ITypeBinding) node.getBinding());
		
		for ( TypeEntry n : node.subtypes )
		{
			types.addAll(getAllTypesR(n));
		}
		
		return types;
	}
	
	public String toString()
	{
		return this.toStringR(this, "");
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
}

