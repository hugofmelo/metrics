package ufrn.dimap.lets.metric.visitor.exceptionalinterface;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.Type;
import org.omg.PortableServer.ThreadPolicyOperations;

import ufrn.dimap.lets.metric.model.SignalerEntry;
import ufrn.dimap.lets.metric.visitor.UncommonSignalerPatternException;

public class MethodVisitor extends ASTVisitor
{
	public Set<String> thrownTypes;
	
	public MethodVisitor ()
	{
		thrownTypes = new HashSet<>();
	}
	
	public boolean visit (ThrowStatement throwNode)
	{	
		Expression throwExpression = throwNode.getExpression();
		
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
}
