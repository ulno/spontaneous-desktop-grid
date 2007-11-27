package ee.ut.f2f.util.nat.traversal;

import ee.ut.f2f.activity.Activity;
import ee.ut.f2f.activity.ActivityEvent;
import ee.ut.f2f.activity.ActivityManager;
import ee.ut.f2f.core.F2FComputing;
import ee.ut.f2f.ui.F2FComputingGUI;
import ee.ut.f2f.util.logging.Logger;

public class StunInfoClient extends Thread implements Activity {
	
	final private static Logger log = Logger.getLogger(StunInfoClient.class);
	
	public StunInfoClient() {
		this.setName("StunInfoClient");
	}
	
	@Override
	public void run(){
		ActivityManager.getDefault().emitEvent(new ActivityEvent(this, ActivityEvent.Type.STARTED));
		log.debug("Starting StunInfoClient Thread ...");
		while(true){
			StunInfo sinf = null;
			try {
				sinf = F2FComputingGUI.connectionManager.startNetworkDiscovery();
				String localId = F2FComputing.getLocalPeer().getID().toString();
				sinf.setId(localId);
				//ActivityManager.getDefault().emitEvent(new ActivityEvent(this, ActivityEvent.Type.FINISHED));
			} catch (Exception e) {
				log.error(e.getLocalizedMessage() + "Stopping StunInfoClient thread", e);
				ActivityManager.getDefault().emitEvent(new ActivityEvent(this, ActivityEvent.Type.FAILED));
				return;
			}
			log.debug("Loaded StunInfo for this machine [\n" + sinf.toString() + "]" );
			F2FComputingGUI.controller.getStunInfoTableModel().add(sinf);
			log.debug("StunInfoClient state : suspended");
			this.suspend();
			log.debug("StunInfoClient state : resumed");
		}
	}

	@Override
	public String getActivityName() {
		return "StunInfoClient";
	}

	@Override
	public Activity getParentActivity() {
		return null;
	}
}
