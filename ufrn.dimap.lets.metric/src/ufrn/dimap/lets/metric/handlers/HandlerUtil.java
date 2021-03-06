package ufrn.dimap.lets.metric.handlers;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.PlatformUI;

public class HandlerUtil
{
	
	public static List<ICompilationUnit> getAllCompilationUnits () throws JavaModelException
	{
		ISelectionService selectionService = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		ISelection selection = selectionService.getSelection();    
		
		// Lista de todos os packages selecionados diretamente ou indiretamente pelo usu�rio
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
}
