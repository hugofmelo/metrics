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
 * A partir de uma CatchClause, parsear o c�digo e verifica se a exce��o capturada � re-sinalizada, encapsulada ou somente tratada.
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
		// Para ser rethrow, a express�o precisa ser um SimpleName. Algo como "throw e"...
		if ( throwExpression instanceof SimpleName )
		{
			// O nome da exce��o lan�ada deve ser o mesmo da exce��o que foi capturada.
			if ( this.caughtExceptionName.toString().equals(throwExpression.toString()) )
			{
				this.isRethrown = true;
			}
			// Sendo um SimpleName e n�o sendo rethrow, esta sinaliza��o n�o est� em um formato comum
			// Ser� considerado que a exce��o capturada teve seu fluxo encerrado, e o throw interno inicia um novo fluxo.
			else
			{
				//throw new UncommonSignalerPatternException ("Padr�o incomum de sinalizador. � sinalizado um SimpleName e a pilha de vari�veis capturadas est� vazia.", throwNode);
			}
		}		
		// Wrapping
		// N�o sendo um rethrow, pode ser um wrapping. O formato comum de express�es wrapping s�o ClassInstanceCreation. Algo como "throw new (...)" 
		else if ( throwExpression instanceof ClassInstanceCreation )
		{			
			// verificar se a nova exce��o est� encapsulando uma exce��o capturada anteriormente, configurando o wrapping...
			@SuppressWarnings("unchecked")
			List<Expression> arguments = ((ClassInstanceCreation)throwExpression).arguments();
			
			// Para cada argumento da instacia��o
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
		// � uma sinaliza��o atipica 
		{
//			signalerEntry.regularPattern = false;
//			throw new UncommonSignalerPatternException ("Padr�o incomum de sinalizador. N�o � sinalizado nem um SimpleName e nem um ClassInstanceCreation.", throwNode);
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
