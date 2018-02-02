package ufrn.dimap.lets.exceptionalinterface;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import org.eclipse.jdt.core.dom.ASTVisitor;
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



/**
 * O MethodVisitor deve ser chamado a partir de um MethodDeclaration.
 * */
public class MethodVisitor extends ASTVisitor
{	
	private MethodNode caller;
	
	private Stack<TryStatement> tries;
	private Stack<CatchClause> catches;
	
	public Set<String> thrownTypes;
	
	public MethodVisitor (MethodNode caller)
	{
		this.caller = caller;
		
		tries = new Stack<>();
		catches = new Stack<>();
		
		thrownTypes = new HashSet<>();
	}
	
	@Override
	public boolean visit ( TryStatement tryStatement )
	{
		this.tries.push(tryStatement);
		return true;
	}
	
	@Override
	public void endVisit ( TryStatement tryStatement )
	{
		this.tries.pop();
	}
	
	@Override
	public boolean visit ( CatchClause catchClause )
	{
		this.catches.push(catchClause);
		return true;	
	}
	
	@Override
	public void endVisit ( CatchClause catchClause )
	{
		this.catches.pop();
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
	
	private void processInvocation ( IMethodBinding methodBinding  )
	{
//		MethodNode callee = findOnChildren(methodBinding);
//		
//		if ( callee != null )
//		{	
//			Set<String> exceptions = callee.getExternalExceptions();
//			
//			if ( !exceptions.isEmpty() )
//			{
//				// Anotar a chamada, como a "!" de swift
//				
//				// verificar se está num try
//				if ( !this.tries.isEmpty() )
//				{
//					
//				}
//			}
//		}
	}
	
	@Override
	public boolean visit (ThrowStatement throwNode)
	{	
		Expression throwExpression = throwNode.getExpression();
		
		ITypeBinding typeBinding = throwExpression.resolveTypeBinding();
		
		
		/*
		if ( throwExpression instanceof ClassInstanceCreation )
		{			
			ClassInstanceCreation classInstanceCreation = (ClassInstanceCreation) throwExpression;
			ITypeBinding binding = classInstanceCreation.getType().resolveBinding();
			String exceptionName;
			
			if ( binding != null )
			{
				exceptionName = classInstanceCreation.getType().resolveBinding().getQualifiedName();
			}
			else
			{
				exceptionName = classInstanceCreation.getType().toString();
			}
			
			thrownTypes.add(exceptionName);
		}
		*/
		
		// VERIFICAR QUAL DOS 3 TIPOS DE SIGNALER ESTE SE ENCAIXA

		// Rethrow
		// Para ser rethrow, a expressão precisa ser um SimpleName. Algo como "throw e"...
		/*
		Expression throwExpression = throwNode.getExpression();
		if ( throwExpression instanceof SimpleName )
		{
			// e a pilha de variáveis capturadas não pode ser vazia.
			if ( !this.catchedVariables.isEmpty() )
			{
				boolean endStackLoop = false;
				String catchedVariable;
				// Para cada variável na pilha
				for ( Iterator<String> ite = this.catchedVariables.iterator() ; endStackLoop == false && ite.hasNext() ;  )
				{
					catchedVariable = ite.next();
					if ( ((SimpleName)throwExpression).getIdentifier().equals(catchedVariable) )
					{
						endStackLoop = true;
						signalerEntry.regularPattern = true;
						signalerEntry.rethrow = true;
						this.model.incrementRethrows();
					}
				}
			}
			// Sendo um SimpleName e não sendo rethrow, esta sinalização não está em um formato comum
			else
			{
				signalerEntry.regularPattern = false;
				throw new UncommonSignalerPatternException ("Padrão incomum de sinalizador. É sinalizado um SimpleName e a pilha de variáveis capturadas está vazia.", throwNode);
			}
		}		
		*/
		/*
		// Wrapping
		// Não sendo um rethrow, pode ser um wrapping. O formato comum de expressões wrapping são ClassInstanceCreation. Algo como "throw new (...)" 
		else if ( throwExpression instanceof ClassInstanceCreation )
		{			
			// verificar se a nova exceção está encapsulando uma exceção capturada anteriormente, configurando o wrapping...
			List<Expression> arguments = ((ClassInstanceCreation)throwExpression).arguments();
			
			boolean wrappingConfirmed = false; 	// Variavel para controle do loop 
			
			// Para cada argumento da instaciação
			for ( Iterator<Expression> argumentIte = arguments.iterator() ; wrappingConfirmed == false && argumentIte.hasNext() ; )
			{
				Expression argument = argumentIte.next();
				if ( argument instanceof SimpleName )
				{
					if ( this.catchedVariables.isEmpty() == false )
					{
						String catchedVariable;
						// Para cada variável na pilha
						for ( Iterator<String> stackIte = this.catchedVariables.iterator() ; wrappingConfirmed == false && stackIte.hasNext() ;  )
						{
							catchedVariable = stackIte.next();
							if ( ((SimpleName)argument).getIdentifier().equals(catchedVariable) )
							{
								wrappingConfirmed = true;
								
								signalerEntry.regularPattern = true;
								signalerEntry.wrapping = true;
								this.model.incrementWrappings();
							}
						}
					}
				}
			}
			
			// Se não for um wrapping, é uma sinalização normal no formato "throw new Exception(...);"
			if ( wrappingConfirmed == true )
			{
				signalerEntry.regularPattern = true;
				signalerEntry.wrapping = true;
				this.model.incrementWrappings();
			}
			else
			{
				signalerEntry.regularPattern = true;
			}
		}
		else
		{
			signalerEntry.regularPattern = false;
			throw new UncommonSignalerPatternException ("Padrão incomum de sinalizador. Não é sinalizado nem um SimpleName e nem um ClassInstanceCreation.", throwNode);
		}
		*/
		return true;
	}
	
	/**
	 * Retorna o MethodNode do callgraph que corresponde ao MethodBinding.
	 * 
	 * A priori, todo MethodBinding deve ser encontrado. Se ele não existe, é porque este MethodBinding corresponde a uma chamada recursiva (grafo completo) ou já foi visitado anteriormente no callgraph (grafo podado).
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
