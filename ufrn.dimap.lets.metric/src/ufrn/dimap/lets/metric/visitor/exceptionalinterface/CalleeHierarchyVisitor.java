package ufrn.dimap.lets.metric.visitor.exceptionalinterface;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IMember;
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
	public Map<String, Method> methods;

	public CalleeHierarchyVisitor ()
	{
		methods = new HashMap<>();
	}

	public boolean visit (MethodWrapper methodWrapper)
	{
		IMember member = methodWrapper.getMember();		

		if (methods.get(member.getHandleIdentifier()) != null)
		{
			System.out.println("Ja processado: " + member.getHandleIdentifier());
			return false;
		}
		else
		{
			System.out.println("Descobrindo: " + member.getHandleIdentifier());
			return true;
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

	public void postVisit (MethodWrapper methodWrapper)
	{
		System.out.println("Processando: " + methodWrapper.getMember().getHandleIdentifier());
		System.out.println("Métodos chamados... ");

		MethodWrapper [] calledWrapperMethods = methodWrapper.getCalls(new NullProgressMonitor());
		for (MethodWrapper m : calledWrapperMethods)
		{
			@SuppressWarnings("restriction")
			String methodIdentifier = m.getMember().getHandleIdentifier(); 
			System.out.println(methodIdentifier);
		}
		System.out.println();

		String methodSourceCode;

		try
		{
			methodSourceCode = methodWrapper.getMember().getSource();
		}
		catch (JavaModelException e)
		{
			throw new RuntimeException ("Não é para ocorrer!");
		} 

		if ( methodSourceCode == null )
		{
			throw new NoSourceCodeException ("" + methodWrapper.getMember().getHandleIdentifier());
		}
		else
		{




			// Nova instancia de Method que vai ser salva
			Method newMethod = new Method(methodWrapper.getMember().getHandleIdentifier());

			// Parsear o método atual, em busca de throws, rethrows, remaps e catches
			MethodVisitor methodVisitor = new MethodVisitor();
			CompilationUnit compilationUnit = HandlerUtil.parseMethod( methodSourceCode );
			compilationUnit.accept(methodVisitor);

			newMethod.thrownTypes.addAll(methodVisitor.thrownTypes);

			// Adicionar a interface excepcional dos métodos chamados no método atual
			for (MethodWrapper m : calledWrapperMethods)
			{
				if ( !m.isRecursive() )
				{
					String methodIdentifier = m.getMember().getHandleIdentifier(); 

					Method calledMethod = methods.get(methodIdentifier);
					newMethod.rethrownTypes.addAll(calledMethod.thrownTypes);
					newMethod.rethrownTypes.addAll(calledMethod.rethrownTypes);
				}
			}

			this.methods.put(newMethod.identifier, newMethod);

			System.out.println("Encerrando: " + newMethod.identifier + "\n");
			System.out.println();
		}

	}

	private boolean exists (Set<Method> methods, String methodIdentifier)
	{
		for ( Method method : methods )
		{
			if ( method.identifier.equals(methodIdentifier) )
			{
				System.out.println("Skipped: " + methodIdentifier);

				return true;
			}
		}

		return false;
	}
}

