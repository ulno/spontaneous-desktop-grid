/**
 * 
 */
package ee.ut.f2f.comm.tcp;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.UUID;

import ee.ut.f2f.activity.Activity;
import ee.ut.f2f.activity.ActivityEvent;
import ee.ut.f2f.activity.ActivityManager;
import ee.ut.f2f.comm.BlockingMessageSender;
import ee.ut.f2f.core.CommunicationFailedException;
import ee.ut.f2f.core.JobCustomObjectInputStream;
import ee.ut.f2f.core.F2FComputing;
import ee.ut.f2f.util.logging.Logger;

class TCPPeer extends BlockingMessageSender implements Activity
{
	final private static Logger log = Logger.getLogger(TCPPeer.class);
	
	
	private TCPCommunicationProvider scProvider = null;
	public TCPCommunicationProvider getCommunicationLayer()
	{
		return this.scProvider;
	}
	
	protected InetSocketAddress socketAddress = null;
	private Socket outSocket = null;
	private ObjectOutput oo = null;
	private ObjectInput oi = null;
	private UUID id = null;
	
	TCPPeer(UUID id, TCPCommunicationProvider layer, InetSocketAddress socketAddress, boolean bIntroduce) throws IOException
	{
		this.id = id;
		this.scProvider = layer;
		this.socketAddress = socketAddress;
		if (bIntroduce) getOo();
	}

	public synchronized void sendMessage(Object message)
			throws CommunicationFailedException
	{
		try
		{
			getOo().writeObject(message);
		}
		catch (IOException e)
		{
			throw new CommunicationFailedException(e);
		}
	}
	
	void setOo(ObjectOutput oo)
	{
		this.oo = oo;
	}

	private ObjectOutput getOo() throws IOException 
	{
		if(oo == null)
		{
			oo = new ObjectOutputStream(getOutSocket().getOutputStream());
			// Writing peer ID into the output stream for the other side to initialize the connection.
			UUID uid = F2FComputing.getLocalPeer().getID();
			oo.writeObject(uid);
			//Client starting listening server respond
			oi = new JobCustomObjectInputStream(outSocket.getInputStream());
			runSocketThread();
		}
		return oo;
	}
	
	private Socket getOutSocket() throws IOException 
	{
		if(outSocket == null) 
		{
			outSocket = new Socket(socketAddress.getAddress(), socketAddress.getPort()); 
		}
		return outSocket;
	}

	void setOi(ObjectInput oi_)
	{
		this.oi = oi_;
		runSocketThread();
	}

	private void runSocketThread() {
		new Thread(new Runnable()
		{
			public void run()
			{
				try
				{
					ActivityManager.getDefault().emitEvent(new ActivityEvent(TCPPeer.this,
							ActivityEvent.Type.STARTED, "start receiving messages"));
					//log.debug(getActivityName() + " Remote socket [" + outSocket.getRemoteSocketAddress() + "]");
					//log.debug(getActivityName() + " Local Bind [" + outSocket.getLocalAddress().getHostAddress() + ":" + outSocket.getLocalPort() + "]");
					while(true)
					{
						try
						{
							Object message = oi.readObject();
							//log.debug("\t\tReceived message from id [" + getID() + "] ip [" +
							//		 outSocket.getRemoteSocketAddress() + ":" + "]"  + "'. Message: '" + message + "'.");
							messageReceived(message, id);
						}
						catch (ClassNotFoundException e)
						{
							log.debug("Error reading object from id [" + id + "] ip [" +
									 outSocket.getRemoteSocketAddress() + "]" + e);
						}
					}
				}
				catch (Exception e){e.printStackTrace();}
				try
				{
					if (oi != null) oi.close();
					oi = null;
					if (oo != null) oo.close();
					oo = null;
					if (outSocket != null) outSocket.close();
					outSocket = null;
				} catch (Exception e) {}
				log.debug("Stopping listening to Peer id [" + id + "]");
				scProvider.removeFriend(id);
				ActivityManager.getDefault().emitEvent(new ActivityEvent(TCPPeer.this,
						ActivityEvent.Type.FINISHED, "connection closed"));
			}
		}).start();
	}
	
	public String toString() 
	{
		return id.toString();
	}

	public String getActivityName()
	{
		return "TCP conn to " + 
			(F2FComputing.getPeer(id) != null? F2FComputing.getPeer(id).getDisplayName(): id);
	}

	public Activity getParentActivity()
	{
		return scProvider;
	}
}
