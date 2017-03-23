package ufrn.dimap.lets.metric.handlers;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import ufrn.dimap.lets.metric.model.CatchEntry;
import ufrn.dimap.lets.metric.model.FinallyEntry;
import ufrn.dimap.lets.metric.model.HierarchyModel;
import ufrn.dimap.lets.metric.model.SignalerEntry;
import ufrn.dimap.lets.metric.model.TryEntry;
import ufrn.dimap.lets.metric.views.SignalersView;
import ufrn.dimap.lets.metric.visitor.MetricsVisitor;
import ufrn.dimap.lets.metric.visitor.UncommonCodePatternException;
import ufrn.dimap.lets.metric.visitor.UncommonSignalerPatternException;

public class AllMetricsHandler extends AbstractHandler
{	
	private final String reportSignalers = "D:/Desenvolvimento/Resultados/signalers.txt";
	private final String reportTries = "D:/Desenvolvimento/Resultados/tries.txt";
	private final String reportCatches = "D:/Desenvolvimento/Resultados/catches.txt";
	private final String reportFinallies = "D:/Desenvolvimento/Resultados/finallies.txt";
	private HierarchyModel model;
	
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		List<ICompilationUnit> compilationUnits;
		
		try 
		{
			// Listando todos os compilation units selecionados
			compilationUnits = HandlerUtil.getAllCompilationUnits();
			
			// Resetando o modelo para receber os dados
			HierarchyModel.clearInstance();
			this.model = HierarchyModel.getInstance();
			
			
			// Processando o código, extraindo dados e armazenando no modelo
			parse( compilationUnits );
			
			
			// Exibindo a view para o usuario, e populando-a em seguida
			SignalersView view = (SignalersView) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView( SignalersView.ID);
			view.setViewInput(this.model);
						
			// Gerando relatório em txt
			this.createReport ();
			
		}
		catch (JavaModelException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (PartInitException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 

		return null;
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
}
