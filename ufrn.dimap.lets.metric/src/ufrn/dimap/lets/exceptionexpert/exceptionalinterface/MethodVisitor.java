package ufrn.dimap.lets.exceptionexpert.exceptionalinterface;

import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTVisitor;
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

import ufrn.dimap.lets.exceptionexpert.model.EIType;
import ufrn.dimap.lets.exceptionexpert.model.MethodNode;


/* Para cada throw ou chamada (MethodInvocation, ClassInstanceCreation, SuperMethodInvocation ou SuperConstructorInvocation),
 * verifica se há exceções externas.
 * 		Sim
 * 			Marca com !
 * 			Para cada tipo excepcional
 * 				(1) Está no try?
 * 					sim
 * 						verifica se possui um catch para esse tipo
 * 							sim					
 * 								verifica se a exceção é re-sinalizada
 * 									addRethrow
 * 								verifica se a exceçaõ é encapsulada
 * 									addWrapped
 * 								a exceção foi capturada
 * 									verifica se a primeira expressão era throw ou call
 * 										call
 * 											addCaught
 * 										throw
 * 											nada a fazer
 *							não
 *								goto 2
 * 					não	
 * 						(2) verifica se primeira expressão era throw ou call
 * 							throw
 * 								addThrow
 * 							call
 * 								addPropagated					
 *		Não
 *			Nada a fazer
 * */



/**
 * O MethodVisitor deve ser chamado a partir de um MethodDeclaration.
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
			evaluate(new EIType(exceptionType), false);
		}
	
		return true;
	}
	
	private void processInvocation ( IMethodBinding methodBinding  )
	{
		MethodNode callee = findOnChildren(methodBinding);
		
		if ( callee != null )
		{	
			Set<EIType> exceptions = callee.getExceptionalInterface().getExternalExceptionTypes();
			
			if ( !exceptions.isEmpty() )
			{
				// TODO Anotar a chamada, como a "!" de swift
				
				for ( EIType exception : exceptions )
				{
					evaluate(exception, true);
				}
			}
		}
	}
	
	public void evaluate(EIType signaledExceptionType, boolean signalerIsCall)
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
				if (EIType.isSubtype (signaledExceptionType, caughtExceptionType))
				{
					caught = true;
					
					ThrowVisitor throwVisitor = new ThrowVisitor (caughtExceptionName);
					catchClause.accept(throwVisitor);
					
					if ( throwVisitor.isWrapped() )
					{
						//caller.getExceptionalInterface().addWrapped(new EIType(throwVisitor.getWrapperExceptionType()), signaledExceptionType);
					}
					else if ( throwVisitor.isRethrown() )
					{
						//caller.getExceptionalInterface().addRethrown(signaledExceptionType);
					}
					else
					{
						if ( signalerIsCall )
						{
							//caller.getExceptionalInterface().addCaught (signaledExceptionType, new EIType (caughtExceptionType));
						}
						else // throw
						{
							// A exceção foi lançada e capturada intra método. Não afeta a interface excepcional.
						}
					}
				}
			}
			
			if ( !caught )
			{
				if ( signalerIsCall )
				{
					//caller.getExceptionalInterface().addPropagated(signaledExceptionType);
				}
				else
				{
					//caller.getExceptionalInterface().addThrown(signaledExceptionType);
				}
			}
		}
		else
		// Sinalização não ocorre no escopo de um try. Ou é um novo fluxo, ou é propagada
		{
			if ( signalerIsCall )
			{
				//caller.getExceptionalInterface().addPropagated(signaledExceptionType);
			}
			else
			{
				//caller.getExceptionalInterface().addThrown(signaledExceptionType);
			}
		}
	}
	
	/**
	 * Retorna o MethodNode do callgraph que corresponde ao MethodBinding.
	 * 
	 * A priori, todos os MethodBinding devem ser encontrados. Se ele não existe, é porque este MethodBinding corresponde a uma chamada recursiva (grafo completo) ou já foi visitado anteriormente no callgraph (grafo podado).
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

	
}
