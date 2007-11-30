package ee.ut.f2f.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

import ee.ut.f2f.comm.CommunicationFailedException;
import ee.ut.f2f.core.F2FPeer;
import ee.ut.f2f.ui.model.FriendModel;
import ee.ut.f2f.util.F2FMessage;

/**
 * @author Jaan Neljandik
 * @created 19.11.2007
 */
public class GroupChatWindow extends JFrame {
	private static final long serialVersionUID = 1L;
	
	public static final String MESSAGE_STRUCTURE = "F2F;key;msg"; 
	
	private JTextField messageField = null;
	private JPanel messagingPanel = null;
	private JButton sendMessageButton = null;
	private JTextArea receievedMessagesTextArea = null;	
	private JList memberList = null;
	private FriendModel memberModel;
	private UIController mainWindow;
	private JButton addButton;
	private JButton removeButton;
	private String chatId;
	
	public GroupChatWindow(Collection<F2FPeer> members, UIController mainWindow, String chatId){
		
		this.mainWindow = mainWindow;
		this.setSize(new Dimension(400, 300));
		this.setLocationRelativeTo(null);
		
		// This id should never contain strings or delimiters used in message structure. 
		// Refer to MESSAGE_STRUCTURE
		this.chatId = chatId != null ? chatId : String.valueOf(System.currentTimeMillis());
		
		// Messaging panel elements.
		messagingPanel = new JPanel();
		messagingPanel.setLayout(new BorderLayout());
		messagingPanel.setPreferredSize(new Dimension(400, 300));
		
		//TODO: Layout	 
		// 	SpringLayout layout = new SpringLayout();
		//	layout.putConstraint(SpringLayout.NORTH, messagingPanel, 5, SpringLayout.SOUTH, friendsPanel);
		//	layout.putConstraint(SpringLayout.WEST, messagingPanel, 5, SpringLayout.WEST, mainPanel);
		
		receievedMessagesTextArea = new JTextArea();
		receievedMessagesTextArea.setEditable(false);
		
		JScrollPane receievedMessagesTextAreaScrollPane = new JScrollPane(receievedMessagesTextArea); 
		messageField = new JTextField();
		
		memberModel = new FriendModel();
		memberList = new JList(memberModel);
		for (F2FPeer peer : members) {
			memberModel.add(peer);
		}
		
		memberList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		memberList.setLayoutOrientation(JList.VERTICAL);
		JScrollPane listScroller = new JScrollPane(memberList);
		listScroller.setPreferredSize(new Dimension(150, 500));
		messagingPanel.add(listScroller, BorderLayout.EAST);
		
		
		addButton = new JButton("Add...");
		addButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//TODO: Add people
			}
		});
		
		sendMessageButton = new JButton("Send");
		sendMessageButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onSendMessage();
			}
		});
		sendMessageButton.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent e) {}
			public void keyTyped(KeyEvent e) {}
			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					onSendMessage();
				}
			}			
		});
		
		removeButton = new JButton("Kick...");
		removeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//TODO: Remove button
			}
		});
		
		messagingPanel.add(receievedMessagesTextAreaScrollPane, BorderLayout.CENTER);		
		JPanel southPanel = new JPanel(new GridBagLayout());
		messagingPanel.add(southPanel, BorderLayout.SOUTH);
		
		GridBagConstraints c = new GridBagConstraints();
		
		//messagingPanelScrollPanel.setMinimumSize(new Dimension(300, 25));
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.8;
		c.weighty = 1.0;
		c.gridy = 0;
		
		southPanel.add(messageField, c);
		
		southPanel.add(sendMessageButton, c);
		
		c.gridx = 0;
		c.gridy = 1;		
		southPanel.add(addButton, c);
		
		c.weightx = 0.5;
		c.gridx = 1;		
		southPanel.add(removeButton, c);
		
		messageField.setPreferredSize(new Dimension(300, 20));
		sendMessageButton.setPreferredSize(new Dimension(80, 20));
		
		this.setContentPane(messagingPanel);	
		this.setVisible(true);		
	}
	
	public void writeMessage(String from, String msg) {
		receievedMessagesTextArea.setText(receievedMessagesTextArea.getText()+"\n"+from+": "+msg);
	}
	
	public void onPeopleAdd(Object[] selectedPeople) {
		
	}
	
	private void onSendMessage() {
		String messageText = MESSAGE_STRUCTURE;
		messageText.replaceAll("key", chatId);	
		messageText.replaceAll("msg", messageField.getText());		
		
		F2FMessage msg = new F2FMessage(F2FMessage.Type.CHAT, null, null, null, messageText);
		// get selected peers and send the message to them
		for (F2FPeer peer : ((FriendModel)memberList.getModel()).getPeers()) {
			try	{
				peer.sendMessage(msg);
			}
			catch (CommunicationFailedException cfe) {
				mainWindow.error("Sending message '"
						+ messageField.getText() + "' to the peer '"
						+ peer.getDisplayName() + "' failed with '"
						+ cfe.getMessage() + "'");
			}					
		}
		writeMessage("me", messageField.getText());
	}

	public String getChatId() {
		return chatId;
	}
	
	public static String findChatId(String message) {
		return message.split(";", 3)[1];
	}	
}
