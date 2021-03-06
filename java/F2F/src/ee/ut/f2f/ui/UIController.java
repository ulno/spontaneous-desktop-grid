package ee.ut.f2f.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.JXTreeTable;
import org.jdesktop.swingx.decorator.ColorHighlighter;

import ee.ut.f2f.activity.ActivityEvent;
import ee.ut.f2f.activity.ActivityManager;
import ee.ut.f2f.core.F2FComputing;
import ee.ut.f2f.core.F2FMessageListener;
import ee.ut.f2f.core.F2FPeer;
import ee.ut.f2f.core.PeerPresenceListener;
import ee.ut.f2f.ui.log.LogHandler;
import ee.ut.f2f.ui.log.LogHighlighter;
import ee.ut.f2f.ui.log.LogTableModel;
import ee.ut.f2f.ui.model.ActivityInfoNewRowsPredicate;
import ee.ut.f2f.ui.model.ActivityInfoSelectionListener;
import ee.ut.f2f.ui.model.ActivityInfoTableModel;
import ee.ut.f2f.ui.model.FriendModel;
import ee.ut.f2f.util.F2FDebug;
import ee.ut.f2f.util.F2FProperties;
import ee.ut.f2f.util.logging.Logger;

public class UIController implements PeerPresenceListener, F2FMessageListener
{
	private static final Logger logger = Logger.getLogger(UIController.class);	
	
	private JFrame frame = null;
	private JPanel mainPanel = null;
	private JPanel friendsPanel = null;
	private JList friendsList = null;
	private FriendModel<F2FPeer> friendModel = null;
	//private JTextArea console = null;
	
	private JButton createChatButton;
	/*
	//NAT/Traversal panel
	private JPanel traversalPanel = null;
	private JTable stunInfoTable = null;
	private StunInfoTableModel stunInfoTableModel = null;
	private JTextArea natLogArea = null;
	private JButton initButton = null;	
	*/
	private JMenu optionsMenu;
	private JMenuBar generalMenuBar = null;
	private JMenu fileMenu = null;
	private JMenu viewMenu = null;
	//private JMenu helpMenu = null;
	private JCheckBoxMenuItem allowCPUMenuItem;
	
	//private JCheckBoxMenuItem showDebugMenuItem = null;
	//private JCheckBoxMenuItem showInfoMenuItem = null;
	//private JCheckBoxMenuItem showErrorMenuItem = null;
	//private JCheckBoxMenuItem autoScrollMenuItem = null;
	private JMenuItem showDebugWindowMenuItem = null;
	private JMenuItem exitMenuItem = null;
	
	private Map<String, GroupChatWindow> chats = new HashMap<String, GroupChatWindow>();
	
	//private boolean showDebug = true;
	//private boolean showInfo = true;
	//private boolean showError = true;
	//private boolean autoscroll = true;
	
	/**
	 *  The peers from whom the computation peers will be selected.
	 */
	private Collection<F2FPeer> selectFromPeers = new ArrayList<F2FPeer>();
	
