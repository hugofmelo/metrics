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
import ufrn.dimap.lets.exceptionalinterface.MethodNode;
import ufrn.dimap.lets.metric.model.HierarchyModel;

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

		System.out.println("CALL TREE");
		System.out.println(methodRoot.printGraph());

		System.out.println("THROWN: ");
		for ( String thrownType : methodRoot.getThrown() )
		{
			System.out.println(thrownType);
		}

		System.out.println("RETHROWN: ");
		for ( String rethrownType : methodRoot.getRethrown() )
		{
			System.out.println(rethrownType);
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
