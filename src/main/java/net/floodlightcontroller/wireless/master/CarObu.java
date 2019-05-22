package net.floodlightcontroller.wireless.master;

import java.net.InetAddress;
import org.projectfloodlight.openflow.types.MacAddress;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import net.floodlightcontroller.wireless.web.ObuSerializer;

@JsonSerialize(using=ObuSerializer.class)
public class CarObu {
	private final MacAddress hwAddress;
	private ObuStatus status;
	private String location = null;
	private long lastHeard;
	
	public CarObu(MacAddress hwAddress) {
		super();
		this.hwAddress = hwAddress;
	}
	
	public MacAddress getMacAddress() {
		return hwAddress;
	}

	public ObuStatus getStatus() {
		return status;
	}

	public void setStatus(ObuStatus status) {
		this.status = status;
	}
	
	public long getLastHeard() {
		return lastHeard;
	}

	public void setLastHeard(long t) {
		this.lastHeard = t;
	}
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof CarObu))
			return false;
		if (obj == this)
			return true;
		CarObu that = (CarObu)obj;
		return (this.hwAddress.equals(that.hwAddress));
	}


}
