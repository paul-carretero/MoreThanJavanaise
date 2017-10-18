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

	public JvnMasterLoadBalancerImpl(JvnRemotePhysical physicalLayer) throws JvnException, RemoteException, MalformedURLException {
		super();
		Naming.rebind(HOST_URL+"JvnLoadBalancer", this);
		this.CoordMap.addPhysicalLayer(physicalLayer, 1);
	}
	
	public JvnMasterLoadBalancerImpl(JvnCoordMap CoordMap, int currentOjectId) throws JvnException, RemoteException, MalformedURLException {
		super(CoordMap,currentOjectId);
		Naming.rebind(HOST_URL+"JvnLoadBalancer", this);
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
			// should retourne the whole map+id
		}
		throw new JvnException("Loadbalancer Slave déjà présent");
	}
	
	@Override
	synchronized public void jvnPhysicalCoordRegister(JvnRemotePhysical coord) throws RemoteException, JvnException {
		this.CoordMap.addPhysicalLayer(coord);
	}

}
