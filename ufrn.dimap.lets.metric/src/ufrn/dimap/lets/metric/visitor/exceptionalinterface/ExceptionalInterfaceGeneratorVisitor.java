package ufrn.dimap.lets.metric.visitor.exceptionalinterface;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.CompilationUnit;
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

		ExceptionalInterfaceGeneratorVisitor.println(method.getHandleIdentifier() + " DESCOBERTO");
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

		ExceptionalInterfaceGeneratorVisitor.println(methodIdentifier + " FINALIZADO");
		
		tabs--;
	}


	private void createNewMethod(IMethod caller, List<IMethod> callees) throws JavaModelException
	{
		// Nova instancia de Method que vai ser salva
		Method newMethod = new Method(caller.getHandleIdentifier());

		if ( isParseable (caller) )
		{
			CompilationUnit compilationUnit = HandlerUtil.parse(caller);

			MethodVisitor methodVisitor = new MethodVisitor(caller);
			compilationUnit.accept(methodVisitor);

			newMethod.addThrownTypes(methodVisitor.thrownTypes);

			// Adicionar a interface excepcional dos métodos chamados no método atual
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

	private List<String> getDeclaredException(IMethod method) throws JavaModelException
	{
		List<String> exceptions = new ArrayList<>();

		for ( String exception : method.getExceptionTypes() )
		{
			exceptions.add(exception.substring(1, exception.length() - 1));
		}

		return exceptions;
	}


	// O método é parseable se possui um CompilationUnit || possui um ClassFile com código-fonte associado
	private boolean isParseable(IMethod method) throws JavaModelException
	{			
		if ( method.getCompilationUnit() != null )
		{
			return true;
		}
		else if ( method.getClassFile() != null && method.getClassFile().getSource() != null )
		{
			return true;
		}
		else
		{
			return false;
		}
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
