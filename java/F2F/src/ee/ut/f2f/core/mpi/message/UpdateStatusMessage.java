package ee.ut.f2f.core.mpi.message;

import java.io.Serializable;

public class UpdateStatusMessage extends BasicMessage implements Serializable{

	private static final long serialVersionUID = 2000035L;
	String mid;

	public UpdateStatusMessage(String mid) {
		this.mid = new String(mid);
	}

	public String getMID() {
		return mid;
	}
}
