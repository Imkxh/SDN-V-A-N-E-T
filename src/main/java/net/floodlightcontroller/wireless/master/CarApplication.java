package net.floodlightcontroller.wireless.master;

import java.net.InetAddress;
import java.util.Map;
import java.util.Set;

import org.projectfloodlight.openflow.types.MacAddress;

import net.floodlightcontroller.wireless.master.WirelessEventSubscription.EventType;


public abstract class CarApplication implements Runnable{
	private ICarAppInterface CarAppInterface;
	
	/**
	 * Set the WirelessMaster to use
	 */
	final void setWirelessInterface (ICarAppInterface application) {
		CarAppInterface = application;
	}
	
	
	/**
	 * Needed to wrap OdinApplications into a thread, and is
	 * implemented by the specific application
	 */
	public abstract void run();
	
	/**
	 * Add a subscription for a particular event defined by oes. cb is
	 * defines the application specified callback to be invoked during
	 * notification. If the application plans to delete the subscription,
	 * later, the onus is upon it to keep track of the subscription
	 * id for removal later.
	 * 
	 * @param oes the susbcription
	 * @param cb the callback
	 */
	protected final void registerSubscription (WirelessEventSubscription wes, NotificationCallback cb){
		CarAppInterface.registerSubscription(wes, cb);
	}
	
	protected final void handoffObuDistance (InetAddress rsuIpAddr,MacAddress obuMac1,MacAddress obuMac2){
		CarAppInterface.handoffObuDistance(rsuIpAddr,obuMac1,obuMac2);
	}
	
	protected final void handOffObuDistance (InetAddress rsuIpAddr){
		CarAppInterface.handOffObuDistance(rsuIpAddr);
	}
	
	protected final void handoffObuPowerControl (InetAddress rsuIpAddr){
		CarAppInterface.handoffObuPowerControl(rsuIpAddr);
	}
	

}
