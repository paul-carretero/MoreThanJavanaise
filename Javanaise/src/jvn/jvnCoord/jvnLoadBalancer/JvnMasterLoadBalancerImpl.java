package jvn.jvnCoord.jvnLoadBalancer;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;

import jvn.jvnCoord.jvnPhysicalLayer.JvnRemotePhysical;
import jvn.jvnExceptions.JvnException;

public class JvnMasterLoadBalancerImpl extends JvnAbstractLoadBalancer {

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = -71074878096960858L;
	private JvnLoadBalancer slave;

	/**
	 * créer depuis une jvm
	 * @param physicalLayer
	 * @throws JvnException
	 * @throws RemoteException
	 * @throws MalformedURLException
	 */
	public JvnMasterLoadBalancerImpl(JvnRemotePhysical physicalLayer) throws JvnException, RemoteException, MalformedURLException {
		super(physicalLayer);
		Naming.rebind(HOST_URL+"JvnLoadBalancer", this);
		System.out.println("[LOADBALANCER] [MASTER] [1]");
		this.CoordMap.addPhysicalLayer(physicalLayer);
		this.CoordMap.initialize();
		this.CoordMap.start();
	}
	
	/**
	 * créer par un slave
	 * @param CoordMap
	 * @param currentOjectId
	 * @throws JvnException
	 * @throws RemoteException
	 * @throws MalformedURLException
	 */
	public JvnMasterLoadBalancerImpl(JvnRemotePhysical physicalLayer, JvnCoordMap CoordMap, int currentOjectId) throws JvnException, RemoteException, MalformedURLException {
		super(physicalLayer, CoordMap,currentOjectId);
		Naming.rebind(HOST_URL+"JvnLoadBalancer", this);
		System.out.println("[LOADBALANCER] [MASTER] [2]");
		this.CoordMap.start();
	}
	
	@Override
	synchronized public int jvnGetObjectId() throws RemoteException, JvnException {
		if (this.slave != null) {
			try {
				this.slave.jvnGetObjectId();
			} catch (@SuppressWarnings("unused") RemoteException e) {
				this.slave = null;
			}
		}
		return this.currentOjectId++;
	}
	
	@Override
	synchronized public int jvnLoadBalancerRegister(JvnLoadBalancer lb) throws RemoteException, JvnException {
		if(this.slave == null) {
			this.slave = lb;
			return this.currentOjectId;
			// TODO should retourne the whole map+id
		}
		throw new JvnException("Loadbalancer Slave déjà présent");
	}
	
	@Override
	synchronized public boolean jvnPhysicalCoordRegister(JvnRemotePhysical coord) throws RemoteException, JvnException {
		this.CoordMap.addPhysicalLayer(coord);
		return (this.slave == null);
	}

	@Override
	public void jvnPhysicalCoordDestroy(JvnRemotePhysical jvnRemotePhysical) throws RemoteException, JvnException {
		this.CoordMap.killAll(jvnRemotePhysical);
	}

}
