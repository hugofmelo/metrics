package ufrn.dimap.lets.exceptionalinterface;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.Type;

/*
 * Para cada throw ou chamada (MethodInvocation, ClassInstanceCreation, SuperMethodInvocation ou SuperConstructorInvocation),
 * verifica se há exceções externas.
 * 		Sim
 * 			Marca com !
 * 			Para cada tipo excepcional
 * 				(1) Está num try?
 * 					sim
 * 						verifica se possui um catch para esse tipo
 * 							sim					
 * 								verifica se a exceção é re-sinalizada
 * 									marca como re-sinalizada
 * 									goto 1
 * 								verifica se a exceçaõ é encapsulada
 * 									marca como encapsulada
 * 									marca o novo tipo
 * 									goto 1
 * 								addCaught (last type)
 *							não
 *								goto 2
 * 					não	
 * 						(2) verifica se primeira expressão era throw ou call
 * 							throw
 * 								addThrow (last type)
 * 							call
 * 								se rethrow == true
 * 									addrethrow (last type)
 * 								se wraps == true
 * 									addwraps (last type)
 * 								senão
 * 									add propagates (last type)					
 *		Não
 *			Nada a fazer
 * */

public class ExceptionEvaluator
{
	private MethodNode caller;
	private ITypeBinding originalException;
	private ITypeBinding lastException;
	private boolean signalerIsCall;
	private boolean rethrow;
	private boolean wrapping;
	
	public ExceptionEvaluator (MethodNode caller, ITypeBinding exception, TryStatement tryStatement, boolean signalerIsCall)
	{
		this.caller = caller;
		this.signalerIsCall = signalerIsCall;
		this.rethrow = false;
		this.wrapping = false;
		this.originalException = exception;
		this.lastException = exception;
		
		evaluate (tryStatement);
	}
	
	private void evaluate ( TryStatement tryStatement )
	{
		if ( tryStatement != null )
		{
			evaluateTryBlock(tryStatement);
		}
		else
		{
			computeResult ();
		}
	}
	
	private void evaluateTryBlock(TryStatement tryStatement)
	{
		List<CatchClause> catches = tryStatement.catchClauses();
		for ( CatchClause catchClause : catches )
		{
			ITypeBinding catchType = catchClause.getException().getType().resolveBinding();
			if (isSubtype (this.lastException, catchType))
			{
				ThrowVisitor throwVisitor = new ThrowVisitor ();
				catchClause.accept(throwVisitor);
				
				if ( throwVisitor.isWrapped() )
				{
					this.wrapping = true;
					this.lastException = throwVisitor.getThrowType();
					this.evaluate(throwVisitor.getContainingTryStatement());
				}
				else if ( throwVisitor.isRethrown() )
				{
					this.rethrow = true;
					this.evaluate(throwVisitor.getContainingTryStatement());
				}
				else
				{
					Set<ITypeBinding> caughtAs = this.caller.getCaught().get(originalException);
					if ( caughtAs == null )
					{
						caughtAs = new HashSet<>();
					}
					
					caughtAs.add(catchType);
					
					this.caller.getCaught().put(originalException, caughtAs);
				}
			}
		}
		
		// Exception not caught
		this.computeResult();
	}
	
	private void computeResult()
	{
		if ( signalerIsCall )
		{
			if ( this.wrapping )
			{
				Set<ITypeBinding> caught = caller.getWrapped().get(lastException); 
				if ( caught == null )
				{
					caught = new HashSet<>();
				}
				
				caught.add(originalException);
				caller.getWrapped().put(this.lastException, caught);
			}
			else if ( this.rethrow )
			{
				caller.getRethrown().add(lastException);
			}
			else
			{
				caller.getPropagated().add(lastException);
			}
		}
		else
		{
			caller.getThrown().add(lastException);
		}
	}
	
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
