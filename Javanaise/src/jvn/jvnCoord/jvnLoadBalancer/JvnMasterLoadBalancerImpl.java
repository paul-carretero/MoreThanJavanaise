package jvn.jvnCoord.jvnLoadBalancer;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import jvn.jvnCoord.jvnPhysicalLayer.JvnRemotePhysical;
import jvn.jvnCoord.jvnPhysicalLayer.JvnRemotePhysicalImpl;
import jvn.jvnExceptions.JvnException;

public class JvnMasterLoadBalancerImpl extends JvnAbstractLoadBalancer {

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = -71074878096960858L;
	private JvnLoadBalancer slave;
	private final ExecutorService asyncOps;

	/**
	 * créer depuis une jvm
	 * @param physicalLayer
	 * @throws JvnException
	 * @throws RemoteException
	 * @throws MalformedURLException
	 */
	public JvnMasterLoadBalancerImpl() throws JvnException, RemoteException, MalformedURLException {
		super();
		Naming.rebind(HOST_URL+"JvnLoadBalancer", this);
		System.out.println("[LOADBALANCER] [MASTER] [1]");
		this.asyncOps = Executors.newSingleThreadExecutor();
		this.coordMap.addPhysicalLayer(JvnRemotePhysicalImpl.jvnGetLocalPhysical());
		this.coordMap.initialize();
		this.coordMap.start();
	}

	/**
	 * créer par un slave
	 * @param CoordMap
	 * @param currentOjectId
	 * @throws JvnException
	 * @throws RemoteException
	 * @throws MalformedURLException
	 */
	public JvnMasterLoadBalancerImpl(JvnCoordMap CoordMap, int currentOjectId) throws JvnException, RemoteException, MalformedURLException {
		super(CoordMap,currentOjectId);
		Naming.rebind(HOST_URL+"JvnLoadBalancer", this);
		System.out.println("[LOADBALANCER] [MASTER] [2]");
		this.asyncOps = Executors.newSingleThreadExecutor();
		this.coordMap.heartbeat();
		this.coordMap.newSlaveLoadBalancer();
		this.coordMap.reArrangeCoords();
		this.coordMap.start();
	}

	@Override
	synchronized public int jvnGetObjectId() throws RemoteException, JvnException {
		if(this.slave == null) {
			this.slave = this.coordMap.getSlaveLoadBalancer();
		}
		if (this.slave != null) {
			try {
				this.slave.jvnGetObjectId();
			} catch (@SuppressWarnings("unused") RemoteException e) {
				this.slave = null;
			}
		}
		return this.currentOjectId++;
	}
	
	private class Task implements Runnable{
		private final JvnCoordMap coordMap;
		private final JvnRemotePhysical coord;

		protected Task(JvnCoordMap coordMap, JvnRemotePhysical coord) {
			this.coordMap 	= coordMap;
			this.coord 		= coord;
		}
		
		@Override
		public void run() {
			this.coordMap.addPhysicalLayer(this.coord);
		}
	}

	@Override
	synchronized public void jvnPhysicalCoordRegister(JvnRemotePhysical coord) throws RemoteException, JvnException {
		this.asyncOps.submit(new Task(this.coordMap, coord));
	}

	@Override
	public void updateJvnCoordMap(JvnCoordMap jcm) throws RemoteException, JvnException {
		throw new JvnException("I'm a Master");
	}
}
