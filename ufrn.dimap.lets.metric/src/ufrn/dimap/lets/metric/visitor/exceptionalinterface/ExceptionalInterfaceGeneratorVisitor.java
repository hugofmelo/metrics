package ufrn.dimap.lets.metric.visitor.exceptionalinterface;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.internal.corext.callhierarchy.CallHierarchy;
import org.eclipse.jdt.internal.corext.callhierarchy.MethodWrapper;

import ufrn.dimap.lets.callgraphvisitor.CallgraphVisitor;
import ufrn.dimap.lets.callgraphvisitor.Orientation;
import ufrn.dimap.lets.metric.handlers.HandlerUtil;
import ufrn.dimap.lets.metric.model.exceptionalinterface.Method;

public class ExceptionalInterfaceGeneratorVisitor extends CallgraphVisitor
{
	private Map<String, Method> methods;
	private static int tabs;

	public ExceptionalInterfaceGeneratorVisitor( CallHierarchy callHierarchy )
	{
		super ( callHierarchy, Orientation.CALLEES );

		methods = new HashMap<>();
		tabs = -1;
	}

	@Override
	public void preVisit(IMethod method)
	{
		tabs++;

		//ExceptionalInterfaceGeneratorVisitor.println(method.getHandleIdentifier() + " DESCOBERTO");
	}

	@Override
	public void postVisit(IMethod method)
	{
		String methodIdentifier = method.getHandleIdentifier();		

		ExceptionalInterfaceGeneratorVisitor.println(methodIdentifier + " PROCESSANDO");

		try
		{
			this.createNewMethod (method, this.getChildren(method));
		}
		catch (JavaModelException e)
		{
			throw new ShouldNotHappenException(e);
		}

		//ExceptionalInterfaceGeneratorVisitor.println(methodIdentifier + " FINALIZADO");
		
		tabs--;
	}

	/**
	 * Adiciona o m�todo no modelo.
	 * 
	 * @param	caller	O m�todo que ser� adicionado ao modelo.
	 * @param	callees		Os m�todos chamados pelo m�todo que ser� adicionado ao modelo.
	 * */
	private void createNewMethod(IMethod caller, List<IMethod> callees) throws JavaModelException
	{
		// Nova instancia de Method que vai ser salva
		Method newMethod = new Method(caller.getHandleIdentifier());

		if ( isParseable (caller) )
		{
			CompilationUnit compilationUnit = HandlerUtil.parse(caller);
			MethodDeclaration methodDeclaration = MethodFinder.find ( caller, compilationUnit );
			
			MethodVisitor methodVisitor = new MethodVisitor(methods);
			methodDeclaration.accept(methodVisitor);

			newMethod.addThrownTypes(methodVisitor.thrownTypes);

			// Adicionar a interface excepcional dos m�todos chamados no m�todo atual
			for (IMethod callee : callees)
			{
				Method calleeMethod = methods.get(callee.getHandleIdentifier());
				
				newMethod.addRethrownTypes(calleeMethod.getThrownTypes());
				newMethod.addRethrownTypes(calleeMethod.getRethrownTypes());
			}
		}
		else
		{
			for ( String exception : getDeclaredException(caller) )
			{
				newMethod.addRethrownType( exception );
			}
		}

		this.methods.put(caller.getHandleIdentifier(), newMethod);

	}

	// O IMethod possui um array de strings que representam as exce��es da interface excepcional declarada. As strings est�o num formato diferente. Este m�todo convete o array com nomes estranhos em uma lista de tamanhos normais.
	private List<String> getDeclaredException(IMethod method) throws JavaModelException
	{
		List<String> exceptions = new ArrayList<>();

		for ( String exception : method.getExceptionTypes() )
		{
			exceptions.add(exception.substring(1, exception.length() - 1));
		}

		return exceptions;
	}

	/**
	 * O IMethod � parseable se n�o � nativo e possui c�digo-fonte (� um CompilationUnit ou um ClassFile com c�digo-fonte linkado).
	 * @param	method	O m�todo a ser testado.
	 * */
	private boolean isParseable(IMethod method) throws JavaModelException
	{		
		if ( method.getCompilationUnit() != null ||
			(method.getClassFile() != null && method.getClassFile().getSource() != null))
		{
			if ( !isAbstract (method.getSource()) )
			{
				return true;
			}
			else
			{
				return false;
			}
		}
		else
		{
			return false;
		}
	}
	
	/**
	 * Dado uma string que representa o c�digo-fonte de um m�todo (podendo incluir Javadoc),
	 * indica se este m�todo � abstrato (definido em uma classe abstrata ou em uma interface).
	 * @param	source	c�digo-fonte do m�todo */
	private boolean isAbstract(String source)
	{
		Pattern pattern = Pattern.compile(".*\\{.*\\}$", Pattern.DOTALL);
		Matcher matcher = pattern.matcher(source);
		boolean isAbstract = !matcher.matches();
		
		return isAbstract;
	}

	public Map<String, Method> getMethods()
	{
		return this.methods;
	}
	
	private static void println (String text)
	{
		for ( int i = 0 ; i < tabs ; i++ )
		{
			System.out.print("\t");
		}

		System.out.println(text);
	}
}
