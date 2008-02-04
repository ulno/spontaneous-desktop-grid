/**
 * 
 */
package ee.ut.f2f.comm.socket;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.UUID;

import ee.ut.f2f.activity.Activity;
import ee.ut.f2f.activity.ActivityEvent;
import ee.ut.f2f.activity.ActivityManager;
import ee.ut.f2f.comm.CommunicationFailedException;
import ee.ut.f2f.comm.CommunicationInitException;
import ee.ut.f2f.comm.CommunicationProvider;
import ee.ut.f2f.core.F2FComputing;
import ee.ut.f2f.util.CustomObjectInputStream;
import ee.ut.f2f.util.logging.Logger;

public class SocketCommunicationProvider implements CommunicationProvider, Activity
{
	private final Logger log = Logger.getLogger(SocketCommunicationProvider.class);

	/**
	 * Name that identifies Socket communication layer.
	 */
	private static final String SOCKET_LAYER_ID = "F2FSocketCommLayer";
	
	private Hashtable<UUID, SocketPeer> peers = new Hashtable<UUID, SocketPeer>();
	
	/**
	 * Creates the communication layer with fixed count of friends.
	 * @throws CommunicationInitException 
	 */
	private static SocketCommunicationProvider socketCommunicationProvider = null;
	private SocketCommunicationProvider()
	{
		ActivityEvent event = new ActivityEvent(this, ActivityEvent.Type.STARTED);
		ActivityManager.getDefault().emitEvent(event);
	}
	/*public SocketCommunicationProvider(InetSocketAddress localPeerAddr, Collection<InetSocketAddress> friends) throws CommunicationInitException
	{
		// Create the list of friend-nodes only when passed. 
		if (friends!=null)
		{
			for (InetSocketAddress friend: friends)
			{
				peers.add(new SocketPeer(this, friend));
			}
		}
	}*/

	public void addFriend(UUID id, InetSocketAddress friend, boolean bIntroduce) throws IOException
	{
		if(!peers.containsKey(id))
		{
			SocketPeer peer = new SocketPeer(id, this, friend, bIntroduce);
			peers.put(id, peer);
			F2FComputing.peerContacted(id, id.toString(), this);
		}
	}
	
	/* (non-Javadoc)
	 * @see ee.ut.f2f.comm.CommunicationLayer#findPeerByID(java.lang.String)
	 */
	public SocketPeer findPeerByID(UUID id)
	{
		if (peers.containsKey(id))
			return peers.get(id);
		return null;
	}

	public String getID()
	{
		return SOCKET_LAYER_ID;
	}
	
	private class SocketListener extends Thread implements Activity
	{
		/*
		 * This socket listenes for incoming connections from other peers.
		 */
		private ServerSocket serverSocket;
		private InetSocketAddress socketAddress = null;
		
		SocketListener(InetSocketAddress inetSoc) throws CommunicationInitException
		{
			try
			{
				serverSocket = new ServerSocket();
				for (int i = 1; i < 11; i++)
				{
					try
					{
						serverSocket.bind(inetSoc);
						break;
					}
					catch (BindException e)
					{
						log.warn("Unable to bind ServerSocket to  local [" + inetSoc.getAddress().getHostAddress() + ":" + inetSoc.getPort() + "]");
						if (i == 10) throw e;
						inetSoc = new InetSocketAddress(inetSoc.getAddress(), inetSoc.getPort() + i);
					}
				}
			}
			catch (IOException e)
			{
				log.error("Unable to bind ServerSocket to  local [" + inetSoc.getAddress().getHostAddress() + ":" + inetSoc.getPort() + "]" , e);
				throw new CommunicationInitException("SocketComm: Could not create server socket! " + e.getMessage(), e);
			}
			setName("SocketListener ["+ inetSoc.getAddress().getHostAddress() + ":" + inetSoc.getPort() + "]");
			socketAddress = inetSoc;
		}
		
		public void run()
		{
			ActivityEvent event;
			try
			{
				event = new ActivityEvent(this, ActivityEvent.Type.STARTED, 
						"Listening to incoming connections");
				ActivityManager.getDefault().emitEvent(event);
				
				// accept incoming connections from other peers
				acceptConnections();
				
				event = new ActivityEvent(this,	ActivityEvent.Type.FINISHED, 
						"Finished listening to incoming connections");
				ActivityManager.getDefault().emitEvent(event);
			}
			catch (RuntimeException e)
			{
				event = new ActivityEvent(this,	ActivityEvent.Type.FAILED, 
						e.toString());
				ActivityManager.getDefault().emitEvent(event);
				throw e;
			}
		}

