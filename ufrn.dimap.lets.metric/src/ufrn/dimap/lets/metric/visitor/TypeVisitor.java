package ufrn.dimap.lets.metric.visitor;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import ufrn.dimap.lets.metric.model.HierarchyModel;

public class TypeVisitor extends ASTVisitor
{
	public HierarchyModel hierarchyModel;
	
	// Construtor
	public TypeVisitor ()
	{
		this.hierarchyModel = HierarchyModel.getInstance();
	}
	
	// Métodos visit
	public boolean visit (TypeDeclaration node)
	{		
		
		if ( isSubtypeOfThrowable( node.resolveBinding() ) )
		{
			this.hierarchyModel.findOrCreateType(node);
		}	
		
		/*
		if ( !node.isInterface() )
		{
			this.hierarchyModel.addType (node);
		}
		*/
		
		return true;
	}

	// Métodos auxiliares
	private static boolean isSubtypeOfThrowable (ITypeBinding type)
	{
		return isSubtypeOfThrowableR (type);
	}
		
	private static boolean isSubtypeOfThrowableR (ITypeBinding type)
	{
		if (type.isClass() == false)
		{
			return false;
		}
		else
		{
			if ( type.getQualifiedName().equals("java.lang.Throwable") )
			{
				return true;
			}
			else if ( type.getQualifiedName().equals("java.lang.Object") )
			{
				return false;
			}
			else
			{
				if ( type.getSuperclass() == null)
				{
					throw new UnresolvedBindingException ( "Failed to resolve superclass of " + type.getQualifiedName() + " class.");
				}
				else
				{
					return isSubtypeOfThrowableR(type.getSuperclass());
				}
			}
		}
	}
}


