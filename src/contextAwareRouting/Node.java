package contextAwareRouting;

import java.util.LinkedList;

public abstract class Node {
	private int nodeID;

	private int xpos;
	private int ypos;
	private  LinkedList<Request> queue;
	private int serviceTime;
	private boolean waiting;
	private int nextNodeID;
	private boolean handlingRequest;

	public Node (int nodeID, int xpos, int ypos) {
		this.nodeID = nodeID;

		this.xpos = xpos;
		this.ypos = ypos;
		this.queue = new LinkedList<Request>();

		this.setServiceTime(0);
		this.setWaiting(false);
		this.setHandlingRequest(false);
	}

	public Node (int nodeID, int xpos, int ypos, LinkedList<Request> queue) {
		this(nodeID,xpos,ypos);
		this.queue = queue;
	}

	public void run() {
		if (!isWaiting()) {
			if (isHandlingRequest()) {
				deployRequest();
				setHandlingRequest(false);
			} else {
				handleRequest();
			}
		}
	}

	protected abstract void handleRequest();

	public int getNodeID() {
		return this.nodeID;
	}

	public int getXpos() {
		return this.xpos;
	}

	public void setXpos(int xpos) {
		this.xpos = xpos;
	}

	public int getYpos() {
		return this.ypos;
	}

	public void setyPos(int ypos) {
		this.ypos = ypos;
	}

	public Request getNextRequest() {
		return queue.getFirst();
	}

	public void addRequest (Request request) {
		request.setState(1, nodeID);
		queue.add(request);
	}

	protected Request removeRequest() {
		return queue.remove();
	}

	public int getQueueSize() {
		return queue.size();
	}

	public LinkedList<Request> getQueue() {
		return queue;
	}

	public int getServiceTime() {
		return serviceTime;
	}

	public void setServiceTime(int serviceTime) {
		this.serviceTime = serviceTime;
	}

	public boolean isWaiting() {
		return waiting;
	}

	public void setWaiting(boolean waiting) {
		this.waiting = waiting;
	}

	public int getNextNodeID() {
		return nextNodeID;
	}

	public void setNextNodeID(int nextNodeID) {
		this.nextNodeID = nextNodeID;
	}

	public boolean isHandlingRequest() {
		return handlingRequest;
	}

	public void setHandlingRequest(boolean handlingRequest) {
		this.handlingRequest = handlingRequest;
	}

	protected void deployRequest() {
		Request request = queue.remove();
		request.setCurrentNodeID(nextNodeID);
	}

}
