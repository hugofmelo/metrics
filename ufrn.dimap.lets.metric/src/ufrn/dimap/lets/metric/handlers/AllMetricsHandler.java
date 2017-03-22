package ufrn.dimap.lets.metric.handlers;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import ufrn.dimap.lets.metric.model.CatchEntry;
import ufrn.dimap.lets.metric.model.FinallyEntry;
import ufrn.dimap.lets.metric.model.MetricsModel;
import ufrn.dimap.lets.metric.model.SignalerEntry;
import ufrn.dimap.lets.metric.model.TryEntry;
import ufrn.dimap.lets.metric.views.SignalersView;
import ufrn.dimap.lets.metric.visitor.MetricsVisitor;
import ufrn.dimap.lets.metric.visitor.UncommonCodePatternException;
import ufrn.dimap.lets.metric.visitor.UncommonSignalerPatternException;

public class AllMetricsHandler extends AbstractHandler
{	
	private final String reportSignalers = "C:/Users/Hugo/Dropbox/Doutorado/Pesquisa/Artigo Taiza/signalers.txt";
	private final String reportTries = "C:/Users/Hugo/Dropbox/Doutorado/Pesquisa/Artigo Taiza/tries.txt";
	private final String reportCatches = "C:/Users/Hugo/Dropbox/Doutorado/Pesquisa/Artigo Taiza/catches.txt";
	private final String reportFinallies = "C:/Users/Hugo/Dropbox/Doutorado/Pesquisa/Artigo Taiza/finallies.txt";
	private MetricsModel model;
	