	public UIController(String title)
	{		
		frame = new JFrame(title);
		
		mainPanel = new JPanel(new BorderLayout());
		frame.setContentPane(mainPanel);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(800, 700);
		mainPanel.setPreferredSize(frame.getSize());
		
		friendsPanel = new JPanel();
		friendsPanel.setLayout(new BoxLayout(friendsPanel, BoxLayout.PAGE_AXIS));
		friendsPanel.setBorder(BorderFactory.createTitledBorder("Friends"));
		friendsPanel.setPreferredSize(new Dimension(200, 300+200));
		mainPanel.add(friendsPanel, BorderLayout.WEST);
		
		friendModel = new FriendModel<F2FPeer>();
		friendsList = new JList(friendModel);
		friendsList.addListSelectionListener(new FriendsListListener());

		friendsList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		friendsList.setLayoutOrientation(JList.VERTICAL);
		JScrollPane listScroller = new JScrollPane(friendsList);
		friendsPanel.add(listScroller);

		createChatButton = new JButton("Create chat");
		createChatButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				createChat(null, true);
			}
		});
		createChatButton.setEnabled(false);
		
		//friendsPanel.add(createChatButton);
			
		JPanel consolePanel = new JPanel();
		consolePanel.setLayout(new GridLayout(1,1));
		//consolePanel.setBorder(BorderFactory.createEmptyBorder());
		consolePanel.setPreferredSize(new Dimension(570, 300+200));
		//layout.putConstraint(SpringLayout.WEST, consolePanel, 5, SpringLayout.EAST, friendsPanel);
		//layout.putConstraint(SpringLayout.NORTH, consolePanel, 0, SpringLayout.NORTH, friendsPanel);
		mainPanel.add(consolePanel);
		
		JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP);
		
		//console = new JTextArea();
		//JScrollPane consoleScroller = new JScrollPane(console);

		//tabs.addTab("Information", consoleScroller);
		consolePanel.add(tabs);
		
		/*///////////////////////////
		//NAT/Traversal Panel
		
		traversalPanel = new JPanel();
		traversalPanel.setLayout(new GridLayout(3,1));
		traversalPanel.setPreferredSize(new Dimension(770, 0));
		
		stunInfoTableModel = new StunInfoTableModel();
			
		stunInfoTable = new JTable(stunInfoTableModel);
		stunInfoTable.setAutoscrolls(true);
		stunInfoTable.setEnabled(false);
		stunInfoTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		
		for (int i = 0; i < stunInfoTableModel.getColumnCount(); i++)
			stunInfoTable.getColumnModel().getColumn(i).setPreferredWidth(StunInfoTableModel.widths[i]);
		
		JScrollPane stunInfoTableScrollPane = new JScrollPane(stunInfoTable);
		stunInfoTableScrollPane.setAutoscrolls(true);
		stunInfoTableScrollPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK), "STUN Info"));
		
		
		//NAT Log
		natLogArea = new JTextArea();
		natLogArea.setEditable(false);
		
		JScrollPane natLogAreaScrollPane = new JScrollPane(natLogArea);
		natLogAreaScrollPane.setAutoscrolls(true);
		natLogAreaScrollPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK), "Traversal Log"));
		
		
		initButton = new JButton("Refresh stun info");
		initButton.addActionListener(
				new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{
						F2FPeer peer = (F2FPeer)friendsList.getSelectedValue();
						if (peer != null)
							peer.updateSTUNInfo();
					}
				}
		);
		JPanel natButtonPanel = new JPanel(new FlowLayout());
		natButtonPanel.add(initButton);
		//natButtonPanel.add(testButton);
		//natButtonPanel.add(testButton2);
	
		traversalPanel.add(stunInfoTableScrollPane);
		traversalPanel.add(natButtonPanel);
		traversalPanel.add(natLogAreaScrollPane);
		tabs.add("NAT Traversal", traversalPanel);
		
		//End of traversal panel
		/////////////////////////////*/
		
		// Activity model
		ActivityInfoTableModel activityInfoTableModel = new ActivityInfoTableModel();
		ActivityManager.getDefault().addListener(
				ActivityEvent.Type.values(), activityInfoTableModel);
		JXTreeTable activityInfoTable = new JXTreeTable(activityInfoTableModel);
		activityInfoTable.setAutoscrolls(true);
		activityInfoTable.addHighlighter(new ColorHighlighter(new Color(1f,1f,.7f),
				Color.BLACK, new ActivityInfoNewRowsPredicate(activityInfoTableModel)));
		activityInfoTable.addTreeSelectionListener(new ActivityInfoSelectionListener(
				activityInfoTableModel));
				
		tabs.addTab("F2F activities", new JScrollPane(activityInfoTable));
		
		// log panel
		LogHandler logHandler = LogHandler.getInstance();
		logHandler.setLevel(java.util.logging.Level.FINE);
		java.util.logging.Logger.getLogger("ee.ut.f2f").addHandler(logHandler);
		if(logHandler != null) {
			LogTableModel tableModel = new LogTableModel();
			JXTable logTable = new JXTable(tableModel);
			logTable.addHighlighter(new LogHighlighter());
			logHandler.setTableModel(tableModel);
			tabs.addTab("Logs", new JScrollPane(logTable));
		}
		
		// tasks panel
		TasksTableModel tasksTableModel = new TasksTableModel();
		JXTable tasksTable = new JXTable(tasksTableModel);
		tabs.addTab("Tasks", new JScrollPane(tasksTable));
		StopTaskButton stopButton = new StopTaskButton(tasksTableModel);
		TaskProgress taskProgress = new TaskProgress(tasksTableModel);
		tasksTable.setDefaultRenderer(StopTaskButton.class, stopButton);
		tasksTable.setDefaultRenderer(TaskProgress.class, taskProgress);
		//tasksTable.getColumnModel().getColumn(3).setCellRenderer(tasksTableModel.stopButton);
		tasksTable.setDefaultEditor(StopTaskButton.class, stopButton.editor);
		//tasksTable.getColumnModel().getColumn(3).setCellEditor(tasksTableModel.stopButton.editor);
		F2FComputing.addTaskListener(tasksTableModel);
		
		generalMenuBar = new JMenuBar();
		frame.setJMenuBar(generalMenuBar);

		fileMenu = new JMenu("File");
		exitMenuItem = new JMenuItem("Exit");
		viewMenu = new JMenu("View");
		//showDebugMenuItem = new JCheckBoxMenuItem("Show debug messages", true);
		//showInfoMenuItem = new JCheckBoxMenuItem("Show info messages", true);
		//showErrorMenuItem = new JCheckBoxMenuItem("Show error messages", true);
		//autoScrollMenuItem = new JCheckBoxMenuItem("AutoScroll console", true);
		showDebugWindowMenuItem = new JMenuItem("Show Debug window");
		
		viewMenu.add(showDebugWindowMenuItem);
		//viewMenu.add(showDebugMenuItem);
		//viewMenu.add(showInfoMenuItem);
		//viewMenu.add(showErrorMenuItem);
		viewMenu.addSeparator();
		//viewMenu.add(autoScrollMenuItem);
		fileMenu.add(exitMenuItem);

		optionsMenu = new JMenu("Options");
		allowCPUMenuItem = new JCheckBoxMenuItem("Allow All Friends To Use My PC", F2FProperties.getF2FProperties().getAllowCPU());
		F2FComputing.allowAllFriendsToUseMyPC(allowCPUMenuItem.getState());
		optionsMenu.add(allowCPUMenuItem);
		allowCPUMenuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				F2FComputing.allowAllFriendsToUseMyPC(allowCPUMenuItem.getState());
			}
		});
		
		//helpMenu = new JMenu("Help");

		generalMenuBar.add(fileMenu);
		generalMenuBar.add(viewMenu);
		generalMenuBar.add(optionsMenu);
		//generalMenuBar.add(helpMenu);
		
		exitMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				frame.dispose();
				System.exit(0);
			}
		});
		
		/*showDebugMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showDebug = showDebugMenuItem.isSelected();
			}
		});
		
		showInfoMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showInfo = showInfoMenuItem.isSelected();
			}
		});
		
		showErrorMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showError = showErrorMenuItem.isSelected();
			}
		});
		
		autoScrollMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				autoscroll = autoScrollMenuItem.isSelected();
				if (autoscroll) {
					console.setCaretPosition(console.getText().length()-1);
				}
			}
		});*/
		
		showDebugWindowMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				F2FDebug.show(true);
			}
		});
		
		F2FComputing.addPeerPresenceListener(this);
		synchronized (friendModel)
		{
			Collection<F2FPeer> peersGUI = friendModel.getPeers();
			Collection<F2FPeer> peersF2F = F2FComputing.getPeers();
			if (peersF2F != null)
				for (F2FPeer peer: peersF2F)
					if (!peersGUI.contains(peer))
						friendModel.add(peer);
		}
		F2FComputing.addMessageListener(ChatMessage.class, this);
		
		frame.setVisible(true);
	}

	public FriendModel<F2FPeer> getFriendModel() {
		return friendModel;
	}

	/**
	 * Prints info message to the console window
	 * @param msg
	 */
	/*public void info(String msg) {
		if(showInfo) {
			console("INFO", msg);
		}
	}*/

	/**
	 * Prints error message to the console window
	 * @param msg
	 */
	/*public void error(String msg) {
		if(showError) {
			console("ERROR", msg);
		}
	}*/
	
	/**
	 * Prints debug message to the console window
	 * @param msg
	 */
	/*public void debug(String msg) {
		if(showDebug) {
			console("DEBUG", msg);
		}
	}*/
	
	/*private void console(String prefix, String msg) {
		console.append(prefix+": "+msg);
		if (!msg.endsWith(String.valueOf('\n'))) {
			console.append(String.valueOf('\n'));
		}
		if (autoscroll) {
			console.setCaretPosition(console.getText().length());
		}
	}*/
	
	/**
	 * @return the collection of peers selected from the friends list.
	 */
	Collection<F2FPeer> getSelectedFriends()
	{
		Collection<F2FPeer> selectedPeers = new ArrayList<F2FPeer>();
		for (int i : friendsList.getSelectedIndices())
		{
			selectedPeers.add(friendModel.getElementAt(i));
		}
		return selectedPeers;
	}
	
	/**
	 * Listener for {@link #friendsPanel}.
	 */
	private class FriendsListListener implements ListSelectionListener
	{
		/**
		 * Update the {@link #selectFromPeers}.
		 */
		public void valueChanged(ListSelectionEvent listSelectionEvent)
		{
		
			// Return if this is one of multiple change events.
			if (listSelectionEvent.getValueIsAdjusting())
				return;
			
			// Process the event
			selectFromPeers.clear();
			selectFromPeers.addAll(getSelectedFriends());
			
			// Can't start chatting with just yourself
			if (selectFromPeers.size() == 0
					|| (selectFromPeers.size() == 1 && selectFromPeers
							.contains(F2FComputing.getLocalPeer()))) {
				createChatButton.setEnabled(false);
			}
			else {
				createChatButton.setEnabled(true);
			}
		}
	} // private class FriendsListListener
	/*
	public void writeNatLog(String msg){
		natLogArea.setText(natLogArea.getText() + "\n" + msg); 
	}

	public StunInfoTableModel getStunInfoTableModel() {
		return stunInfoTableModel;
	}

	public void setStunInfoTableModel(StunInfoTableModel stunInfoTableModel) {
		this.stunInfoTableModel = stunInfoTableModel;
	}
	*/
	private GroupChatWindow createChat(String chatId, boolean isCreator){
		GroupChatWindow chat = new GroupChatWindow(selectFromPeers, this, chatId, isCreator);
		chats.put(chat.getChatId(), chat);
		
		return chat;
	}
	
	public void killChat(String key) {
		chats.remove(key);
	}
	
	public void messageReceived(Object msg, F2FPeer sender)
	{
		if (msg instanceof ChatMessage);
		else
		{
			logger.warn("UIController.messageRecieved() handles only ChatMessage");
			return;
		}
		ChatMessage chatMsg = (ChatMessage) msg;
		String message = chatMsg.msg;
		//logger.info("Received: " + message + ", from: " + sender.getDisplayName());
		
		//Message structure: type;chatId;restOfMessage
		String type = GroupChatWindow.findMsgType(message);
		String chatId = GroupChatWindow.findChatId(message);
		String restOfMessage = GroupChatWindow.findRestOfMsg(message);
		
		GroupChatWindow chat = chats.get(chatId);
		if(chat==null) {
			chat = createChat(chatId, false);
			chats.put(chatId, chat);
		}
		
		if (type.equals(GroupChatWindow.CHAT_TYPE_CTRL)) {
			chat.chatControlReceived(restOfMessage, sender);
		}
		else if (type.equals(GroupChatWindow.CHAT_TYPE_MSG)) {
			chat.chatMessageReceived(restOfMessage, sender);
		}
		else if (type.equals(GroupChatWindow.CHAT_TYPE_END)) {
			// Removes you from chat
			chat.dispose();
			killChat(chatId);
		}
	}

	public void peerContacted(F2FPeer peer)
	{
		synchronized (friendModel)
		{
			if (!friendModel.getPeers().contains(peer))
				friendModel.add(peer);
		}
	}

	public void peerUnContacted(F2FPeer peer)
	{
		synchronized (friendModel)
		{
			friendModel.remove(peer);
		}
	}
}
