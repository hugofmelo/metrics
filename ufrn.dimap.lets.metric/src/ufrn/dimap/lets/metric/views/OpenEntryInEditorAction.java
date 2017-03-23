package ufrn.dimap.lets.metric.views;

import java.util.Iterator;

import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.texteditor.ITextEditor;

import ufrn.dimap.lets.metric.model.AbstractEntry;
import ufrn.dimap.lets.metric.model.TypeEntry;

public class OpenEntryInEditorAction extends Action
{
	private StructuredViewer viewer;
	
	public OpenEntryInEditorAction ( StructuredViewer viewer )
	{
		this.viewer = viewer;
	}
	
	public void run()
	{
		IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
		Iterator <IStructuredSelection> iteSelection = selection.iterator();
		
		while ( iteSelection.hasNext() )
		{
			AbstractEntry entry = (AbstractEntry) iteSelection.next();
			
			try
			{
				openAndSelect (entry);
			}
			catch (PartInitException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			catch (JavaModelException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
	private void openAndSelect (AbstractEntry entry) throws PartInitException, JavaModelException 
	{
		//IEditorPart editor = JavaUI.openInEditor(entry.getICompilationUnit());
		IEditorPart editor = JavaUI.openInEditor(entry.getJavaElement());
		
		if (entry.hasNode() && !(entry instanceof TypeEntry) )
		{
			ITextEditor textEditor = (ITextEditor) editor;
			textEditor.selectAndReveal(entry.getStartPosition(), entry.getLength());
		}
		
	}
}
