package ufrn.dimap.lets.metric.views;


import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.*;
import org.eclipse.ui.texteditor.ITextEditor;

import ufrn.dimap.lets.metric.model.CatchEntry;
import ufrn.dimap.lets.metric.model.MetricsModel;
import ufrn.dimap.lets.metric.model.SignalerEntry;
import ufrn.dimap.lets.metric.model.AbstractViewEntry;

import org.eclipse.jface.viewers.*;
import org.eclipse.swt.graphics.Image;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.action.*;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.*;
import org.eclipse.ui.ide.IDE;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.SWT;


/**
 * This sample class demonstrates how to plug-in a new
 * workbench view. The view shows data obtained from the
 * model. The sample creates a dummy model on the fly,
 * but a real implementation would connect to the model
 * available either in this or another plug-in (e.g. the workspace).
 * The view is connected to the model using a content provider.
 * <p>
 * The view uses a label provider to define how model
 * objects should be presented in the view. Each
 * view can present the same model objects using
 * different labels and icons, if needed. Alternatively,
 * a single label provider can be shared between views
 * in order to ensure that objects of the same type are
 * presented in the same way everywhere.
 * <p>
 */

public class SignalersView extends ViewPart {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "ufrn.dimap.lets.metric.views.SignalersView";

	private MetricsModel metricsModel;
	private TableViewer viewer;
	private Action action1;
	private Action action2;
	private Action doubleClickAction;
	private int itemId = 1;

	class ViewLabelProvider extends LabelProvider implements ITableLabelProvider {
		public String getColumnText(Object obj, int index)
		{
			if ( obj instanceof CatchEntry )
			{
				CatchEntry entry = (CatchEntry) obj;
				
				String rowItem = "";
			
				rowItem += itemId++ + ": "; 
				rowItem += entry.getInitLineNumber() + " - ";
				rowItem += entry.getLoCs() + " - ";
				
				rowItem += entry.catchedException.getQualifiedName() + " ";
				
				rowItem += entry.getICompilationUnit().getHandleIdentifier();
				
				return rowItem;
			}
			else if ( obj instanceof SignalerEntry )
			{
				SignalerEntry entry = (SignalerEntry) obj;
				
				String rowItem = "";
				
				rowItem += entry.getInitLineNumber() + ": ";
				rowItem += entry.getLoCs() + " - ";
				
				if ( entry.regularPattern )
				{
					if ( entry.rethrow ) rowItem += "T ";
					else				 rowItem += "F ";
					
					if ( entry.wrapping ) rowItem += "T ";
					else				  rowItem += "F ";
				}
				else
				{
					rowItem += "Error ";
				}
				
				rowItem += entry.signaledException.getQualifiedName() + " ";
				
				rowItem += entry.getICompilationUnit().getHandleIdentifier();
				
				return rowItem;
			}
			else if ( obj instanceof AbstractViewEntry )
			{
				AbstractViewEntry entry = (AbstractViewEntry) obj;
				
				String rowItem = "";
				
				rowItem += entry.getInitLineNumber() + ": ";
				rowItem += entry.getLoCs() + " - ";
				rowItem += entry.getICompilationUnit().getHandleIdentifier();
				
				return rowItem;
			}
			
			return "Failed in generate text. Type not supported.";
		}
		
		public Image getColumnImage(Object obj, int index) {
			return getImage(obj);
		}
		public Image getImage(Object obj) {
			return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT);
		}
	}
	
	class ViewContentProvider implements IStructuredContentProvider {
		
		@Override
		public Object[] getElements(Object inputElement) {
			MetricsModel model = (MetricsModel) inputElement;
			
			/*
			List<SignalerEntry> signalers = new ArrayList<SignalerEntry> ();
			
			for ( SignalerEntry entry : model.getSignalers() )
			{
				if ( entry.regularPattern == false )
				{
					signalers.add(entry);
				}
			}
			
			return signalers.toArray();
			*/
			
			return model.getCatches().toArray();
		}
	}

/**
	 * The constructor.
	 */
	public SignalersView() {
		this.metricsModel = null;
	}

	/**
	 * This is a callback that will allow us
	 * to create the viewer and initialize it.
	 */
	
	public void setViewInput (Object input)
	{
		this.metricsModel = (MetricsModel) input;
		this.viewer.setInput(input);
	}
	
	public void createPartControl(Composite parent) {
		viewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		
		viewer.setContentProvider(new ViewContentProvider());
		viewer.setInput(metricsModel);
		viewer.setLabelProvider(new ViewLabelProvider());

		// Create the help context id for the viewer's control
		PlatformUI.getWorkbench().getHelpSystem().setHelp(viewer.getControl(), "ufrn.dimap.lets.metric.viewer");
		getSite().setSelectionProvider(viewer);
		makeActions();
		hookContextMenu();
		hookDoubleClickAction();
		contributeToActionBars();
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				SignalersView.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(action1);
		manager.add(new Separator());
		manager.add(action2);
	}

	private void fillContextMenu(IMenuManager manager) {
		manager.add(action1);
		manager.add(action2);
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}
	
	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(action1);
		manager.add(action2);
	}

	private void makeActions() {
		action1 = new Action() {
			public void run() {
				showMessage("Action 1 executed");
			}
		};
		action1.setText("Action 1");
		action1.setToolTipText("Action 1 tooltip");
		action1.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
			getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
		
		action2 = new Action() {
			public void run() {
				showMessage("Action 2 executed");
			}
		};
		action2.setText("Action 2");
		action2.setToolTipText("Action 2 tooltip");
		action2.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
				getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
		
		doubleClickAction = new Action() {
			
		};
	}

	private void hookDoubleClickAction() {
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				doubleClickAction.run();
			}
		});
	}
	private void showMessage(String message) {
		MessageDialog.openInformation(
			viewer.getControl().getShell(),
			"Signalers View",
			message);
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}
}
