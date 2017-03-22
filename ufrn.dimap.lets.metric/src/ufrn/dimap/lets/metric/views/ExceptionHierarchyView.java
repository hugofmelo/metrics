package ufrn.dimap.lets.metric.views;


import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import ufrn.dimap.lets.metric.model.hierarchy.HierarchyModel;
import ufrn.dimap.lets.metric.model.hierarchy.TypeEntry;


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

public class ExceptionHierarchyView extends ViewPart {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "ufrn.dimap.lets.metric.views.ExceptionHierarchyView";

	private HierarchyModel hierarchyModel;
	private TreeViewer viewer;
	private Action action1;
	private Action action2;
	private Action doubleClickAction;
	 

	class ViewLabelProvider implements ILabelProvider
	{

		@Override
		public void addListener(ILabelProviderListener listener) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void dispose() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public boolean isLabelProperty(Object element, String property) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void removeListener(ILabelProviderListener listener) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public Image getImage(Object element) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getText(Object element)
		{
			if ( element instanceof TypeEntry )
				return "" + ((ITypeBinding)((TypeEntry)element).getBinding()).getQualifiedName();
			else
				return null;
		}
		
	}
	
	class ViewContentProvider implements ITreeContentProvider {
		
		@Override
		public Object[] getElements(Object parentElement)
		{
			HierarchyModel model;
			
			if ( parentElement instanceof HierarchyModel )
			{
				model = (HierarchyModel) parentElement;
				
				return new Object [] {model.getRoot()};
			}
			else
				return null;
		}

		@Override
		public Object[] getChildren(Object parentElement)
		{
			if ( parentElement instanceof TypeEntry )
				return ((TypeEntry)parentElement).subtypes.toArray();
			else
				return null;
		}

		@Override
		public Object getParent(Object element)
		{
			if ( element instanceof TypeEntry )
				return ((TypeEntry)element).superType;
			else
				return null;
		}

		@Override
		public boolean hasChildren(Object element)
		{
			if ( element instanceof TypeEntry )
				return ((TypeEntry)element).subtypes.size() > 0;
			else
				return false;
		}
	}

/**
	 * The constructor.
	 */
	public ExceptionHierarchyView() {
		
	}

	/**
	 * This is a callback that will allow us
	 * to create the viewer and initialize it.
	 */
	
	public void setViewInput (Object input)
	{
		this.hierarchyModel = (HierarchyModel) input;
		this.viewer.setInput(input);
	}
	
	public void createPartControl(Composite parent) {
		viewer = new TreeViewer(parent);
		viewer.setContentProvider(new ViewContentProvider());
		viewer.setLabelProvider(new ViewLabelProvider());
		viewer.setInput(this.hierarchyModel);
		viewer.expandAll();

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
				ExceptionHierarchyView.this.fillContextMenu(manager);
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
		
		doubleClickAction = new OpenEntryInEditorAction(viewer);
		/*
		doubleClickAction = new Action() {
			public void run() {
				ISelection selection = viewer.getSelection();
				HierarchyNode node = (HierarchyNode) ((IStructuredSelection)selection).getFirstElement();
				
				try {
					JavaUI.openInEditor(node.exception);
				} catch (PartInitException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (JavaModelException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				/*
				IFile file = null;
				ICompilationUnit iCompilationUnit = (ICompilationUnit) JavaCore.create (node.exception);
				try {
					file = (IFile)iCompilationUnit.getUnderlyingResource();
				} catch (JavaModelException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
				IWorkbenchPage page = window.getActivePage();
				IEditorPart editor = null;
				try {
					editor = org.eclipse.ui.ide.IDE.openEditor(page, file);
				} catch (PartInitException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				ITextEditor textEditor = (ITextEditor) editor;
				textEditor.selectAndReveal(entry.offset, entry.length);
				
				//showMessage("Double-click detected on "+obj.toString());
			}
		};
		*/
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
			"Exception Hierarchy",
			message);
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}
}
