package net.floodlightcontroller.wireless.master;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import org.projectfloodlight.openflow.types.MacAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.floodlightcontroller.core.IFloodlightProviderService;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.internal.IOFSwitchService;
import net.floodlightcontroller.wireless.master.WirelessEventSubscription.EventType;

public class RsuManager {
protected static Logger log = LoggerFactory.getLogger(WirelessMaster.class);
	
	private final ConcurrentHashMap<InetAddress, IRsuAgent> rsuMap = 
			new ConcurrentHashMap<>();
	//private IOFSwitchService switchService;
	private WirelessMaster master;
    private final ObuManager obuManager;

    private final Timer failureDetectionTimer = new Timer();//故障检测定时器
    private int agentTimeout = 6000;//毫秒
    private int obuTimeout = 6000;
    
    
	protected RsuManager (WirelessMaster master, ObuManager obuManager) {
		this.master = master;
		this.obuManager = obuManager;
	}
	
	protected RsuManager (ObuManager obuManager) {
		this.obuManager = obuManager;
	}
	
	/**
	 * Removes an agent from the agent manager
	 *
	 * @param agentInetAddr
	 */
	protected void removeAgent(InetAddress agentInetAddr) {
		synchronized (this) {
			rsuMap.remove(agentInetAddr);
		}
	}
	
	protected boolean receiveRsuMsg(final MacAddress obuMac) {
		
		if (obuMac == null || obuManager.inObuMap (obuMac)) {//下次监听直接跳过
    		return false;
    	}
		
		//System.out.println("aaaaa");
		synchronized (this) {
			obuManager.addObu(obuMac);
			//System.out.println("bbbb");
			log.info("Adding obuMac to map: " + obuMac.toString());
			CarObu carobu = obuManager.getObu(obuMac);
			carobu.setLastHeard(System.currentTimeMillis());
			failureDetectionTimer.scheduleAtFixedRate(new ObuFailureDetectorTask(carobu), 1, obuTimeout/2);
		}			
		return true;	
	}
	
	
	
	/**
     * Handle a ping from an agent. If an agent was added to the
     * agent map, return true.
     *
     * @param agentAddr
     * @return true if an agent was added
     */
	/*protected boolean receivePing(final InetAddress agentAddr) {
		
	}*/
	protected void receiveTest(final InetAddress agentAddr,final String msg) {
		
		
		synchronized (this) {
			
			String properties[] = msg.split("#");
			
			/*if(!rsuMap.containsKey(agentAddr)) {
				
				IRsuAgent agent = new RsuAgent();
				rsuMap.put(agentAddr,agent);
				
			}*/
			
			
			if(!obuManager.inObuMap(MacAddress.of(properties[0]))) {
			//CarObu co = new CarObu(MacAddress.of(properties[0]));
			obuManager.addObu(MacAddress.of(properties[0]));
			}
		}
		
	}
	
	/**
	 * 建立与RSU之间的TCP连接
	 *
	 */
	protected boolean handleAddr(final InetAddress rsuAddr) {
		
		if( rsuAddr == null || isTracked (rsuAddr)) {
			return false;
		}
		
		synchronized (this) {
			
			if(isTracked(rsuAddr)) {
				return false;
			}
			
			IRsuAgent agent = new RsuAgent();
			//System.out.println("12332323");
			agent.init(rsuAddr);
			agent.setLastHeard(System.currentTimeMillis());
			
			//agent.sendText();//发送测试数据
			
			rsuMap.put(rsuAddr, agent);
			log.info("Adding RsuAgent to map: " + rsuAddr.getHostAddress());
			
			failureDetectionTimer.scheduleAtFixedRate(new AgentFailureDetectorTask(agent), 1, agentTimeout/2);
		}
		return true;
		
	}
	
	
	//RSU监听模块
	private class AgentFailureDetectorTask extends TimerTask {
		private final IRsuAgent agent;
		
		AgentFailureDetectorTask(final IRsuAgent agent){
			this.agent = agent;
		}
		
		@Override
		public void run() {
			if ((System.currentTimeMillis() - agent.getLastHeard()) >= agentTimeout) {
				log.error("RsuAgent: " + agent.getIpAddress().getHostAddress().toString() + " has timed out");
				
				if (master != null && !agent.getisReboot()) {
					/*
					 * 向外发送通知，AP超时，已经下线
					 */
					String ipAddress = agent.getIpAddress().getHostAddress();
					String reason = "timeout";
					
					//master.subscriptionNotify(EventType.AGENT_OFFLINE, ipAddress + " " + reason);
				}
				
				// Agent should now be cleared out
				removeAgent(agent.getIpAddress());
				this.cancel();
			}
			
		}
	}
	
	//OBU监听模块
	private class ObuFailureDetectorTask extends TimerTask {
		private final CarObu obu;
		
		ObuFailureDetectorTask(final CarObu obu){
			this.obu = obu;
		}
		
		@Override
		public void run() {
			if ((System.currentTimeMillis() - obu.getLastHeard()) >= obuTimeout) {
				log.error("OBU: " + obu.getMacAddress() + " has timed out");
				
				obuManager.removeObu(obu.getMacAddress());
				/*ObuStatus status = obu.getStatus();
				status.removeRssiMap(obu.getMacAddress());*/
				
				this.cancel();
			}
			
		}
	}
	
	
	protected boolean isTracked (final InetAddress rsuAddr) {
		
		return rsuMap.containsKey(rsuAddr);
		
	}
	
	protected IRsuAgent getAgent(final InetAddress rsuInetAddr) {
		assert (rsuInetAddr != null);
		return rsuMap.get(rsuInetAddr);
	}
	
	
}
