package ufrn.dimap.lets.metric.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.PlatformUI;

import ufrn.dimap.lets.exceptionalinterface.CallgraphGenerator;
import ufrn.dimap.lets.exceptionalinterface.EIType;
import ufrn.dimap.lets.exceptionalinterface.MethodNode;

public class ExceptionalInterfaceHandler extends AbstractHandler
{	
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		try
		{
			ICompilationUnit compilationUnit = getSelectedCompilationUnit();

			IType types[] = compilationUnit.getTypes();
			for ( IType type : types )
			{
				IMethod methods[] = type.getMethods();

				for ( IMethod method : methods )
				{
					bbb (method);
					return null;
				}

			}
		}
		catch (JavaModelException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}
	
	private void bbb( IMethod method)
	{
		MethodNode methodRoot = CallgraphGenerator.generatePrunedGraphFrom(method);

		try
		{
			methodRoot.computeExceptionalInterface();
		}
		catch (JavaModelException e)
		{
			e.printStackTrace();
		}

		//System.out.println("CALL TREE");
		//System.out.println(methodRoot.printGraph());

		System.out.println("METHOD");
		System.out.println(methodRoot.toString());
		
		System.out.println("CALLEES: ");
		for ( MethodNode callee : methodRoot.getChildren())
		{
			System.out.println(callee.toString());
		}
		
		System.out.println("CAUGHT: ");
		for ( EIType type : methodRoot.getExceptionalInterface().getCaught().keySet())
		{
			System.out.println(type.getQualifiedName());
		}

		System.out.println("PROPAGATED: ");
		for ( EIType type : methodRoot.getExceptionalInterface().getPropagated() )
		{
			System.out.println(type.getQualifiedName());
		}
		
		System.out.println("THROWN: ");
		for ( EIType type : methodRoot.getExceptionalInterface().getThrown() )
		{
			System.out.println(type.getQualifiedName());
		}
		
		System.out.println("RETHROWN: ");
		for ( EIType type : methodRoot.getExceptionalInterface().getRethrown() )
		{
			System.out.println(type.getQualifiedName());
		}
		
		System.out.println("WRAPPED: ");
		for ( EIType type : methodRoot.getExceptionalInterface().getWrapped().keySet() )
		{
			System.out.println(type.getQualifiedName());
		}
		System.out.println();
	}

	public static ICompilationUnit getSelectedCompilationUnit () throws JavaModelException
	{
		ISelectionService selectionService = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		ISelection selection = selectionService.getSelection();    

		// Buscar por todos os packages selecionados
		if(selection instanceof StructuredSelection)
		{
			Object element = ((StructuredSelection)selection).getFirstElement();

			if( element instanceof ICompilationUnit)
			{
				return (ICompilationUnit) element;	
			}
		}

		return null;
	}
}
