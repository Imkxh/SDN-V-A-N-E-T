package net.floodlightcontroller.wireless.master;

import java.util.Set;
import java.net.InetAddress;
import java.util.Map;
import java.util.Set;

import org.projectfloodlight.openflow.types.MacAddress;

import net.floodlightcontroller.wireless.master.WirelessEventSubscription.EventType;

public interface ICarAppInterface {

	Set<CarObu> getObus();
	
	void registerSubscription (WirelessEventSubscription wes, NotificationCallback cb);

	void handoffObuDistance (InetAddress rsuIpAddr, MacAddress obuMac1, MacAddress obuMac2);

	void handoffObuPowerControl(InetAddress rsuIpAddr);

	void handOffObuDistance(InetAddress rsuIpAddr);
	
}