		private void acceptConnections()
		{
			while(true)
			{
				try
				{
					// wait while someone tries to connect
					Socket socket = serverSocket.accept();
					ObjectInput oi = new CustomObjectInputStream(socket.getInputStream());
					ObjectOutput oo = new ObjectOutputStream(socket.getOutputStream());
					log.debug("\t\tAccepted socket from IP: '"+socket.getInetAddress().getHostAddress()+"' port: "+ socket.getPort());
					log.debug("\t\tBinded socket on local IP: '"+socket.getLocalAddress().getHostAddress()+"' port: "+ socket.getLocalPort());
					Object message = oi.readObject();
					if (message instanceof TCPTestPacket)
					{
						TCPTestPacket testPacket = (TCPTestPacket)message;
						if (testPacket.address.equals(socketAddress))
						{
							log.debug("received TCPTest packet, sending reply");
							oo.writeObject(testPacket);
						}
						else
						{
							log.warn("received TCPTest packet with wrong address");
						}
						oi.close();
						oo.close();
						socket.close();
						continue;
					}
					// the first message has to be the ID of remote peer
					UUID remoteID = (UUID)message;
					if (peers.containsKey(remoteID))
					{
						oi.close();
						oo.close();
						socket.close();
						log.warn("socket peer is already known");
						continue;
					}
					SocketPeer peer = new SocketPeer(remoteID, SocketCommunicationProvider.this, null, false);
					// start listening for messages from the peer
					peer.setOo(oo);
					peer.setOi(oi);
					peers.put(remoteID, peer);
					F2FComputing.peerContacted(remoteID, socket.getInetAddress().getHostAddress()+":"+ socket.getPort(), SocketCommunicationProvider.this);
					log.debug("\t\tAccepted remote connection from '"+remoteID+"'");
				}
				catch (Exception e)
				{
					log.warn("Problems creating the socket communication: " + e);
					e.printStackTrace();
				}
			}
		}

		public String getActivityName()
		{
			return getName();
		}

		public Activity getParentActivity()
		{
			return SocketCommunicationProvider.this;
		}
	}

	public void sendMessage(UUID destinationPeer, Object message) throws CommunicationFailedException
	{
		if (!peers.containsKey(destinationPeer))
			throw new CommunicationFailedException("socket peer wasn't found");
		peers.get(destinationPeer).sendMessage(message);
	}

	public String getActivityName()
	{
		return "SocketCommProvider";
	}

	public Activity getParentActivity()
	{
		return null;
	}

	public int getWeight()
	{
		return CommunicationProvider.SOCKET_COMM_WEIGHT;
	}

	public static SocketCommunicationProvider getInstance()
	{
		if (socketCommunicationProvider == null)
		{
			synchronized (SocketCommunicationProvider.class)
			{
				if (socketCommunicationProvider == null)
					socketCommunicationProvider = new SocketCommunicationProvider();
			}
		}
		return socketCommunicationProvider;
	}

	private Collection<InetSocketAddress> serverSockets = new ArrayList<InetSocketAddress>();
	Collection<InetSocketAddress> getServerSocketAddresses() { return serverSockets; } 
	public void addServerSocket(InetSocketAddress inetSoc) throws CommunicationInitException
	{
		SocketListener socketListener = new SocketListener(inetSoc);
		serverSockets.add(socketListener.socketAddress);
		
		//TODO: kill this thread if the communication provider is deleted/not used any more
		new Thread(socketListener).start();
	}

	
	private void addFriend(final InetSocketAddress address)
	{
		new Thread()
		{
			public void run()
			{
				try
				{
					Socket socket = new Socket(address.getAddress(), address.getPort());
					ObjectOutput oo = new ObjectOutputStream(socket.getOutputStream());
					ObjectInput oi = new ObjectInputStream(socket.getInputStream());
					// send local peer's ID
					oo.writeObject(F2FComputing.getLocalPeer().getID());
					UUID remoteID = (UUID)oi.readObject();
					if (peers.containsKey(remoteID))
					{
						oi.close();
						oo.close();
						socket.close();
						log.warn("socket peer is already known");
						return;
					}
					SocketPeer peer = new SocketPeer(remoteID, SocketCommunicationProvider.this, null, false);
					// start listening for messages from the peer
					peer.setOo(oo);
					peer.setOi(oi);
					peers.put(remoteID, peer);
					F2FComputing.peerContacted(remoteID, address.getAddress().getHostAddress() +":"+ address.getPort(), SocketCommunicationProvider.this);
					log.debug("Accepted remote connection from '"+remoteID+"'");
				}
				catch (Exception e)
				{
					log.warn("could not find Socket peer at " + address.getAddress().getHostAddress() +":"+ address.getPort());
				}
			}
		}.start();
	}
	public void addFriends(Collection<InetSocketAddress> friends)
	{
		for (InetSocketAddress address: friends)
			addFriend(address);
	}
}
