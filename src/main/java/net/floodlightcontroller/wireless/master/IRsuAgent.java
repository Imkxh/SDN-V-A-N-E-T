package net.floodlightcontroller.wireless.master;

import java.net.InetAddress;

public interface IRsuAgent {

	
	public int init (InetAddress host);
	
	public void setLastHeard (long t);
	
	public long getLastHeard (); 
	
	public InetAddress getIpAddress ();
	
	public boolean getisReboot();
	
	public void addObuDistanceMsg(String msg);
	
	public void addObuPowerMsg(String msg);
	
	public void sendText();
	
}
