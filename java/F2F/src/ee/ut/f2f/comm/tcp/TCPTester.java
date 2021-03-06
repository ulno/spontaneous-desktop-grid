package ee.ut.f2f.comm.tcp;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

import ee.ut.f2f.activity.Activity;
import ee.ut.f2f.activity.ActivityEvent;
import ee.ut.f2f.activity.ActivityManager;
import ee.ut.f2f.core.CommunicationFailedException;
import ee.ut.f2f.core.F2FComputing;
import ee.ut.f2f.core.F2FMessageListener;
import ee.ut.f2f.core.F2FPeer;
import ee.ut.f2f.util.logging.Logger;

class TCPTester extends Thread implements Activity, F2FMessageListener
{
	final private static Logger log = Logger.getLogger(TCPTester.class);
	
	private enum Status
	{
		INIT,
		START
	}
	private Status status = null;
	
	private F2FPeer remotePeer = null;
	
	/**
	 * Constructs new TCP connection tester thread.
	 * 
	 * @param peer The remote peer where to connect.
	 */
	TCPTester(F2FPeer peer)
	{
		super("TCPTester [" + peer.getDisplayName() + "]");
		remotePeer = peer;
		status = Status.INIT;
	}

	private Collection<InetSocketAddress> remoteServerSockets = null; 
	private ArrayList<Integer> remoteResults = new ArrayList<Integer>();
	public void messageReceived(Object message, F2FPeer sender)
	{
		if (sender.equals(remotePeer))
		{
			if (message instanceof TCPTestMessage)
				receivedTCPTestMessage((TCPTestMessage)message);
			else
				log.warn("TCPTester.messageRecieved() handles only TCPTestMessage");
		}
	}
	
	private void receivedTCPTestMessage(TCPTestMessage msg)
	{
		if (msg.type == TCPTestMessage.Type.INIT)
				status = Status.START;
		else if (msg.type == TCPTestMessage.Type.ADDRESSES)
			remoteServerSockets = msg.addresses;
		else if (msg.type == TCPTestMessage.Type.RESULT)
			remoteResults.add(msg.result);
	}

	private InetSocketAddress usedAddress = null;
	public void run()
	{
		// do not start TCP tests before SocketComm providers are initialized
		if (!TCPCommInitiator.isInitialized()) return;
		
		F2FComputing.addMessageListener(TCPTestMessage.class, this);
		// just for information catch any exceptions that may occur
		try
		{			
			testProcess();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			log.warn(getActivityName() + e.getMessage());
			ActivityManager.getDefault().emitEvent(new ActivityEvent(this,ActivityEvent.Type.FAILED, e.getMessage()));
		}
		F2FComputing.removeMessageListener(TCPTestMessage.class, this);
	}
	
