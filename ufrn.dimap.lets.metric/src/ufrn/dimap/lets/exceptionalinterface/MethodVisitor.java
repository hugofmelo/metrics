package ufrn.dimap.lets.exceptionalinterface;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;


/*
 * Para cada throw ou chamada (MethodInvocation, ClassInstanceCreation, SuperMethodInvocation ou SuperConstructorInvocation),
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
	private Block tryBlock;
	private CatchClause catchClause;
	
	public MethodVisitor (MethodNode caller)
	{
		this.caller = caller;
		this.tryBlock = null;
		this.catchClause = null;
	}
	
	@Override
	public boolean visit ( TryStatement tryStatement )
	{
		this.tryBlock = tryStatement.getBody();
		return true;
	}

	@Override
	public void endVisit ( TryStatement tryStatement )
	{
		this.tryBlock = null;
	}
	
	@Override
	public boolean visit ( CatchClause catchClause )
	{
		this.tryBlock = null;
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
		
		ExceptionEvaluator.evaluate(tryStatement, exceptionType, false, this.caller);
	
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
					ExceptionEvaluator.evaluate(tryStatement, exception, true, caller);
				}
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
}
