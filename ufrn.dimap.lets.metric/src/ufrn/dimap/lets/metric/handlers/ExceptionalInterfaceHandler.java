package ufrn.dimap.lets.metric.handlers;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.internal.corext.callhierarchy.CallHierarchy;
import org.eclipse.jdt.internal.corext.callhierarchy.MethodCall;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.PlatformUI;

import ufrn.dimap.lets.metric.model.CatchEntry;
import ufrn.dimap.lets.metric.model.FinallyEntry;
import ufrn.dimap.lets.metric.model.HierarchyModel;
import ufrn.dimap.lets.metric.model.SignalerEntry;
import ufrn.dimap.lets.metric.model.TryEntry;
import ufrn.dimap.lets.metric.model.exceptionalinterface.Method;
import ufrn.dimap.lets.metric.visitor.MetricsVisitor;
import ufrn.dimap.lets.metric.visitor.UncommonCodePatternException;
import ufrn.dimap.lets.metric.visitor.UncommonSignalerPatternException;
import ufrn.dimap.lets.metric.visitor.exceptionalinterface.CalleeHierarchyVisitor;
import ufrn.dimap.lets.metric.visitor.exceptionalinterface.ExceptionalInterfaceGeneratorVisitor;

public class ExceptionalInterfaceHandler extends AbstractHandler
{	
	private final String reportSignalers = "D:/Desenvolvimento/Resultados/signalers.txt";
	private final String reportTries = "D:/Desenvolvimento/Resultados/tries.txt";
	private final String reportCatches = "D:/Desenvolvimento/Resultados/catches.txt";
	private final String reportFinallies = "D:/Desenvolvimento/Resultados/finallies.txt";
	private HierarchyModel model;
	private ExceptionalInterfaceGeneratorVisitor visitor;
	
	
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		CallHierarchy hierarchy = new CallHierarchy();
		IJavaSearchScope searchScope = SearchEngine.createWorkspaceScope();
		hierarchy.setSearchScope(searchScope);
		visitor = new ExceptionalInterfaceGeneratorVisitor(hierarchy);
		
		
		
