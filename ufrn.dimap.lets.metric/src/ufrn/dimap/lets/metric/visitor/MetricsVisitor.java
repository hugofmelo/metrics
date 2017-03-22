package ufrn.dimap.lets.metric.visitor;

import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;

import ufrn.dimap.lets.metric.model.CatchEntry;
import ufrn.dimap.lets.metric.model.FinallyEntry;
import ufrn.dimap.lets.metric.model.MetricsModel;
import ufrn.dimap.lets.metric.model.SignalerEntry;
import ufrn.dimap.lets.metric.model.TryEntry;

public class MetricsVisitor extends ASTVisitor
{
	private Stack <String> catchedVariables;
	
	//private CompilationUnit compilationUnit;
	public MetricsModel model;
	
	public MetricsVisitor (MetricsModel model)
	{
		this.model = model;
		
		this.catchedVariables = new Stack <String> ();
	}
	
	public boolean visit (ThrowStatement throwNode)
	{
		// Cria uma nova entrada de signaler
		SignalerEntry signalerEntry = new SignalerEntry(throwNode);
		
		// Adiciona entrada no modelo
		this.model.addSignalerEntry(signalerEntry);
		

		// VERIFICAR QUAL DOS 3 TIPOS DE SIGNALER ESTE SE ENCAIXA

		// Rethrow
		// Para ser rethrow, a expressão precisa ser um SimpleName. Algo como "throw e"...
		Expression throwExpression = throwNode.getExpression();
		if ( throwExpression instanceof SimpleName )
		{
			// e a pilha de variáveis capturadas não pode ser vazia.
			if ( this.catchedVariables.isEmpty() == false )
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
		
		// Wrapping
		// Não sendo um rethrow, pode ser um wrapping. O formato comum de expressões wrapping são ClassInstanceCreation. Algo como "throw new (...)" 
		else if ( throwExpression instanceof ClassInstanceCreation )
		{			
			// verificar se a nova exceção está encapsulando uma exceção capturada anteriormente, configurando o wrapping...
			List<Expression> arguments = ((ClassInstanceCreation)throwExpression).arguments();
			
			boolean wrappingConfirmed = false; 	// Variavel para controle do loop 
			Expression argument;
			// Para cada argumento da instaciação
			for ( Iterator<Expression> argumentIte = arguments.iterator() ; wrappingConfirmed == false && argumentIte.hasNext() ; )
			{
				argument = argumentIte.next();
				if ( argument instanceof SimpleName )
				{
					if ( this.catchedVariables.isEmpty() == false )
					{
						String catchedVariable;
						// Para cada variável na pilha
						for ( Iterator<String> stackIte = this.catchedVariables.iterator() ; wrappingConfirmed == false && stackIte.hasNext() ;  )
						{
							catchedVariable = stackIte.next();
							if ( ((SimpleName)throwExpression).getIdentifier().equals(catchedVariable) )
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
		
		return true;
	}
	
	public boolean visit (TryStatement tryNode)
	{
		
		TryEntry tryEntry = new TryEntry (tryNode);
		this.model.addTryEntry(tryEntry);
		
		if (tryNode.getFinally() != null)
		{
			FinallyEntry finallyEntry = new FinallyEntry (tryNode.getFinally());
			this.model.addFinallyEntry(finallyEntry);
		}
		
		return true;
	}
	
	public boolean visit (CatchClause catchNode)
	{
		String catchedVariable;
		
		catchedVariable = catchNode.getException().getName().getIdentifier();
		catchedVariables.push(catchedVariable);
		
		
		CatchEntry catchEntry = new CatchEntry (catchNode);
		this.model.addCatchEntry (catchEntry);
		
		return true;
	}
	
	public void endVisit (CatchClause catchClauseNode)
	{
		this.catchedVariables.pop();
		
	}

}