	public Object execute(ExecutionEvent event) throws ExecutionException {

		ISelectionService  selectionService = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		ISelection selection = selectionService.getSelection(/*"org.eclipse.jdt.ui.ProjectExplorer"*/);    
		
		IProject project = null;
		IJavaProject javaProject = null;
		IPackageFragmentRoot packageFragmentRoot = null;
		IPackageFragment packageFragment = null;
		
		LinkedHashSet <IPackageFragment> srcPackages = new LinkedHashSet<IPackageFragment> ();
		
		if(selection instanceof StructuredSelection)
		{
			List <Object> elements = ((StructuredSelection)selection).toList();
			
			for ( Object element : elements )
			{
				if( element instanceof IJavaProject)
				{
					javaProject = (IJavaProject)element;
					
					try {
						for ( IPackageFragment pf : javaProject.getPackageFragments() )
						{
							if ( pf.getKind() == IPackageFragmentRoot.K_SOURCE )	
							{
								srcPackages.add(pf);
							}
						}
					} catch (JavaModelException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}               
					finally {
						
					}
				
				}
				else if( element instanceof IPackageFragmentRoot)
				{
					packageFragmentRoot = (IPackageFragmentRoot)element;
					
					try {
						for ( IJavaElement pf : packageFragmentRoot.getChildren() )
						{
							if ( ((IPackageFragment)pf).getKind() == IPackageFragmentRoot.K_SOURCE )	
							{
								srcPackages.add((IPackageFragment)pf);
							}
						}
					} catch (JavaModelException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				else if( element instanceof IPackageFragment)
				{
					packageFragment = (IPackageFragment)element;
					
					srcPackages.add(packageFragment);
				}
			}
			
		}
		
		/*
		if(selection instanceof IStructuredSelection)
		{    
			Object element = ((IStructuredSelection)selection).getFirstElement();    

			if (element instanceof IResource)
			{    
				project= ((IResource)element).getProject();    
			}
		}
		*/
		/*
		// Get the root of the workspace
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();
		// Get all projects in the workspace
		IProject[] projects = root.getProjects();
		// Loop over all projects

		srcPackages = new ArrayList<IPackageFragment>();

		for (IProject project : projects)
		{
			try 
			{
				printProjectInfo(project);
			} 
			catch (CoreException e)
			{
				e.printStackTrace();
			}
		}
		*/
		
		/*
		try 
		{
			//if (project.isNatureEnabled(JavaCore.NATURE_ID))
			//{
			//	IJavaProject javaProject = JavaCore.create(project);

				for ( IPackageFragment packageFragment : javaProject.getPackageFragments() )
				{
					if ( packageFragment.getKind() == IPackageFragmentRoot.K_SOURCE )	
					{
						if (packageFragment.isOpen())
						{
							srcPackages.add(packageFragment);
						}
					}
				}
			//}
		} catch (JavaModelException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (CoreException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		*/
		try {
			parsePackages( srcPackages );
		} catch (JavaModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		

		return null;
	}

	/*
	private void printProjectInfo(IProject project) throws CoreException,
	JavaModelException
	{
		System.out.println("Working in project " + project.getName());
		// check if we have a Java project
		if (project.isNatureEnabled(JavaCore.NATURE_ID))
		{
			IJavaProject javaProject = JavaCore.create(project);

			for ( IPackageFragment packageFragment : javaProject.getPackageFragments() )
			{
				if ( packageFragment.getKind() == IPackageFragmentRoot.K_SOURCE )	
				{
					srcPackages.add(packageFragment);
				}
			}
		}
	}
	*/

	private void parsePackages(HashSet<IPackageFragment> srcPackages) throws JavaModelException
	{
		MetricsVisitor visitor;

		this.model = new MetricsModel();
		visitor = new MetricsVisitor(this.model);
		
		for ( IPackageFragment srcPackage : srcPackages )
		{
			for (ICompilationUnit unit : srcPackage.getCompilationUnits())
			{
				// now create the AST for the ICompilationUnits
				CompilationUnit parse = parse(unit);

				//System.out.println(parse.getJavaElement().getElementName());
				try
				{
					parse.accept(visitor);
				}
				catch (UncommonSignalerPatternException e)
				{
					e.printStackTrace();
					// TODO Poderia exibir o trecho de código e perguntar ao programdor que tipo de sinalizador é aquele
				}
				catch (UncommonCodePatternException e)
				{
					System.err.println( e.getMessage() );
					System.err.println( e.getASTNode().toString() );
				}
				
			}
		}

		
		
		System.out.println("total signalers: " + this.model.getSignalers().size());
		System.out.println("total catches: " + this.model.getCatches().size());
		System.out.println("total finally: " + this.model.getFinallies().size());
		System.out.println("total tries: " + this.model.getTries().size());
		System.out.println("total rethrows: " + this.model.getRethrows());
		System.out.println("total wrapped: " + this.model.getWrappings());
		
		try {
			SignalersView view = (SignalersView) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView( SignalersView.ID);
			view.setViewInput(visitor.model);
			//view.setFocus();
		} catch (PartInitException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		this.createReport ();
	}

	private void createReport()
	{
		FileWriter outputFile = null;
		
		String handleIdentifier;
		String project, sourceDir, pkg, clazz;
		
		try {
			
			// SIGNALERS
			outputFile = new FileWriter(new File(this.reportSignalers));
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
			
			outputFile.close();
			
			// CATCHES
			outputFile = new FileWriter(new File(this.reportCatches));
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
			
			outputFile.close();
			
			
			// TRIES
			outputFile = new FileWriter(new File(this.reportTries));
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
			
			outputFile.close();

			// FINALLIES
			outputFile = new FileWriter(new File(this.reportFinallies));
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
			
			outputFile.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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

	private void printICompilationUnitInfo(IPackageFragment mypackage)
			throws JavaModelException {
		for (ICompilationUnit unit : mypackage.getCompilationUnits()) {
			printCompilationUnitDetails(unit);

		}
	}

	private void printIMethods(ICompilationUnit unit) throws JavaModelException {
		IType[] allTypes = unit.getAllTypes();
		for (IType type : allTypes) {
			printIMethodDetails(type);
		}
	}

	private void printCompilationUnitDetails(ICompilationUnit unit)
			throws JavaModelException {
		System.out.println("Source file " + unit.getElementName());
		Document doc = new Document(unit.getSource());
		System.out.println("Has number of lines: " + doc.getNumberOfLines());
		printIMethods(unit);
	}

	private void printIMethodDetails(IType type) throws JavaModelException {
		IMethod[] methods = type.getMethods();
		for (IMethod method : methods) {

			System.out.println("Method name " + method.getElementName());
			System.out.println("Signature " + method.getSignature());
			System.out.println("Return Type " + method.getReturnType());

		}
	}
}