		try
		{
			ICompilationUnit compilationUnit = getSelectedCompilationUnit();
			
			IType types[] = compilationUnit.getTypes();
			for ( IType type : types )
			{
				IMethod methods[] = type.getMethods();
				
				for ( IMethod method : methods )
				{
					//System.out.println( method.getElementName() );
					
					this.aaa(method);
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

	
	
	
	private void aaa ( IMethod method )
	{
		visitor.accept(method);
		
		Method resultMethod = visitor.getMethods().get(method.getHandleIdentifier());
		
		System.out.println(resultMethod.getIdentifier());
		
		System.out.println("Thrown: ");
		for ( String thrownType : resultMethod.getThrownTypes() )
		{
			System.out.println(thrownType);
		}
		
		System.out.println("Rethrown: ");
		for ( String rethrownType : resultMethod.getRethrownTypes() )
		{
			System.out.println(rethrownType);
		}
		System.out.println();
		
		/*
		ArrayList<MethodWrapper> callsWrapper = new ArrayList<MethodWrapper>();
		for (int i = 0; i < calleeWrapper.length; i++) 
		{
			MethodWrapper[] aaa = calleeWrapper[i].getCalls(new NullProgressMonitor());
			callsWrapper.addAll(Arrays.asList(aaa));
		}

		for (int i = 0; i < callsWrapper.size(); i++)
		    methodCalls.add(callsWrapper.get(i).getMethodCall());
		// Now you will get method calls in methodCalls list.
		
		for ( MethodCall methodCall : methodCalls )
		{
			System.out.println(methodCall.getMember());
		}
		*/
	}
	
	private void createReport() throws IOException
	{
		this.createSignalersReport();
		this.createCatchesReport();
		this.createTriesReport();
		this.createFinalliesReport();
	}

	private void parse (List<ICompilationUnit> compilationUnits) 
	{
		MetricsVisitor visitor;

		visitor = new MetricsVisitor();
		
		for (ICompilationUnit unit : compilationUnits)
		{
			CompilationUnit parse = HandlerUtil.parse(unit);
			
			try
			{
				parse.accept(visitor);
			}
			catch (UncommonSignalerPatternException e)
			{
				System.err.println( e.getMessage() );
				System.err.println( e.getCodeSnippet() );
				System.err.println( e.getFileName() );
				System.err.println( e.getLineNumber() );
				System.err.println();
				System.err.println();
			}
			catch (UncommonCodePatternException e)
			{
				System.err.println( e.getMessage() );
				System.err.println( e.getCodeSnippet() );
				System.err.println( e.getFileName() );
				System.err.println( e.getLineNumber() );
				System.err.println();
				System.err.println();
			}
		}
			
	}

	private void createSignalersReport() throws IOException
	{
		FileWriter outputFile = null;	
		
		outputFile = new FileWriter(new File(this.reportSignalers));
		
		try
		{
			String handleIdentifier;
			String project, sourceDir, pkg, clazz;
		
			outputFile.write("Line\tProject\tSource dir\tPackage\tClass\tSignaled Class\tRegular\tRethrow\tWrapped\tLoCs\n");
			for ( SignalerEntry entry : this.model.getSignalers() )
			{
				handleIdentifier = entry.getICompilationUnit().getHandleIdentifier();
				
				project = handleIdentifier.substring(handleIdentifier.indexOf('=') + 1, handleIdentifier.indexOf('/'));
				sourceDir = handleIdentifier.substring(handleIdentifier.indexOf('/') + 1, handleIdentifier.indexOf('<'));
				pkg = handleIdentifier.substring(handleIdentifier.indexOf('<') + 1, handleIdentifier.indexOf('{'));
				clazz = handleIdentifier.substring(handleIdentifier.indexOf('{') + 1);			
				
				outputFile.write(entry.getInitLineNumber()+"\t");
				outputFile.write(project+"\t"+sourceDir+"\t"+pkg+"\t"+clazz+"\t");
				outputFile.write(entry.signaledException.getQualifiedName()+"\t");
				outputFile.write(entry.regularPattern+"\t");
				outputFile.write(entry.rethrow+"\t");
				outputFile.write(entry.wrapping+"\t");
				outputFile.write(entry.getLoCs()+"\n");				
			}
		} 
		finally
		{
			outputFile.close();
		}
	}
	
	private void createCatchesReport() throws IOException
	{
		FileWriter outputFile = null;	
		outputFile = new FileWriter(new File(this.reportCatches));
		
		try
		{
			String handleIdentifier;
			String project, sourceDir, pkg, clazz;
		
			outputFile.write("Line\tProject\tSource dir\tPackage\tClass\tCatched Class\tLoCs\n");
			for ( CatchEntry entry : this.model.getCatches() )
			{
				handleIdentifier = entry.getICompilationUnit().getHandleIdentifier();
				
				project = handleIdentifier.substring(handleIdentifier.indexOf('=') + 1, handleIdentifier.indexOf('/'));
				sourceDir = handleIdentifier.substring(handleIdentifier.indexOf('/') + 1, handleIdentifier.indexOf('<'));
				pkg = handleIdentifier.substring(handleIdentifier.indexOf('<') + 1, handleIdentifier.indexOf('{'));
				clazz = handleIdentifier.substring(handleIdentifier.indexOf('{') + 1);
				
				outputFile.write(entry.getInitLineNumber()+"\t");
				outputFile.write(project+"\t"+sourceDir+"\t"+pkg+"\t"+clazz+"\t");
				outputFile.write(entry.catchedException.getQualifiedName()+"\t");
				outputFile.write(entry.getLoCs()+"\n");
			}
		} 
		finally
		{
			outputFile.close();
		}
	}
	
	private void createTriesReport() throws IOException
	{
		FileWriter outputFile = null;	
		
		outputFile = new FileWriter(new File(this.reportTries));
		
		try
		{
			String handleIdentifier;
			String project, sourceDir, pkg, clazz;
		
			outputFile.write("Line\tProject\tSource dir\tPackage\tClass\tLoCs\n");
			for ( TryEntry entry : this.model.getTries() )
			{
				handleIdentifier = entry.getICompilationUnit().getHandleIdentifier();
				
				project = handleIdentifier.substring(handleIdentifier.indexOf('=') + 1, handleIdentifier.indexOf('/'));
				sourceDir = handleIdentifier.substring(handleIdentifier.indexOf('/') + 1, handleIdentifier.indexOf('<'));
				pkg = handleIdentifier.substring(handleIdentifier.indexOf('<') + 1, handleIdentifier.indexOf('{'));
				clazz = handleIdentifier.substring(handleIdentifier.indexOf('{') + 1);
				
				outputFile.write(entry.getInitLineNumber()+"\t");
				outputFile.write(project+"\t"+sourceDir+"\t"+pkg+"\t"+clazz+"\t");
				outputFile.write(entry.getLoCs()+"\n");
			}
		} 
		finally
		{
			outputFile.close();
		}
	}
	
	private void createFinalliesReport() throws IOException
	{
		FileWriter outputFile = null;	
		
		outputFile = new FileWriter(new File(this.reportFinallies));
		
		try
		{
			String handleIdentifier;
			String project, sourceDir, pkg, clazz;
		
			
			outputFile.write("Line\tProject\tSource dir\tPackage\tClass\tLoCs\n");
			for ( FinallyEntry entry : this.model.getFinallies() )
			{
				handleIdentifier = entry.getICompilationUnit().getHandleIdentifier();
				
				project = handleIdentifier.substring(handleIdentifier.indexOf('=') + 1, handleIdentifier.indexOf('/'));
				sourceDir = handleIdentifier.substring(handleIdentifier.indexOf('/') + 1, handleIdentifier.indexOf('<'));
				pkg = handleIdentifier.substring(handleIdentifier.indexOf('<') + 1, handleIdentifier.indexOf('{'));
				clazz = handleIdentifier.substring(handleIdentifier.indexOf('{') + 1);
				
				outputFile.write(entry.getInitLineNumber()+"\t");
				outputFile.write(project+"\t"+sourceDir+"\t"+pkg+"\t"+clazz+"\t");
				outputFile.write(entry.getLoCs()+"\n");
			}
		} 
		finally
		{
			outputFile.close();
		}
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
