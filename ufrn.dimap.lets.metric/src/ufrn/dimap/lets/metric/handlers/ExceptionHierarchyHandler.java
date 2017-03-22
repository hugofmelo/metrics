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

import ufrn.dimap.lets.metric.model.hierarchy.HierarchyModel;
import ufrn.dimap.lets.metric.views.ExceptionHierarchyView;
import ufrn.dimap.lets.metric.visitor.ExceptionHierarchyVisitor;
import ufrn.dimap.lets.metric.visitor.UnresolvedBindingException;

public class ExceptionHierarchyHandler extends AbstractHandler
{	
	private final String reportHierarchy = "C:/Users/hugofm/Desenvolvimento/Resultados/hierarchy.txt";
	private HierarchyModel model;
	
	
	public Object execute(ExecutionEvent event) throws ExecutionException
	{

		List<ICompilationUnit> compilationUnits;
		
		try 
		{
			// Listando todos os compilation units selecionados
			compilationUnits = HandlerUtil.getAllCompilationUnits();
			
			// Resetando o modelo para receber os dados
			this.model = new HierarchyModel();
			
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
		ExceptionHierarchyVisitor visitor;

		visitor = new ExceptionHierarchyVisitor(this.model);
		
		for (ICompilationUnit unit : compilationUnits)
		{
			CompilationUnit parse = parse(unit);

			//System.out.println(parse.getJavaElement().getElementName());
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
		
		try {
			outputFile = new FileWriter(new File(this.reportHierarchy));
			outputFile.write(this.model.toString());
		} 
		finally
		{
			outputFile.close();
		}
	}
	
	/**
	 * Reads a ICompilationUnit and creates the AST DOM for manipulating the
	 * Java source file
	 *
	 * @param unit
	 * @return
	 */

	private static CompilationUnit parse(ICompilationUnit unit) {
		ASTParser parser = ASTParser.newParser(AST.JLS8);
		parser.setResolveBindings(true);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(unit);

		return (CompilationUnit) parser.createAST(null); // parse
	}
	
	private class ExceptionComparator implements Comparator <ITypeBinding>
	{

		@Override
		public int compare (ITypeBinding first, ITypeBinding second)
		{
			return first.getQualifiedName().compareTo(second.getQualifiedName());
		}
		
	}
}
