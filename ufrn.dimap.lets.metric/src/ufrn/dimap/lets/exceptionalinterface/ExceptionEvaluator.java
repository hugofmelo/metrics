package ufrn.dimap.lets.exceptionalinterface;

import java.util.List;

import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.TryStatement;


public class ExceptionEvaluator
{
	public static void evaluate(TryStatement tryStatement, ITypeBinding signaledExceptionType, boolean signalerIsCall, MethodNode caller)
	{
		if ( tryStatement != null )
		{
			boolean caught = false;
			
			@SuppressWarnings("unchecked")
			List<CatchClause> catches = tryStatement.catchClauses();
			for ( CatchClause catchClause : catches )
			{
				ITypeBinding caughtExceptionType = catchClause.getException().getType().resolveBinding();
				SimpleName caughtExceptionName = catchClause.getException().getName();
				if (isSubtype (signaledExceptionType, caughtExceptionType))
				{
					caught = true;
					
					ThrowVisitor throwVisitor = new ThrowVisitor (caughtExceptionName);
					catchClause.accept(throwVisitor);
					
					if ( throwVisitor.isWrapped() )
					{
						caller.addWrapped(throwVisitor.getWrapperExceptionType(), signaledExceptionType);
					}
					else if ( throwVisitor.isRethrown() )
					{
						caller.addRethrown(signaledExceptionType);
					}
					else
					{
						if ( signalerIsCall )
						{
							caller.addCaught (signaledExceptionType, caughtExceptionType);
						}
						else // throw
						{
						}
					}
				}
			}
			
			if ( !caught )
			{
				if ( signalerIsCall )
				{
					caller.addPropagated(signaledExceptionType);
				}
				else
				{
					caller.addThrown(signaledExceptionType);
				}
			}
		}
		else
		{
			// Exception not caught on handlers
			if ( signalerIsCall )
			{
				caller.addPropagated(signaledExceptionType);
			}
			else
			{
				caller.addThrown(signaledExceptionType);
			}
		}
	}
	
	/**
	 * Verifica se subType é do mesmo tipo ou subtipo de superType.
	 * @param subType
	 * @param superType
	 * @return
	 */
	private static boolean isSubtype(ITypeBinding subType, ITypeBinding superType)
	{
		ITypeBinding type = subType;
		
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

	

	
}
