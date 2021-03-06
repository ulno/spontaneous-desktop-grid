package ee.ut.f2f.visualizer.advisor;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.ICoolBarManager;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarContributionItem;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;

import ee.ut.f2f.visualizer.action.CollectDataAction;
import ee.ut.f2f.visualizer.action.OpenFileAction;
import ee.ut.f2f.visualizer.action.SaveFileAction;
import ee.ut.f2f.visualizer.editor.GraphEditor;

/**
 * Responsible for creating, adding and disposing of the actions added to a
 * workbench window.
 * 
 * Each window will be populated with new actions.
 * 
 * @author Indrek Priks
 */
public class ApplicationActionBarAdvisor extends ActionBarAdvisor {
	
	private IWorkbenchAction exitAction;
	private IWorkbenchAction aboutAction;
	private IWorkbenchAction newWindowAction;
	private Action openFileAction;
	private Action saveFileAction;
	private Action collectDataAction;
	
	/**
	 * Default constructor.
	 * 
	 * @param configurer
	 *          action bar configurer
	 */
	public ApplicationActionBarAdvisor(IActionBarConfigurer configurer) {
		super(configurer);
	}
	
	/**
	 * Creates the actions and registers them.
	 * 
	 * Registering is needed to ensure that key bindings work. The corresponding
	 * commands keybindings are defined in the plugin.xml file. Registering also
	 * provides automatic disposal of the actions when the window is closed.
	 * 
	 * @param window
	 *          Workbench window
	 */
	@Override
	protected void makeActions(final IWorkbenchWindow window) {
		
		exitAction = ActionFactory.QUIT.create(window);
		register(exitAction);
		
		aboutAction = ActionFactory.ABOUT.create(window);
		register(aboutAction);
		
		newWindowAction = ActionFactory.OPEN_NEW_WINDOW.create(window);
		register(newWindowAction);
		
		openFileAction = new OpenFileAction(window, "Open From File", GraphEditor.ID);
		register(openFileAction);
		
		saveFileAction = new SaveFileAction(window, "Save As File");
		register(saveFileAction);
		
		collectDataAction = new CollectDataAction(window, "Collect Data", GraphEditor.ID);
		register(collectDataAction);
		
	}
	
	/**
	 * Fills the menu with actions.
	 * 
	 * @param menuBar
	 *          Menu manager
	 */
	@Override
	protected void fillMenuBar(IMenuManager menuBar) {
		MenuManager fileMenu = new MenuManager("&File", IWorkbenchActionConstants.M_FILE);
		MenuManager helpMenu = new MenuManager("&Help", IWorkbenchActionConstants.M_HELP);
		
		menuBar.add(fileMenu);
		// Add a group marker indicating where action set menus will appear.
		menuBar.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
		menuBar.add(helpMenu);
		
		// File
		fileMenu.add(newWindowAction);
		fileMenu.add(new Separator());
		fileMenu.add(openFileAction);
		fileMenu.add(collectDataAction);
		fileMenu.add(new Separator());
		fileMenu.add(saveFileAction);
		fileMenu.add(new Separator());
		fileMenu.add(exitAction);
		
		// Help
		helpMenu.add(aboutAction);
	}
	
	/**
	 * Fills the coolbar with actions.
	 * 
	 * @param coolBar
	 *          CoolBar manager
	 */
	@Override
	protected void fillCoolBar(ICoolBarManager coolBar) {
		IToolBarManager toolbar = new ToolBarManager(SWT.FLAT | SWT.RIGHT);
		coolBar.add(new ToolBarContributionItem(toolbar, "main"));
		toolbar.add(collectDataAction);
		toolbar.add(openFileAction);
		toolbar.add(saveFileAction);
	}
}
