package ufrn.dimap.lets.metric.visitor.exceptionalinterface;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.internal.core.BinaryMethod;
import org.eclipse.jdt.internal.core.SourceMethod;
import org.eclipse.jdt.internal.corext.callhierarchy.CallHierarchyVisitor;
import org.eclipse.jdt.internal.corext.callhierarchy.MethodWrapper;

import ufrn.dimap.lets.metric.handlers.HandlerUtil;
import ufrn.dimap.lets.metric.model.exceptionalinterface.Method;

public class CalleeHierarchyVisitor extends CallHierarchyVisitor
{
	//Nota: Existem 3 estados para um método, que são controlados pelos dois maps.
	// O método que está no map "methods" está finalizado. O seu valor em "methodsCalls" não importa.
	// O valor em "methodsCalls" se refere às chamadas àquele metodo. É como uma pilha de chamadas. No caso, somente no postVisit da primeira chamada (quando o valor do map é 0), deve ser processada. Essa solução é necessária porque a implementação do visitor não elimita automaticamente chamadas recursivas.
	private Map<String, Integer> methodsCalls;
	public Map<String, Method> methods;

	private static int tabs;

	public CalleeHierarchyVisitor ()
	{
		methods = new HashMap<>();
		methodsCalls = new HashMap<>();
		tabs = -1;
	}


	//Nota: Todo método visitado vai ser pós visitado. Por isso no início dos dois métodos deve ser testado se o método já foi visitado.
	public boolean visit (MethodWrapper methodWrapper)
	{
		/*
		tabs++;
		if ( methodWrapper.getMember().getElementType() != IJavaElement.METHOD )
		{
		CalleeHierarchyVisitor.println( methodWrapper.getMember().getElementType() + " - " + methodWrapper.getMember().getHandleIdentifier() );
		}
		
		
		return true;
		*/
		
		tabs++;

		String methodIdentifier = methodWrapper.getMember().getHandleIdentifier();		
		Method method = methods.get(methodIdentifier);
		Integer status = methodsCalls.get(methodIdentifier);
		
		if ( methodWrapper.getMember().getElementType() != IJavaElement.METHOD )
		{
			return true;
		}
		else if ( method != null )
		{
			CalleeHierarchyVisitor.println(methodIdentifier + " FINALIZADO");
			return false;
		}
		else if ( status == null )
		{
			CalleeHierarchyVisitor.println(methodIdentifier + " NÃO DESCOBERTO");
			methodsCalls.put(methodIdentifier, 0);
			return true;
		}
		else // method == null && status != null
		{
			CalleeHierarchyVisitor.println(methodIdentifier + " DESCOBERTO");
			methodsCalls.put(methodIdentifier, status+1);
			return false;
		}
		
		/*
		System.out.print("getMethodCall - ");
		System.out.println(methodWrapper.getMethodCall());

		System.out.print("getName - ");
		System.out.println(methodWrapper.getName());

		System.out.print("getMember - ");
		System.out.println(methodWrapper.getMember());

		System.out.print("getParent - ");
		System.out.println(methodWrapper.getParent());

		System.out.print("getMember.getHandleIdentifier - ");
		System.out.println(methodWrapper.getMember().getHandleIdentifier());

		System.out.println();

		return true;
		 */
	}

	@SuppressWarnings("restriction")
	public void postVisit (MethodWrapper callerWrapper)
	{
		String methodIdentifier = callerWrapper.getMember().getHandleIdentifier();		
		Method method = methods.get(methodIdentifier);
		Integer status = methodsCalls.get(methodIdentifier);
		
		CalleeHierarchyVisitor.println(methodIdentifier + " PROCESSANDO");

		if ( callerWrapper.getMember().getElementType() != IJavaElement.METHOD )
		{
			return;
		}
		else if ( method == null && status == 0 )
		{
			IMethod caller;
			List<IMethod> callees = new ArrayList<>();
			
			caller = (IMethod) callerWrapper.getMember();
			
			for ( MethodWrapper calleeWrapper : callerWrapper.getCalls(new NullProgressMonitor()) )
			{
				if (calleeWrapper.getMember().getElementType() == IJavaElement.METHOD)
				{
					callees.add((IMethod) calleeWrapper.getMember());
				}
			}
			
			try
			{
				createNewMethod (caller, callees);
			}
			catch (JavaModelException e)
			{
				throw new RuntimeException (e);
			}
		}
		else
		{
			this.methodsCalls.put(methodIdentifier, status - 1);
		}
		
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
				if ( !caller.equals(callee) )
				{
					Method calleeMethod = methods.get(callee.getHandleIdentifier());
					try
					{
					newMethod.addRethrownTypes(calleeMethod.getThrownTypes());
					}
					catch (RuntimeException e)
					{
						e.printStackTrace();
					}
					newMethod.addRethrownTypes(calleeMethod.getRethrownTypes());
				}
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
		String [] rawExceptions = method.getExceptionTypes();
		List<String> exceptions = new ArrayList<>();

		for ( String exception : exceptions )
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


	private static void println (String text)
	{
		for ( int i = 0 ; i < tabs ; i++ )
		{
			System.out.print("\t");
		}

		System.out.println(text);
	}
}

