package jvn.jvnCoord.jvnLoadBalancer;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import jvn.jvnCoord.jvnPhysicalLayer.JvnRemotePhysical;
import jvn.jvnExceptions.JvnException;

/**
 * @author Paul Carretero
 * Loadbalancer slave
 * recoit les mise à jour d'un loadbalancer master
 * lors de la mort de celui ci, créer un nouveau loadbalancer master et se termine
 */
public class JvnSlaveLoadBalancerImpl extends JvnAbstractLoadBalancer implements Runnable{

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID 	= -4250661332414004583L;
	
	/**
	 * Temps entre deux Heartbeats
	 */
	private static final int 	REFRESH_RATE 	= 200;
	
	/**
	 * master de référence pour le loadbalancer
	 */
	private final JvnLoadBalancer master;

	/**
	 * Constructeur par défault
	 * @throws RemoteException
	 * @throws MalformedURLException
	 * @throws JvnException
	 * @throws NotBoundException
	 */
	public JvnSlaveLoadBalancerImpl() throws RemoteException, MalformedURLException, JvnException, NotBoundException {
		super();
		this.master			= (JvnLoadBalancer) this.rmiRegistry.lookup("JvnLoadBalancer");
		this.currentOjectId	= this.master.jvnInitObjectId();
		Naming.rebind(HOST_URL+"JvnLoadBalancerSlave", this);
		(new Thread(this)).start();
		System.out.println("[LOADBALANCER] [SLAVE]");
	}

	@SuppressWarnings("unused")
	@Override
	public void run() {
		boolean alive = true;
		while(alive) {
			try {
				this.master.ping();
			} catch (RemoteException e) {
				alive = false;
			}
			try {
				Thread.sleep(REFRESH_RATE);
			} catch (InterruptedException e) {
				System.out.println("Goodbye old friend...");
			}
		}
		if(!alive) {
			try {
				new JvnMasterLoadBalancerImpl(this.coordMap, this.currentOjectId); // local
			} catch (RemoteException | MalformedURLException | JvnException e) {
				e.printStackTrace();
			}
			this.destroy();
		}
	}
	
	@Override
	synchronized public int jvnGetObjectId() throws RemoteException, JvnException {
		return this.currentOjectId++;
	}
	
	@Override
	public void jvnPhysicalCoordRegister(JvnRemotePhysical coord) throws RemoteException, JvnException {
		throw new JvnException("Slave Loadbalancer");
	}

	@Override
	public void updateJvnCoordMap(JvnCoordMap jcm) throws RemoteException, JvnException {
		this.coordMap = jcm;
	}

	@Override
	public int jvnInitObjectId() throws RemoteException, JvnException {
		throw new JvnException("Slave Loadbalancer");
	}
}
