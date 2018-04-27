package ufrn.dimap.lets.exceptionexpert.model;

import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.ITypeBinding;

public class EIType
{
	private ITypeBinding typeBinding;
	private IType type;
	
	public EIType ( ITypeBinding binding )
	{
		this.typeBinding = binding;
		this.type = null;
	}
	
	public EIType ( IType type )
	{
		this.typeBinding = null;
		this.type = type;
	}
	
	public String getQualifiedName ()
	{
		if ( this.typeBinding != null )
		{
			return this.typeBinding.getQualifiedName();
		}
		else
		{
			return this.type.getFullyQualifiedName();
		}
	}
	
	@Override
	public boolean equals (Object other)
	{
		if ( this == other )
		{
			return true;
		}
		else if ( other instanceof EIType )
		{
			String thisName = this.getQualifiedName();
			String otherName = ((EIType)other).getQualifiedName();
			EIType casted = (EIType) other;
			return this.getQualifiedName().equals(casted.getQualifiedName());
		}
		else
		{
			return false;
		}
	}
	
	@Override
	public int hashCode ()
	{
		return this.getQualifiedName().hashCode();
	}
	
	/**
	 * Verifica se subType é do mesmo tipo ou subtipo de superType.
	 * @param subType
	 * @param superType
	 * @return
	 */
	public static boolean isSubtype(EIType subType, ITypeBinding superType)
	{
		ITypeBinding type = subType.typeBinding;
		
		while ( type != null )
		{
			if ( type.getQualifiedName().equals(superType.getQualifiedName()) )
			{
				return true;
			}
			else
			{
				type = type.getSuperclass();
			}
		}
		
		return false;
	}
	
	public String toString ()
	{
		return this.getQualifiedName();
	}
}
