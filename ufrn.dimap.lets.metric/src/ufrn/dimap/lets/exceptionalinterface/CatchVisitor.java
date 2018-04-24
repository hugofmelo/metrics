package ufrn.dimap.lets.exceptionalinterface;

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


/**
 * O CatchVisitor processa as chamadas dentro de um catch e verifica se existem cadeias de exceções.
 * 
 * O CatchVisitor deve ser chamado a partir de uma CatchClause.
 * */
public class CatchVisitor extends ASTVisitor
{	
	public CatchVisitor ()
	{
		
	}
	
	@Override
	public boolean visit ( CatchClause catchClause )
	{
		return true;
	}
	
	@Override
	public void endVisit ( CatchClause catchClause )
	{
		this.process(catchClause);
	}

	private void process( CatchClause catchClause )
	{
		
	}
	
}
