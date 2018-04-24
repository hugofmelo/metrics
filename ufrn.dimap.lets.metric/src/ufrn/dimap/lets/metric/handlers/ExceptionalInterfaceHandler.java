package ufrn.dimap.lets.metric.handlers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
import ufrn.dimap.lets.exceptionalinterface.Signaler;

public class ExceptionalInterfaceHandler extends AbstractHandler
{	
	private static Logger logger = LogManager.getLogger();
	
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		long time;
		IMethod method = null;
		
		try
		{
			method = getSelectedMethod();
		}
		catch (JavaModelException e)
		{
			logger.warn(e);
			return null;
		}
		
		time = System.nanoTime();
		MethodNode methodRoot = CallgraphGenerator.generatePrunedGraphFrom(method);
		time = System.nanoTime() - time; 
		
		logger.info ( "Tempo de criação do grafo de chamadas:\t" + time );
		logger.info ( "Grafo de chamadas:\n" + methodRoot.printGraph());
		
		try
		{
			time = System.nanoTime();
			methodRoot.computeExceptionalInterface();
			time = System.nanoTime() - time;
			
			
			logger.info ( "Interface excepcional do método:\n" + methodRoot.getExceptionalInterface());
			
			logger.info("METHOD");
			logger.info(methodRoot);
			logger.info ( "Tempo de calculo das interfaces excepcionais:\t" + time );
			
			logger.info("CALLEES");
			for ( MethodNode callee : methodRoot.getChildren())
			{
				logger.info(callee);
			}
			
			logger.info("EXCEPTIONAL INTERFACE: ");
			for ( Signaler signaler : methodRoot.getExceptionalInterface().getSignalers() )
			{
				logger.info(signaler);
			}
		}
		catch (JavaModelException e)
		{
			logger.error(e);
		}
		
		return null;
	}
	
	private static IMethod getSelectedMethod () throws JavaModelException
	{
		ISelectionService selectionService = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		ISelection selection = selectionService.getSelection();    

		// Buscar por todos os packages selecionados
		if(selection instanceof StructuredSelection)
		{
			Object element = ((StructuredSelection)selection).getFirstElement();

			if( element instanceof IMethod)
			{
				return (IMethod) element;	
			}
		}

		return null;
	}
	
	private static ICompilationUnit getSelectedCompilationUnit () throws JavaModelException
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