	private void testProcess() throws CommunicationFailedException, IOException
	{
		ActivityManager.getDefault().emitEvent(new ActivityEvent(this,ActivityEvent.Type.STARTED));
		log.debug(getActivityName() + "started");
		
		// make sure that other peer has started the test too
		ActivityManager.getDefault().emitEvent(new ActivityEvent(this,ActivityEvent.Type.CHANGED, "waiting for init"));
		Thread thread = new Thread()
		{
			public void run()
			{
				for (int i = 0; i < 600; i++)
				{
					try {
						remotePeer.sendMessage(new TCPTestMessage());
						if (status != Status.INIT) return;
						Thread.sleep(1000);
					} catch (Exception e) {}
				}
			}
		};
		thread.start();
		while (true)
		{
			try {
				thread.join();
				break;
			} catch (InterruptedException e) {}
		}
		if (status == Status.INIT)
		{
			log.error("timeout while waiting for init from remote TCP test thread");
			ActivityManager.getDefault().emitEvent(new ActivityEvent(this,ActivityEvent.Type.FAILED, "timeout while waiting for init from remote TCP test thread"));
			// stop INIT sender
			status = null;
			return;
		}
		
		// exchange the server sockets that peers are listening on
		remotePeer.sendMessage(
			new TCPTestMessage(TCPCommunicationProvider.getInstance().getServerSocketAddresses()));
		// wait at most 10 minutes for remote addresses
		ActivityManager.getDefault().emitEvent(new ActivityEvent(this,ActivityEvent.Type.CHANGED, "waiting for addresses"));
		for (int i = 0; i < 1200; i++)
		{
			if (remoteServerSockets != null) break;
			try
			{
				Thread.sleep(500);
			}
			catch (InterruptedException e) {}
		}
		if (remoteServerSockets == null)
		{
			log.error("remoteServerSockets == null");
			ActivityManager.getDefault().emitEvent(new ActivityEvent(this,ActivityEvent.Type.FAILED, "remoteServerSockets == null"));
			return;
		}
		ActivityManager.getDefault().emitEvent(new ActivityEvent(this,ActivityEvent.Type.CHANGED, "got addresses"));
		log.debug("got remote socket addresses: " + remoteServerSockets.size());
		
		// start threads that try to connect to the remote sockets ...
		Collection<TCPTestThread> testThreads = new ArrayList<TCPTestThread>();
		for (InetSocketAddress address: remoteServerSockets)
			testThreads.add(new TCPTestThread(address));
		for (TCPTestThread test: testThreads)
			test.start();
		ActivityManager.getDefault().emitEvent(new ActivityEvent(this,ActivityEvent.Type.CHANGED, "started test threads"));
		// ... and wait until the first of them exits, max 10 min
		for (int i = 0; i < 1200; i++)
		{
            boolean allTestsStopped = true;
            for (TCPTestThread test: testThreads)
            {
                if (test.getState() != Thread.State.TERMINATED)
                {
                    allTestsStopped = false;
                    break;
                }
            }
            
			if (usedAddress != null || allTestsStopped) break;
			try
			{
				Thread.sleep(500);
			}
			catch (InterruptedException e) {}
		}
		ActivityManager.getDefault().emitEvent(new ActivityEvent(this,ActivityEvent.Type.CHANGED, "tests ended: " + (usedAddress == null ? "fail" : "success")));
		
		// exchange the results
		// and a random number that will be used to select the final connection if
		// if both sides made a successful test
		Integer localResult;// 0 means that test(s) failed
		Integer remoteResult;
		Random random = new Random(F2FComputing.getLocalPeer().getID().getLeastSignificantBits()+System.currentTimeMillis());
		for (int r = 0; ; r++)//this is repeated until peers generate different random numbers
		{
			//if (r == 0) localResult = 5;
			//else
			//{
			if (usedAddress != null)
			{
				localResult = random.nextInt();
				while (localResult.intValue() == 0)
					localResult = random.nextInt();
			} else localResult = 0;
			//}//test case
			ActivityManager.getDefault().emitEvent(new ActivityEvent(this,ActivityEvent.Type.CHANGED, "local result is " + localResult));
			remotePeer.sendMessage(new TCPTestMessage(localResult));
			ActivityManager.getDefault().emitEvent(new ActivityEvent(this,ActivityEvent.Type.CHANGED, "waiting remote result"));
			for (int i = 0; i < 1200; i++)
			{
				if (remoteResults.size() > r) break;
				try
				{
					Thread.sleep(500);
				}
				catch (InterruptedException e) {}
			}
			if (remoteResults.size() <= r)
			{
				log.error("remoteResult.size() ("+remoteResults.size()+") <= r ("+r+")");
				ActivityManager.getDefault().emitEvent(new ActivityEvent(this,ActivityEvent.Type.FAILED, "remoteResult.size() ("+remoteResults.size()+") <= r ("+r+")"));
				return;
			}
			remoteResult = remoteResults.get(r);
			ActivityManager.getDefault().emitEvent(new ActivityEvent(this,ActivityEvent.Type.CHANGED, "remote result is " + remoteResult));
			
			// end the test if local tests failed
			if (localResult.intValue() == 0) break;
	
			// end the test if local and remote results are different
			if (!localResult.equals(remoteResult)) break;
			
			ActivityManager.getDefault().emitEvent(new ActivityEvent(this,ActivityEvent.Type.CHANGED, "generate new results"));
		}
		
		// now we know the results
		if (localResult.intValue() == 0 && remoteResult.intValue() == 0)
		{
			ActivityManager.getDefault().emitEvent(new ActivityEvent(this,ActivityEvent.Type.FINISHED, "no TCP connection"));
			return;
		}
		if (remoteResult.intValue() != 0 && (localResult.intValue() == 0 || remoteResult.intValue() > localResult.intValue()))
		{
			ActivityManager.getDefault().emitEvent(new ActivityEvent(this,ActivityEvent.Type.FINISHED, "remote peer creates connection"));
			return;
		}
		
		// create the connection
		ActivityManager.getDefault().emitEvent(new ActivityEvent(this,ActivityEvent.Type.CHANGED, "local peer creates connection"));
		TCPCommunicationProvider.getInstance().addFriend(remotePeer.getID(), usedAddress, true);
		ActivityManager.getDefault().emitEvent(new ActivityEvent(this,ActivityEvent.Type.FINISHED, "TCP connection created"));
	}
	
