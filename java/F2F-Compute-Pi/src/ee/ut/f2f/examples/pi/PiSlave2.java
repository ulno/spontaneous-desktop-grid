package ee.ut.f2f.examples.pi;

import java.io.Serializable;

import ee.ut.f2f.core.CommunicationFailedException;
import ee.ut.f2f.core.Task;
import ee.ut.f2f.core.TaskProxy;
import ee.ut.f2f.util.F2FDebug;

import org.uncommons.maths.random.MersenneTwisterRNG;

public class PiSlave2 extends Task implements Serializable
{
	private static final long serialVersionUID = 3877180695685895155L;

	AtomicLongVector computedPoints = new AtomicLongVector (0,0);
	
	long intervalms = 10000;
	void setInterval(long intervalms)
	{
		this.intervalms = intervalms;
	}
			
	public void runTask()
	{
		// get proxy of master task
		TaskProxy masterProxy = this.getTaskProxy(this.getJob().getMasterTaskID());
		if (masterProxy == null) throw new RuntimeException("Proxy of master task was not found!");
		
		// Start a thread which computes the points
		ComputePoints compTask = new ComputePoints ();
		compTask.start();
		
		// Send the computed points back to the master
		while (true)
		{
			// Wait
			try {
				Thread.sleep(intervalms);
			} catch (InterruptedException e) {}
			
			// Test if stop message has been sent
			if (isStopped()) break;
				
			F2FDebug.println("Stop-condition not met, sending back results. total: "
					+ computedPoints.getUnSyncTotal() + " positives: " 
					+ computedPoints.getUnSyncPositive() );
			// send results back to master
			try {
				masterProxy.sendMessage( computedPoints );
				// reset computiation thread
				computedPoints.set(0, 0);
			} catch (CommunicationFailedException e) {
				e.printStackTrace();
			}
		}
	}

	// end the calculations
	public void messageReceivedEvent(String remoteTaskID)
	{
		if (!remoteTaskID.equals(this.getJob().getMasterTaskID())) return;
		TaskProxy proxy = this.getTaskProxy(remoteTaskID);
		if (!proxy.hasMessage()) return;
		Long stopcondition = (Long) proxy.receiveMessage();
		if (stopcondition.intValue() == 0) stopTask();
		this.interrupt();
	}
	
	private class ComputePoints extends Thread
	{
		public void run()
		{
			MersenneTwisterRNG random = new MersenneTwisterRNG();
			while(!isStopped())
			{
				double x = random.nextDouble();
				double y = random.nextDouble();
				// check if the point in the quarter-cicle with radius 1
				if( x*x + y*y < 1.0 )
					computedPoints.positiveHit();
				else
					computedPoints.negativeHit();
			}
		}
	}
}
