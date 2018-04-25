package ufrn.dimap.lets.exceptionalinterface;

import java.util.Iterator;
import java.util.TreeSet;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;


/* 
 * A interface excepcional de um bloco (m�todo ou bloco try) �: interface
 * excepcional de cada chamada UNI�O interface excepcional de cada bloco interno a ele.
 * s
 * A interface excepcional de um bloco try �: interface excepcional das suas chamadas COMPLEMENTO
 * exce��es capturadas nos catches UNI�O exce��es lan�adas nos catches.
 * */



/**
 * EIVisitor calcula a interface excepcional de m�todos e blocos try.
 * 
 * O c�lculo da interface excepcional � sequencial. Cada chamada de m�todo contribui com a interface
 * excepcional. Se este m�dulo for um bloco try-catch, os 
 * blocos catch ao final s�o usados para remover exce��es da interface excepcional, bem como
 * inserir novas.
 * */
public class EIVisitor extends ASTVisitor
{	
	private MethodNode caller;
	private ASTNode parent;
	private ExceptionalInterface exceptionalInterface;
	
	public EIVisitor ( MethodNode caller, ASTNode parentNode )
	{
		this.caller = caller;
		this.parent = parentNode;
		this.exceptionalInterface = new ExceptionalInterface();
	}
	
	@Override
	public boolean visit ( TryStatement tryStatement )
	{
		// Entraria em loop em blocos try aninhados
		if ( tryStatement != parent )
		{
			EIVisitor visitor = new EIVisitor(caller, tryStatement);
			tryStatement.accept(visitor);
			
			this.exceptionalInterface.addSignalers(visitor.getExceptionalInterface().getSignalers());
			
			return false;
		}
		else
		{
			return true;
		}
	}	
	
	@Override
	public boolean visit ( CatchClause catchClause )
	{
		ITypeBinding caughtExceptionType = catchClause.getException().getType().resolveBinding();
			
		Iterator<Signaler> signalerIte = this.exceptionalInterface.getSignalers().iterator();
		while (signalerIte.hasNext())
		{
			Signaler signaler = signalerIte.next();
			
			if (EIType.isSubtype (signaler.getType(), caughtExceptionType))
			{
				signalerIte.remove();
			}
		}
		
		return true;
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
		
		if ( methodBinding.getJavaElement() != null )
		{
			processInvocation (methodBinding);
		
			return true;
		}
		else
		{
			// TODO usar logger
			System.out.println("WARNING: M�todo sem JavaElement: " + instanceCreation);
			return false;
		}
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
		
		CompilationUnit cu = (CompilationUnit) throwNode.getRoot();
		
		Signaler signaler = new Signaler(new EIType(exceptionType), cu.getJavaElement().getHandleIdentifier(), throwNode.getStartPosition(), throwNode.getLength());
		
		this.exceptionalInterface.addSignaler(signaler);

		return true;
	}
	
	private void processInvocation ( IMethodBinding methodBinding  )
	{
		MethodNode callee = findOnChildren(methodBinding);
		
		if ( callee != null )
		{	
			TreeSet<Signaler> signalers  = callee.getExceptionalInterface().getSignalers();
			
			this.exceptionalInterface.addSignalers( signalers );
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

	public ExceptionalInterface getExceptionalInterface() {
		return this.exceptionalInterface;
	}
}
