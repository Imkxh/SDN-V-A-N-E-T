package net.floodlightcontroller.wireless.master;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFType;
import org.projectfloodlight.openflow.types.MacAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.floodlightcontroller.core.FloodlightContext;
import net.floodlightcontroller.core.IFloodlightProviderService;
import net.floodlightcontroller.core.IOFMessageListener;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.internal.IOFSwitchService;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.restserver.IRestApiService;
import net.floodlightcontroller.threadpool.IThreadPoolService;
import net.floodlightcontroller.wireless.master.WirelessEventSubscription.EventType;
import net.floodlightcontroller.wireless.master.WirelessEventSubscription.SubType;
import net.floodlightcontroller.wireless.web.WirelessWebRoutable;

public class WirelessMaster implements IFloodlightModule, 
IOFMessageListener, IFloodlightService,ICarAppInterface {


	protected static Logger log = LoggerFactory.getLogger(WirelessMaster.class);
	
	protected IFloodlightProviderService floodlightProvider;
	protected IOFSwitchService switchService;
	protected IRestApiService restApi;
	
	private ScheduledExecutorService executor;
	
	private final RsuManager rsuManager;
	private final ObuManager obuManager;
	
	private final ConcurrentMap<WirelessEventSubscription, NotificationCallback> 
						subscriptions = new ConcurrentHashMap<>();

	static private final String DEFAULT_POOL_FILE = "Poolfile";
	static private final int DEFAULT_PORT = 2819;// UDP端口，心跳报文、AP状态信息、移动终端接入信息等
	static private final int SAFE_DISTANCE = 20;
	
	public WirelessMaster(){
		obuManager = new ObuManager();
		rsuManager = new RsuManager(this, obuManager);
	}
	
	public WirelessMaster(RsuManager rsuManager,ObuManager obuManager) {
		this.rsuManager = rsuManager;
		this.obuManager = obuManager;
	}
	//************** Wireless Agent->Master protocol handlers ***************//
	
/*	*//**
	 * Handle a ping from an agent
	 * 
	 * @param InetAddress of the agent
	 *//*
	protected void handleAgentPing(final InetAddress agentAddr) {
		if (rsuManager.receivePing(agentAddr)) {
			// if the above leads to a new agent being
			// tracked, push the current subscription list
			// to it.
			IApAgent agent = agentManager.getAgent(agentAddr);
			pushAllSubscriptionsToAgent(agent);//向ap下发无缝切换的订阅事件
			
			//executor.execute(new AgentObtainApInfoRunnable(agent));//获取Rsu信息
			subscriptionNotify(EventType.AGENT_ONLINE, agent.getIpAddress().getHostAddress());//为金美提供ap上线消息的订阅事件
		}
		
	}*/
	protected void handleRsuMsg(final String msg){
		
		String properties[] = msg.split("#");
		MacAddress obuMac = MacAddress.of(properties[0]);
		MacAddress obuAddr = MacAddress.of(properties[9]);
		
		if(!rsuManager.receiveRsuMsg(obuMac)){
			updateObuLastHeard (obuMac);
			
		}
		/*else {
			updateObuLastHeard (obuMac);
		}*/

		CarObu carobu = obuManager.getObu(MacAddress.of(properties[0]));
		ObuStatus status = carobu.getStatus();
		if (status == null) {
			status = new ObuStatus();
			carobu.setStatus(status);
		}
		status.setAllStatus(MacAddress.of(properties[0]), properties[1],properties[2], properties[3], properties[4], properties[5],
				properties[6], properties[7], properties[8], properties[9],properties[10]);
		
		//车辆离线后，从rssiMap里移除该车
		if(!obuManager.inObuMap(obuAddr)) {
			CarObu anotherObu = obuManager.getObu(obuMac);
			ObuStatus anotherStatus = anotherObu.getStatus();
			anotherStatus.removeRssiMap(obuAddr);
		}
		
	}
	
	protected void handleRsuTest(final InetAddress agentAddr,final String msg) {
		
		rsuManager.receiveTest(agentAddr,msg);//实例化，分配空间
		//********************存入数据*******************************//
		//updateObuStatus(msg);
		
		String properties[] = msg.split("#");
		CarObu carobu = obuManager.getObu(MacAddress.of(properties[0]));
		ObuStatus status = carobu.getStatus();
		if (status == null) {
			status = new ObuStatus();
			carobu.setStatus(status);
		}
		
	/*	System.out.println(Float.parseFloat(properties[1]));
		System.out.println(properties[3]);
		System.out.println(properties[4]);*/
		
		
		status.setAllStatus(MacAddress.of(properties[0]), properties[1],
				properties[2], properties[3], properties[4], properties[5], properties[6], properties[7], properties[8], properties[9],properties[10]);
		
		//obuManager.getObuMapKey();
		
		//*************************计算任意两车的距离********************
		
		/*测试代码
		 * double a = 0;
		a = obuManager.countCarDistance(MacAddress.of("20:47:da:9c:25:a2"), MacAddress.of("20:47:da:9c:25:a1"));
		System.out.println("jilissssss:--->"+a);*/
		
		
		
	}
	
	protected void handleSafeMsg(String msg) {
		
		subscriptionNotify(EventType.SAFA_HANDLE,msg);
		
	}
	
	
	protected void buildTcp(final InetAddress rsuAddr) {
		
		if(rsuManager.handleAddr(rsuAddr)) {
			
			IRsuAgent agent = rsuManager.getAgent(rsuAddr);
			subscriptionNotify(EventType.AGENT_ONLINE, agent.getIpAddress().getHostAddress());
		}
		else
			updateAgentLastHeard (rsuAddr);
	}
	
	
	public List<String> getObusLists() {
		
		
		//System.out.println("power--->"+obuManager.handleObuPowerControl());
		return obuManager.getObusList();
		
		
		
	}
/*	private synchronized void updateObuStatus(String msg) {
		
		String properties[] = msg.split(" ",5);
		CarObu carobu = obuManager.getObu(MacAddress.of(properties[0]));
		ObuStatus status = carobu.getStatus();
		System.out.println("aaaaaaaaaaaaaaaaaaa");
		
		status.setAllStatus(MacAddress.of(properties[0]), 3,
				4, 5, properties[4]);
		System.out.println("ccc");
		MacAddress a =  carobu.getStatus().getObuMacAddress();
		System.out.println(a.toString());
		
		
	}*/
	
	private void handoffObuPower(InetAddress rsuIpAddr) {
		synchronized (this) {
			IRsuAgent agent = rsuManager.getAgent(rsuIpAddr);
			
			List<String> powerList = new ArrayList<String>();
			powerList = obuManager.handleObuPowerControl();
			if(powerList != null) {
				
				for(String powerMsg : powerList) {
					executor.execute(new ObuHandoffPowerControl(agent,powerMsg));
				}
			}
		}
	}
	
	private void handDistance(InetAddress rsuIpAddr) {
		synchronized (this) {
			IRsuAgent agent = rsuManager.getAgent(rsuIpAddr);
			List<String> distanceList = new ArrayList<String>();
			distanceList = obuManager.getObusList();
			//String alist;
			for(String list:distanceList){
				String[] fields = list.split(" ");
				MacAddress obuAddr1 = MacAddress.of(fields[0]);
				MacAddress obuAddr2 = MacAddress.of(fields[1]);
				double s = obuManager.countCarDistance(obuAddr1, obuAddr2);
				int mark = -1;
				if(s >= SAFE_DISTANCE) {
					mark = 1;
				}
				else
					mark = 2;
				
				String safeMsg1 = obuAddr1.toString()+" "+mark;
				executor.execute(new ObuHandoffSafeDistance(agent,safeMsg1));
				
				String safeMsg2 = obuAddr2.toString()+" "+mark;
				executor.execute(new ObuHandoffSafeDistance(agent,safeMsg2));
			}
			
		}
		
	}
	
	//安全消息为1，不安全消息为2
	private void handoffObuDistan (InetAddress rsuIpAddr, MacAddress obuMac1, MacAddress obuMac2){
		
		synchronized (this) {
			
		IRsuAgent agent = rsuManager.getAgent(rsuIpAddr);
		double s = obuManager.countCarDistance(obuMac1, obuMac2);
		int mark = -1;
		if(s >= SAFE_DISTANCE) {
			mark = 1;
		}
		else
			mark = 2;
		
		String safeMsg = obuMac1.toString()+" "+mark;
		executor.execute(new ObuHandoffSafeDistance(agent,safeMsg));
		}
	}
	
	private class ObuHandoffSafeDistance implements Runnable {
		final IRsuAgent agent;
		final String msg;
		ObuHandoffSafeDistance(IRsuAgent rsuAgent,String safeMsg){
			this.agent = rsuAgent;
			this.msg = safeMsg;
		}
		
		@Override
		public void run() {
			agent.addObuDistanceMsg(msg);
		}
	}
	
	private class ObuHandoffPowerControl implements Runnable {
		final IRsuAgent agent;
		final String msg;
		ObuHandoffPowerControl(IRsuAgent rsuAgent,String powerMsg){
			this.agent = rsuAgent;
			this.msg = powerMsg;
		}
		
		@Override
		public void run() {
			agent.addObuPowerMsg(msg);
		}
	}

	private void updateAgentLastHeard(InetAddress agentAddr) {
		IRsuAgent agent = rsuManager.getAgent(agentAddr);
		
		if (agent != null) {
			// Update last-heard for failure detection
			agent.setLastHeard(System.currentTimeMillis());
		}
	}
	//********************************重写ICarAppInterface***************************************//
	//** Methods provided for the applications(from IApplicationInterface) **//
	
	private void updateObuLastHeard(MacAddress obuMac) {
		CarObu obu = obuManager.getObu(obuMac);
		if (obu != null) {
			// Update last-heard for failure detection
			obu.setLastHeard(System.currentTimeMillis());
		}
	}
	
	@Override
	public Set<CarObu> getObus() {
		return obuManager.getObusFromMap();
	}
	
	@Override
	public void registerSubscription(WirelessEventSubscription wes, 
			NotificationCallback cb) {
		// FIXME: Need to calculate subscriptions per pool
		assert (wes != null);
		assert (cb != null);

		subscriptions.put(wes, cb);//很重要，所有的订阅与回调都存入这个map中
		
		/*if (wes.getSubType() == SubType.APAGENT) {//无缝切换的时候用到
			String sub = wes.getEventType().toString().toLowerCase();
			if (wes.getExtra() != null && !wes.getExtra().equals("")) 
				sub = sub + " " + wes.getExtra();
			 *   sub = "mobility_signal ether src staMAC1 or staMAC2 or.............."  
			executor.execute(new AgentPushSubscriptionRunnable(pool, sub));
		}*/
	}
	
	@Override
	public void handOffObuDistance (InetAddress rsuIpAddr){
		handDistance(rsuIpAddr);
	}
	
	@Override
	public void handoffObuDistance (InetAddress rsuIpAddr, MacAddress obuMac1, MacAddress obuMac2) {
		handoffObuDistan(rsuIpAddr,obuMac1,obuMac2);
	}

	@Override
	public void handoffObuPowerControl(InetAddress rsuIpAddr) {
		handoffObuPower(rsuIpAddr);
	}
	
	//********************************************************************************************//

	//*****************************public方法***************************************************************************************//
	/**
	 * Send subscription message to all subscribers
	 * @param event subscription event type
	 * @param msg subscription message
	 */
	public void subscriptionNotify(WirelessEventSubscription.EventType event, String msg) {
		assert (event != null);
		
		for (Entry<WirelessEventSubscription, NotificationCallback> entry : 
			subscriptions.entrySet()) {
			if (entry.getKey().getEventType().equals(event)) {
				entry.getValue().handle(event, msg);//触发回调
			}
		}
	}
	
/*	private void pushAllSubscriptionsToAgent(final IApAgent agent) {
		
		for (WirelessEventSubscription wes : subscriptions.keySet()) {
			if (wes.getSubType() == SubType.APAGENT) {
				String sub = wes.getEventType().toString().toLowerCase();
				if (wes.getExtra() != null && !wes.getExtra().equals("")) 
					sub = sub + " " + wes.getExtra();
				
				agent.setSubscriptions(sub);
			}
			
		}
	}*/
	
	//******************** IFloodlightModule methods ********************//
	
	@Override
	public Collection<Class<? extends IFloodlightService>> getModuleServices() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<Class<? extends IFloodlightService>, IFloodlightService> getServiceImpls() {
		Map<Class<? extends IFloodlightService>, IFloodlightService> m = 
				new HashMap<Class<? extends IFloodlightService>,
        IFloodlightService>();
        m.put(WirelessMaster.class, this);
        return m;
	}

	@Override
	public Collection<Class<? extends IFloodlightService>> getModuleDependencies() {
		Collection<Class<? extends IFloodlightService>> l =
		        new ArrayList<Class<? extends IFloodlightService>>();
		l.add(IFloodlightProviderService.class);
		l.add(IOFSwitchService.class);
		l.add(IRestApiService.class);
		return l;
	}

	@Override
	public void init(FloodlightModuleContext context) throws FloodlightModuleException {
		floodlightProvider = context.getServiceImpl(IFloodlightProviderService.class);
		switchService = context.getServiceImpl(IOFSwitchService.class);
		restApi = context.getServiceImpl(IRestApiService.class);
		IThreadPoolService tp = context.getServiceImpl(IThreadPoolService.class);
		executor = tp.getScheduledExecutor();
		
		try {
			readPoolFile(context);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	private void readPoolFile(FloodlightModuleContext context) 
			throws UnknownHostException, InstantiationException, 
			IllegalAccessException, ClassNotFoundException, IOException {
		
		// read config options读配置选项
        Map<String, String> configOptions = context.getConfigParams(this);
        
        // List of trusted agents
        String agentAuthListFile = DEFAULT_POOL_FILE;
        String agentAuthListFileConfig = configOptions.get("PoolFile");
        
        if (agentAuthListFileConfig != null) {
        	agentAuthListFile = agentAuthListFileConfig; 
        }
        
        List<CarApplication> applicationList = 
        		new ArrayList<CarApplication>();
		BufferedReader br = new BufferedReader(
				new FileReader(agentAuthListFile));
		
		String strLine;
		
		/* Each line has the following format:
		 * 
		 * IPAddr-of-agent  pool1 pool2 pool3 ...
		 */
		while ((strLine = br.readLine()) != null) {
			if (strLine.startsWith("#")) // comment
				continue;
			
			if (strLine.length() == 0) // blank line
				continue;
			
			
			// NAME
						String [] fields = strLine.split(" "); 
						if (!fields[0].equals("NAME")) {
							log.error("Missing NAME field " + fields[0]);
							log.error("Offending line: " + strLine);
							System.exit(1);
						}
						
						if (fields.length != 2) {
							log.error("A NAME field should specify a single string as a pool name");
							log.error("Offending line: " + strLine);
							System.exit(1);
						}

						String poolName = fields[1];
						//poolManager.addPool(poolName);
						System.out.println("pool:"+poolName);
					
			// 读取APPLICATIONS，表示系统中将要加载的APP
			strLine = br.readLine();

			fields = strLine.split(" ");
			
			if (!fields[0].equals("APPLICATIONS")) {
				log.error("A NETWORKS field should be followed by an APPLICATIONS field");
				log.error("Offending line: " + strLine);
				System.exit(1);
			}
			
			for (int i = 1; i < fields.length; i++) {
				CarApplication appInstance = (CarApplication) Class.forName(fields[i]).newInstance();
				appInstance.setWirelessInterface(this);

				applicationList.add(appInstance);
			}
		}
		
		br.close();
		
		// 加线程运行 applications
        for (CarApplication app: applicationList) {
        	if (app == null) {
				System.out.println("null");
        	}
        	executor.execute(app);
        }
	}

	@Override
	public void startUp(FloodlightModuleContext context) throws FloodlightModuleException {
		floodlightProvider.addOFMessageListener(OFType.PACKET_IN, this);//this指代WirelessMaster
        executor.execute(new RsuMsgServer(this, DEFAULT_PORT, executor));//建立UDP连接 实现AP主动上传消息   
 //     RsuManager.setSwitchService(switchService);
        restApi.addRestletRoutable(new WirelessWebRoutable());
	}

	//******************** IOFMessageListener methods ********************//
	
	@Override
	public String getName() {
		return WirelessMaster.class.getSimpleName();
	}

	@Override
	public boolean isCallbackOrderingPrereq(OFType type, String name) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isCallbackOrderingPostreq(OFType type, String name) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Command receive(IOFSwitch sw, OFMessage msg, FloodlightContext cntx) {
		return Command.CONTINUE;
	}
}
