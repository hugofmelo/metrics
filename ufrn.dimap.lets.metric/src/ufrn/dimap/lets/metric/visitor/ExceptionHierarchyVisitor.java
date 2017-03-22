package ufrn.dimap.lets.metric.visitor;

import java.util.Stack;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import ufrn.dimap.lets.metric.model.hierarchy.HierarchyModel;

public class ExceptionHierarchyVisitor extends ASTVisitor
{
	public HierarchyModel exceptionHierarchyModel;
	
	// Essa pilha auxiliar serve para não calcular 2 vezes a hierarquia da exceção 
	private Stack <ITypeBinding> typesStack;
	
	
	public ExceptionHierarchyVisitor ( HierarchyModel exceptionHierarchyModel )
	{
		this.exceptionHierarchyModel = exceptionHierarchyModel;
	}
	
	public boolean visit (TypeDeclaration node)
	{
		ITypeBinding typeBinding;
		
		typeBinding = node.resolveBinding();
		
				
		if ( this.isSubtypeOfThrowable( typeBinding ) == true )
		{
			this.exceptionHierarchyModel.addType(typesStack);
		}	

		return true;
	}

	private boolean isSubtypeOfThrowable (ITypeBinding type)
	{
		this.typesStack = new Stack <ITypeBinding> ();
		
		return isSubtypeOfThrowableR (type);
	}
		
	
	private boolean isSubtypeOfThrowableR (ITypeBinding type)
	{
		if (type.isClass() == false)
		{
			return false;
		}
		else
		{
			if ( type.getQualifiedName().equals("java.lang.Throwable") )
			{
				this.typesStack.push(type); // add Throwable
				this.typesStack.push(type.getSuperclass()); // add Object
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
					this.typesStack.push(type);
					return isSubtypeOfThrowableR(type.getSuperclass());
				}
			}
		}
	}
	
	

}


