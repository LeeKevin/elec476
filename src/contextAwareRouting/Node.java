package contextAwareRouting;

import java.util.ArrayList;
import java.util.LinkedList;

import contextAwareRouting.Request.Status;

public abstract class Node {
	//Node attributes
	protected int nodeID;
	private double xpos;
	private double ypos;
	protected ArrayList<Integer> appList;
	protected LinkedList<Request> queue;
	
	//Current request variables
	protected int serviceTime = -1;
	private int nextNodeID;
	protected Request reqInService;

	public Node (int nodeID, double xpos, double ypos) {
		this.nodeID = nodeID;
		this.xpos = xpos;
		this.ypos = ypos;
		this.queue = new LinkedList<Request>();
	}

	public Node (int nodeID, double xpos, double ypos, ArrayList<Integer> appList) {
		this(nodeID,xpos,ypos);
		this.appList = appList;
	}
	
	public void run() {
		//if serving count the time down
		if (serviceTime>0){
			serviceTime--;
		//finish service
		}else if(serviceTime==0){
			//deploy if there is anything to deploy
			if (reqInService != null)
				deployRequest();
			//if anything in queue start service
			if (queue.size()>0){
				serviceNextRequest();
			//else set state to idle
			}else{
				serviceTime--;
			}
		//if idle check if anything in queue and start service
		}else{
			if (queue.size()>0){
				serviceNextRequest();
			}
		}
	}

	protected void deployRequest() {
		//If valid next node send it along
		if (nextNodeID >= 0){
			reqInService.setCurrentNodeID(nextNodeID);
			Mainline.server.retrieveNode(nextNodeID).addRequest(reqInService);
		//else drop it
		}else{
			reqInService.calculateTimeInSystem(fivetrial.mainline.time);
			reqInService.setStatus(Status.DROPPED);
			fivetrial.mainline.dropped ++;
			fivetrial.mainline.numdone++;
		}
	}

	protected void sendToServer() {
		Mainline.server.addNodeRequest(nodeID);
	}

	//Calculates and sets the amount of time node waits to simulate servicing
	protected void calculateServiceTime(Request request) {
		RandomNumGen generator = new RandomNumGen();
		double rate;
		
		//If apps are implemented
		if (appList != null){
			//Gets the request application
			int reqApp = request.getApp();
			int minDist = Mainline.numApps;
			
			//cycles through node applications to check for best fit distance is minimum separation around a circle of diameter numapps
			for (Integer app : appList) {
				int dist = Math.min(Math.abs(reqApp - app), Math.min(reqApp, app) + Mainline.numApps - Math.max(reqApp, app));
				if (dist < minDist)
					minDist = dist;
			}
			//rate follows exponential decay with distance
			rate = (minDist == 0) ? 2.0 : 1/(0.5 * Math.log((double) minDist) + 1.0);
		}else{
			rate = 2;
		}
		
		//exponentialy distributed random time based on calculated rate
		setServiceTime( (int) (generator.nextExp(rate)*10)); // service time in centiseconds
	}
	
	//For context aware calculations returns the rate that would otherwise be used to calculate service time (see above)
	public double calculateServiceRate(Request request) {
		double rate;
		
		if (appList != null){
			int reqApp = request.getApp();
			int minDist = Mainline.numApps;
			for (Integer app : appList) {
				int dist = Math.min(Math.abs(reqApp - app), Math.min(reqApp, app) + Mainline.numApps - Math.max(reqApp, app));
				if (dist < minDist)
					minDist = dist;
			}
			rate = (minDist == 0) ? 2.0 : 1/(0.5 * Math.log((double) minDist) + 1.0);
		}else{
			rate = 2;
		}
		return rate;
	}

	//NODE ID
	public int getNodeID() {
		return this.nodeID;
	}

	//Position Methods
	public double getXpos() {
		return this.xpos;
	}

	public void setXpos(double xpos) {
		this.xpos = xpos;
	}

	public double getYpos() {
		return this.ypos;
	}

	public void setYpos(double ypos) {
		this.ypos = ypos;
	}

	//Applist Methods
	public ArrayList<Integer> getAppList(){
		ArrayList<Integer> apps = new ArrayList<Integer>();
		for (Integer app : appList)
			apps.add(app);
		return apps;
	} 	

	public void addApp(Integer app){
		appList.add(app);
	} 	

	public void remove(Integer app){
		if (appList.contains(app))
			appList.remove(appList.indexOf(app));
	}

	//Queue Methods
	public Request getNextRequest() {
		return queue.getFirst();
	}

	public void addRequest (Request request) {
		request.setInQueue(true);
		queue.add(request);
	}

	protected Request removeRequest() {
		return queue.remove();
	}

	public int getQueueSize() {
		return queue.size();
	}

	public LinkedList<Request> getQueue(){
		LinkedList<Request> reqs = new LinkedList<Request>();
		for (Request req : queue)
			reqs.add(req);
		return reqs;
	}

	public Request getReqInService() {
		return reqInService;
	}

	public void setReqInService (Request request) {
		this.reqInService = request;
	}

	//Service Time Methods
	public int getServiceTime() {
		return serviceTime;
	}

	public void setServiceTime(int serviceTime) {
		if (serviceTime < 0)
			this.serviceTime = 0;
		else 
			this.serviceTime = serviceTime;

	}

	public int getNextNodeID() {
		return nextNodeID;
	}

	public void setNextNodeID(int nextNodeID) {
		this.nextNodeID = nextNodeID;
	}
	
	protected abstract void serviceNextRequest();



}
