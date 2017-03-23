package ufrn.dimap.lets.metric.handlers;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import ufrn.dimap.lets.metric.model.HierarchyModel;
import ufrn.dimap.lets.metric.views.ExceptionHierarchyView;
import ufrn.dimap.lets.metric.visitor.TypeVisitor;
import ufrn.dimap.lets.metric.visitor.UnresolvedBindingException;

public class ExceptionHierarchyHandler extends AbstractHandler
{	
	private final String reportHierarchy = "D:/Desenvolvimento/Resultados/hierarchy.txt";
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
			ExceptionHierarchyView view = (ExceptionHierarchyView) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(ExceptionHierarchyView.ID);
			view.setViewInput(this.model);
			//view.setFocus();
			
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

	private void parse (List<ICompilationUnit> compilationUnits) 
	{
		TypeVisitor visitor;

		visitor = new TypeVisitor();
		
		for (ICompilationUnit unit : compilationUnits)
		{
			CompilationUnit parse = HandlerUtil.parse(unit);
			
			try
			{
				parse.accept(visitor);
			}
			catch (UnresolvedBindingException ube)
			{
				System.err.println(ube.getMessage());
			}
		}
	}

	private void createReport() throws IOException
	{
		FileWriter outputFile = null;
		//ExceptionComparator comparator = new ExceptionComparator();
		
		
		outputFile = new FileWriter(new File(this.reportHierarchy));
		
		try
		{
			outputFile.write(this.model.toString());
		} 
		finally
		{
			outputFile.close();
		}
	}
}
