package ufrn.dimap.lets.exceptionexpert.exceptionalinterface;

import java.util.Iterator;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.ThrowStatement;

/**
 * A partir de uma CatchClause, parsear o código e verifica se a exceção capturada é re-sinalizada, encapsulada ou somente tratada.
 * @author Hugo
 *
 */
public class ThrowVisitor extends ASTVisitor
{
	private SimpleName caughtExceptionName;
	private ITypeBinding wrapperExceptionType;
	private boolean isWrapped;
	private boolean isRethrown;
	
	public ThrowVisitor (SimpleName caughtExceptionName)
	{
		this.caughtExceptionName = caughtExceptionName;
		this.wrapperExceptionType = null;
		this.isRethrown = false;
		this.isWrapped = false;
	}
	
	@Override
	public boolean visit ( ThrowStatement throwStatement )
	{
		Expression throwExpression = throwStatement.getExpression();
	
		// VERIFICAR QUAL DOS 3 TIPOS DE SIGNALER ESTE SE ENCAIXA

		// Rethrow
		// Para ser rethrow, a expressão precisa ser um SimpleName. Algo como "throw e"...
		if ( throwExpression instanceof SimpleName )
		{
			// O nome da exceção lançada deve ser o mesmo da exceção que foi capturada.
			if ( this.caughtExceptionName.toString().equals(throwExpression.toString()) )
			{
				this.isRethrown = true;
			}
			// Sendo um SimpleName e não sendo rethrow, esta sinalização não está em um formato comum
			// Será considerado que a exceção capturada teve seu fluxo encerrado, e o throw interno inicia um novo fluxo.
			else
			{
				//throw new UncommonSignalerPatternException ("Padrão incomum de sinalizador. É sinalizado um SimpleName e a pilha de variáveis capturadas está vazia.", throwNode);
			}
		}		
		// Wrapping
		// Não sendo um rethrow, pode ser um wrapping. O formato comum de expressões wrapping são ClassInstanceCreation. Algo como "throw new (...)" 
		else if ( throwExpression instanceof ClassInstanceCreation )
		{			
			// verificar se a nova exceção está encapsulando uma exceção capturada anteriormente, configurando o wrapping...
			@SuppressWarnings("unchecked")
			List<Expression> arguments = ((ClassInstanceCreation)throwExpression).arguments();
			
			// Para cada argumento da instaciação
			for ( Iterator<Expression> argumentIte = arguments.iterator() ; this.isWrapped == false && argumentIte.hasNext() ; )
			{
				Expression argument = argumentIte.next();
				if ( argument instanceof SimpleName )
				{
					if ( this.caughtExceptionName.equals(argument) )
					{
						this.isWrapped = true;
						this.wrapperExceptionType = throwExpression.resolveTypeBinding(); 
					}
				}
			}
		}
		else
		// É uma sinalização atipica 
		{
//			signalerEntry.regularPattern = false;
//			throw new UncommonSignalerPatternException ("Padrão incomum de sinalizador. Não é sinalizado nem um SimpleName e nem um ClassInstanceCreation.", throwNode);
		}
		
		return false;
	}

	public ITypeBinding getWrapperExceptionType() {
		return wrapperExceptionType;
	}

	public boolean isWrapped() {
		return isWrapped;
	}

	public boolean isRethrown() {
		return isRethrown;
	}
	
	
}
