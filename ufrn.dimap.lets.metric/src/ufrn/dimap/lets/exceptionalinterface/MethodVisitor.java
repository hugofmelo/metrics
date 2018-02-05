package ufrn.dimap.lets.exceptionalinterface;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;


/* Para cada throw ou chamada (MethodInvocation, ClassInstanceCreation, SuperMethodInvocation ou SuperConstructorInvocation),
 * verifica se h� exce��es externas.
 * 		Sim
 * 			Marca com !
 * 			Para cada tipo excepcional
 * 				(1) Est� no try?
 * 					sim
 * 						verifica se possui um catch para esse tipo
 * 							sim					
 * 								verifica se a exce��o � re-sinalizada
 * 									addRethrow
 * 								verifica se a exce�a� � encapsulada
 * 									addWrapped
 * 								a exce��o foi capturada
 * 									verifica se a primeira express�o era throw ou call
 * 										call
 * 											addCaught
 * 										throw
 * 											nada a fazer
 *							n�o
 *								goto 2
 * 					n�o	
 * 						(2) verifica se primeira express�o era throw ou call
 * 							throw
 * 								addThrow
 * 							call
 * 								addPropagated					
 *		N�o
 *			Nada a fazer
 * */



/**
 * O MethodVisitor deve ser chamado a partir de um MethodDeclaration.
 * Pr�-condi��o: o m�todo parseado n�o possui tries aninhados.
 * */
public class MethodVisitor extends ASTVisitor
{	
	private MethodNode caller;
	private TryStatement tryStatement;
	private CatchClause catchClause;
	
	public MethodVisitor (MethodNode caller)
	{
		this.caller = caller;
		this.tryStatement = null;
		this.catchClause = null;
	}
	
	@Override
	public boolean visit ( TryStatement tryStatement )
	{
		this.tryStatement = tryStatement;
		return true;
	}
	
	@Override
	public void endVisit ( TryStatement tryStatement )
	{
		this.tryStatement = null;
	}

	@Override
	public boolean visit ( CatchClause catchClause )
	{
		this.catchClause = catchClause;
		return true;
	}
	
	@Override
	public void endVisit ( CatchClause catchClause )
	{
		this.catchClause = null;
	}	
	
	@Override
	public boolean visit ( MethodInvocation methodInvocation )
	{
		IMethodBinding methodBinding = methodInvocation.resolveMethodBinding();
		
		processInvocation (methodBinding);
		
		return true;
	}
	
	@Override
	public boolean visit ( ClassInstanceCreation instanceCreation )
	{
		IMethodBinding methodBinding = instanceCreation.resolveConstructorBinding();
		
		processInvocation (methodBinding);
		
		return true;
	}
	
	@Override
	public boolean visit ( SuperMethodInvocation methodInvocation )
	{
		IMethodBinding methodBinding = methodInvocation.resolveMethodBinding();
		
		processInvocation (methodBinding);
		
		return true;
	}
	
	@Override
	public boolean visit ( SuperConstructorInvocation instanceCreation )
	{
		IMethodBinding methodBinding = instanceCreation.resolveConstructorBinding();
		
		processInvocation (methodBinding);
		
		return true;
	}
	
	@Override
	public boolean visit (ThrowStatement throwNode)
	{	
		Expression throwExpression = throwNode.getExpression();
		ITypeBinding exceptionType = throwExpression.resolveTypeBinding();
		
		if (!onCatchBlock())
		{
			evaluate(exceptionType, false);
		}
	
		return true;
	}
	
	private void processInvocation ( IMethodBinding methodBinding  )
	{
		MethodNode callee = findOnChildren(methodBinding);
		
		if ( callee != null )
		{	
			Set<ITypeBinding> exceptions = callee.getExternalExceptions();
			
			if ( !exceptions.isEmpty() )
			{
				// TODO Anotar a chamada, como a "!" de swift
				
				for ( ITypeBinding exception : exceptions )
				{
					evaluate(exception, true);
				}
			}
		}
	}
	
	public void evaluate(ITypeBinding signaledExceptionType, boolean signalerIsCall)
	{
		if ( onTryBody() )
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
							// A exce��o foi lan�ada e capturada intra m�todo. N�o afeta a interface excepcional.
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
		// Sinaliza��o n�o ocorre no escopo de um try. Ou � um novo fluxo, ou � propagada
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
	
	/**
	 * Retorna o MethodNode do callgraph que corresponde ao MethodBinding.
	 * 
	 * A priori, todos os MethodBinding devem ser encontrados. Se ele n�o existe, � porque este MethodBinding corresponde a uma chamada recursiva (grafo completo) ou j� foi visitado anteriormente no callgraph (grafo podado).
	 * 
	 * @param methodBinding
	 * @return 
	 */
	private MethodNode findOnChildren ( IMethodBinding methodBinding )
	{		
		for ( MethodNode node : this.caller.getChildren() )
		{
			if ( methodBinding.getJavaElement().getHandleIdentifier().equals(node.getIdentifier()) )
			{
				return node;
			}
		}
		
		return null;
	}

	private boolean onTryBody()
	{
		return this.tryStatement != null && this.catchClause == null;
	}

	private boolean onCatchBlock()
	{
		return this.catchClause != null;
	}

	/**
	 * Verifica se subType � do mesmo tipo ou subtipo de superType.
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
