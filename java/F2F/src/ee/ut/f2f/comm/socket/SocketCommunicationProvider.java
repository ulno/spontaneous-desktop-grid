/**
 * 
 */
package ee.ut.f2f.comm.socket;

import java.io.IOException;
import java.io.ObjectInput;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

import ee.ut.f2f.comm.CommunicationFailedException;
import ee.ut.f2f.comm.CommunicationInitException;
import ee.ut.f2f.comm.CommunicationProvider;
import ee.ut.f2f.util.CustomObjectInputStream;
import ee.ut.f2f.util.F2FDebug;

public class SocketCommunicationProvider implements CommunicationProvider
{
	//private final Logger LOG = LogManager.getLogger(SocketCommunicationLayer.class);

	/**
	 * Name that identifies Socket communication layer.
	 */
	private static final String SOCKET_LAYER_ID = "F2FSocketCommLayer";
	
	private Collection<SocketPeer> peers = new ArrayList<SocketPeer>();
	private SocketPeer localPeer;

	/**
	 * Creates the communication layer with fixed count of friends.
	 * @throws CommunicationInitException 
	 */
	public SocketCommunicationProvider(InetSocketAddress localPeerAddr, Collection<InetSocketAddress> friends) throws CommunicationInitException
	{
		// Create the list of friend-nodes only when passed. 
		if (friends!=null)
		{
			for (InetSocketAddress friend: friends)
			{
				peers.add(new SocketPeer(this, friend));
			}
		}
		this.localPeer = new SocketPeer(this, localPeerAddr);
		new Thread(new SocketListener(this.localPeer.socketAddress.getPort())).start();
	}

	/* (non-Javadoc)
	 * @see ee.ut.f2f.comm.CommunicationLayer#findPeerByID(java.lang.String)
	 */
	public SocketPeer findPeerByID(String sID) throws CommunicationFailedException {
		for(SocketPeer peer: peers)
		{
			if(sID.equals(peer.getID())) return peer;
		}
		return null;
	}

	SocketPeer getLocalPeer() {
		return localPeer;
	}

	public String getID()
	{
		return SOCKET_LAYER_ID;
	}
	
	private class SocketListener implements Runnable
	{
		/*
		 * This socket listenes for incoming connections from other peers.
		 */
		private ServerSocket serverSocket;
		
		SocketListener(int port) throws CommunicationInitException
		{
			try
			{
				serverSocket = new ServerSocket(port);
			}
			catch (IOException e)
			{
				throw new CommunicationInitException("Could not create server socket! ", e);
			}
		}
		
		public void run()
		{
			while(true) {
				try {
					// wait while someone tries to connect
					Socket socket = serverSocket.accept();
					ObjectInput oi = new CustomObjectInputStream(socket.getInputStream());
					
					// the first message has to be the ID of remote peer
					String remoteID = (String)oi.readObject();
					//TODO: inform Core that new peer is found
					// and accept the connection even if the peer is not in the peer list!
					F2FDebug.println("\t\tSearching for peer by ID '"+remoteID+"'");
					SocketPeer peer = (SocketPeer) findPeerByID(remoteID);
					if (peer != null)
					{
						// start listening for messages from the peer
						peer.setOi(oi);
						F2FDebug.println("\t\tAccepted remote connection from '"+remoteID+"'");
					}
					else
					{
						oi.close();
						socket.close();
						F2FDebug.println("\t\tUID '"+remoteID+"' is unknown. Closed the connection.");
					}
				} catch (Exception e) {
					F2FDebug.println("\t\tProblems creating the socket communication: " + e);
					e.printStackTrace();
				}
			}
		}
	}

	public boolean isLocalPeerID(String ID)
	{
		return localPeer.getID().equals(ID);
	}

	public String[] getLocalPeerIDs()
	{
		return new String[]{ localPeer.getID() };
	}

	public void sendMessage(UUID destinationPeer, Object message) {
		// TODO Auto-generated method stub
		
	}
}