package ufrn.dimap.lets.metric.model.hierarchy;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import ufrn.dimap.lets.metric.model.AbstractViewEntry;

public class TypeEntry extends AbstractViewEntry
{
	public TypeEntry superType;
	public List<TypeEntry> subtypes;
	
	public TypeEntry (TypeDeclaration node)
	{
		super(node, node.resolveBinding());
		
		this.superType = null;
		this.subtypes = new ArrayList<TypeEntry>();
	}
	
	public TypeEntry (ITypeBinding binding)
	{
		super(binding);
		
		this.superType = null;
		this.subtypes = new ArrayList<TypeEntry>();
	}
	
	/*
	public TypeEntry (TypeDeclaration node, HierarchyNode parent )
	{
		//super()
		
		this.exception = exception;
		this.parent = parent;
		this.used = false;
		this.children = new ArrayList<HierarchyNode>();
	}
	*/
}

