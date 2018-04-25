package ufrn.dimap.lets.metric.handlers;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.PlatformUI;

import ufrn.dimap.lets.exceptionalinterface.CallGraphPrinter;
import ufrn.dimap.lets.exceptionalinterface.CallgraphGenerator;
import ufrn.dimap.lets.exceptionalinterface.MethodNode;
import ufrn.dimap.lets.exceptionalinterface.ShouldNotHappenException;
import ufrn.dimap.lets.exceptionalinterface.Signaler;

public class ExceptionalInterfaceHandler extends AbstractHandler
{		
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
			throw new ShouldNotHappenException(e);
		}
		
		time = System.nanoTime();
		MethodNode methodRoot = CallgraphGenerator.generatePrunedGraphFrom(method);
		time = System.nanoTime() - time; 
		
		System.out.println("\n\nINICIO DA EXECUÇÃO\n");
		System.out.println("Tempo (milisegundos) de criação do grafo de chamadas: " + TimeUnit.MILLISECONDS.convert(time, TimeUnit.NANOSECONDS));
		System.out.println( "Grafo de chamadas completo:");
		
		try
		{
			CallGraphPrinter.printPruned(methodRoot, Paths.get("..//teste.txt"));
		}
		catch (IOException e1)
		{
			e1.printStackTrace();
		}
		
		
		
//		System.out.println( "Grafo de chamadas podado:\n" + methodRoot.printPrunedGraph());
//		System.out.println();
		
		try
		{
			time = System.nanoTime();
			// TODO processar callgraph e interface junto
			methodRoot.computeExceptionalInterface();
			time = System.nanoTime() - time;
			
			System.out.println( "Tempo (milisegundos) de calculo das interfaces excepcionais: " + TimeUnit.MILLISECONDS.convert(time, TimeUnit.NANOSECONDS) );
			
			
			System.out.println();
			System.out.println("METHOD");
			System.out.println(methodRoot);
			System.out.println("CALLEES");
			for ( MethodNode callee : methodRoot.getChildren())
			{
				System.out.println(callee);
			}
			
			System.out.println("EXCEPTIONAL INTERFACE: ");
			for ( Signaler signaler : methodRoot.getExceptionalInterface().getSignalers() )
			{
				System.out.println(signaler);
			}
		}
		catch (Exception e)
		{
			System.out.println(e);
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

		throw new RuntimeException("Não foi selecionado um método!!");
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