	public String getActivityName()
	{
		return getName();
	}
	public Activity getParentActivity()
	{
		return null;
	}
	
	public String toString()
	{
		return getActivityName() + " status [" + status + "]";
	}
	
	private class TCPTestThread extends Thread implements Activity
	{
		private InetSocketAddress address;
		private TCPTestThread(InetSocketAddress address)
		{
			this.address = address;
		}
		
		public void run()
		{
			ActivityManager.getDefault().emitEvent(new ActivityEvent(this,ActivityEvent.Type.STARTED));
			try
			{
				testProcess();
			}
			catch (Exception e)
			{
				ActivityManager.getDefault().emitEvent(new ActivityEvent(this,ActivityEvent.Type.FAILED, "error: " + e.getMessage()));
				return;
			}
			
			// the first suitable address is saved
			if (usedAddress == null)
			{
				synchronized (TCPTester.this)
				{
					if (usedAddress == null)
					{
						usedAddress = address;
						log.debug(getActivityName() + " was first to establish TCP connection");
					}
				}
			}
			ActivityManager.getDefault().emitEvent(new ActivityEvent(this,ActivityEvent.Type.FINISHED));
		}
		
		private void testProcess() throws Exception
		{
			// try to connect to the remote address
			Socket outSocket = new Socket(address.getAddress(), address.getPort());
			ObjectOutput oo = new ObjectOutputStream(outSocket.getOutputStream());
			ObjectInput oi = new ObjectInputStream(outSocket.getInputStream());
			// send test packet
			TCPTestPacket testPacket = new TCPTestPacket(address); 
			oo.writeObject(testPacket);
			Object message = oi.readObject();
			if (message instanceof TCPTestPacket)
			{
				TCPTestPacket reply = (TCPTestPacket) message;
				if (testPacket.address.equals(reply.address)) return;
			}
			throw new Exception("received wrong object");
		}

		public String getActivityName()
		{
			return "TCPTestThread [" + address.getAddress().getHostAddress() + ":" + address.getPort() +"]";
		}

		public Activity getParentActivity()
		{
			return TCPTester.this;
		}
	}
}

class TCPTestMessage implements Serializable
{
	private static final long serialVersionUID = 1739475405217757980L;
	enum Type
	{
		INIT,
		ADDRESSES,
		RESULT
	}
	
	Type type = null;
	TCPTestMessage()
	{
		type = Type.INIT;
	}
	
	Collection<InetSocketAddress> addresses = null;
	TCPTestMessage(Collection<InetSocketAddress> addresses)
	{
		type = Type.ADDRESSES;
		this.addresses = addresses;
	}
	Integer result = null;
	TCPTestMessage(Integer result)
	{
		type = Type.RESULT;
		this.result = result;
	}
	
	public String toString()
	{
		String s = "TCPTestMessage ";
		if (type == Type.INIT) s += "INIT";
		else if (type == Type.ADDRESSES) s += "ADDRESSES";
		else if (type == Type.RESULT) s += "RESULT " + result;
		return s;
	}
}

class TCPTestPacket implements Serializable
{
	private static final long serialVersionUID = 5831574908971237295L;
	final InetSocketAddress address;
	TCPTestPacket(InetSocketAddress address)
	{
		this.address = address;
	}
}