package ufrn.dimap.lets.metric.handlers;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.PlatformUI;

import ufrn.dimap.lets.exceptionalinterface.InvalidStateException;

public class HandlerUtil
{

	public static List<ICompilationUnit> getAllCompilationUnits () throws JavaModelException
	{
		ISelectionService selectionService = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		ISelection selection = selectionService.getSelection();    

		// Lista de todos os packages selecionados diretamente ou indiretamente pelo usuário
		LinkedHashSet <IPackageFragment> srcPackages = new LinkedHashSet<IPackageFragment> ();

		// Buscar por todos os packages selecionados
		if(selection instanceof StructuredSelection)
		{
			@SuppressWarnings("unchecked")
			List <Object> elements = ((StructuredSelection)selection).toList();

			for ( Object element : elements )
			{
				if( element instanceof IJavaProject)
				{
					IJavaProject javaProject = (IJavaProject)element;

					for ( IPackageFragment pf : javaProject.getPackageFragments() )
					{
						if ( pf.getKind() == IPackageFragmentRoot.K_SOURCE )	
						{
							srcPackages.add(pf);
						}
					}	
				}
				else if( element instanceof IPackageFragmentRoot)
				{

					IPackageFragmentRoot packageFragmentRoot = (IPackageFragmentRoot)element;


					for ( IJavaElement pf : packageFragmentRoot.getChildren() )
					{
						if ( ((IPackageFragment)pf).getKind() == IPackageFragmentRoot.K_SOURCE )	
						{
							srcPackages.add((IPackageFragment)pf);
						}
					}
				}
				else if( element instanceof IPackageFragment)
				{
					srcPackages.add((IPackageFragment)element);
				}
			}
		}

		// Para cada packageFragment, pegar os compilationUnits dele
		List <ICompilationUnit> compilationUnits = new ArrayList <ICompilationUnit> ();
		for ( IPackageFragment packageFragment : srcPackages )
		{
			for ( ICompilationUnit cu : packageFragment.getCompilationUnits() )
			{
				compilationUnits.add(cu);
			}
		}

		return compilationUnits;
	}

	/**
	 * Reads a ICompilationUnit and creates the AST DOM for manipulating the
	 * Java source file
	 *
	 * @param unit
	 * @return
	 * @throws JavaModelException 
	 */
	public static CompilationUnit parse(ICompilationUnit unit)
	{
		ASTParser parser = ASTParser.newParser(AST.JLS8);
		parser.setResolveBindings(true);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(unit);

		return (CompilationUnit) parser.createAST(null);
	}

	/**
	 * Recebe uma string que representa o código de um método. 
	 *
	 * @param sourceCode de um método
	 * @return
	 */
	public static CompilationUnit parseMethod(String sourceCode)
	{
		// O argumento sourceCode representa um único método. Como o parser do eclipse não aceita parsear somente um método, nós encapsulamos ele em uma classe. 
		String dummyClass = "public class A {"+ sourceCode + "}";

		ASTParser parser = ASTParser.newParser(AST.JLS8);
		parser.setResolveBindings(true);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(dummyClass.toCharArray());

		return (CompilationUnit) parser.createAST(null);
	}

	public static CompilationUnit parse(IMethod method) throws JavaModelException
	{
		if ( method.getCompilationUnit() != null )
		{
			return HandlerUtil.parse( method.getCompilationUnit() );
		}
		else if ( method.getClassFile() != null && method.getClassFile().getSource() != null )
		{
			return HandlerUtil.parse( method.getClassFile() );
		}
		else
		{
			throw new InvalidStateException("IMethod invalido. Deveria ser ECompilationUnit ou IClassFile com source.");
		}
	}

	private static CompilationUnit parse(IClassFile classFile)
	{
		ASTParser parser = ASTParser.newParser(AST.JLS8);
		parser.setResolveBindings(true);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(classFile);

		return (CompilationUnit) parser.createAST(null);
	}
}
