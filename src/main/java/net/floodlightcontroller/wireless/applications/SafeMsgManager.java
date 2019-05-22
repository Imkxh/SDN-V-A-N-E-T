package net.floodlightcontroller.wireless.applications;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.projectfloodlight.openflow.types.MacAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.floodlightcontroller.wireless.master.CarApplication;
import net.floodlightcontroller.wireless.master.NotificationCallback;
import net.floodlightcontroller.wireless.master.WirelessEventSubscription;
import net.floodlightcontroller.wireless.master.WirelessEventSubscription.EventType;
import net.floodlightcontroller.wireless.master.WirelessEventSubscription.SubType;

public class SafeMsgManager extends CarApplication {

	protected static Logger log = LoggerFactory.getLogger(SafeMsgManager.class);
	
	
	public SafeMsgManager() {
		
	}
	
	private void init () {
		WirelessEventSubscription sub = new WirelessEventSubscription(
				SafeMsgManager.class.getName(), SubType.APAGENT, EventType.SAFA_HANDLE);
		NotificationCallback cb = new NotificationCallback() {
			@Override
			public void handle(EventType type, String msg) {
				handleMsg(msg);
			}
		};
		registerSubscription(sub, cb);
	}
	
	@Override
	public void run() {
		init ();
	}
	
	private void handleMsg(String msg) {
		
		//System.out.println("123___"+msg);
		String[] fields = msg.split(" ");
		
		InetAddress agentAddr = null;
		try {
			agentAddr = InetAddress.getByName(fields[0]);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		MacAddress obuAddr1 = MacAddress.of(fields[1]);
		MacAddress obuAddr2 = MacAddress.of(fields[2]);
		
		//handoffObuDistance(agentAddr,obuAddr1,obuAddr2);//安全消息控制(不可行，逻辑错误)
		handOffObuDistance(agentAddr);
		
		handoffObuPowerControl(agentAddr);//功率控制
		
	}
}
