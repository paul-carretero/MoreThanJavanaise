package jvn.jvnCoord.jvnLoadBalancer;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import jvn.jvnCoord.jvnPhysicalLayer.JvnRemotePhysical;
import jvn.jvnCoord.jvnPhysicalLayer.JvnRemotePhysicalImpl;
import jvn.jvnExceptions.JvnException;

/**
 * @author Paul Carretero
 * Loadbalancer master
 * Gère les id des objets javanaise et met à jour un slave pour la redondance
 * Gère également la liste des coordinateurs (et les opérations associées)
 */
public class JvnMasterLoadBalancerImpl extends JvnAbstractLoadBalancer {

	/**
	 * serialVersionUID
	 */
	private static final long 	serialVersionUID = -71074878096960858L;
	
	/**
	 * loadbalancer slave mis à jour (ou null si non existant)
	 */
	private JvnLoadBalancer 	slave;
	
	/**
	 * permet des operations asynchrone (notament pour laisser des initialisations se terminer)
	 */
	private final ExecutorService asyncOps;

	/**
	 * créer depuis une machine physique lors du premier démarrage
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
	 * @param CoordMap la map des coordinateur
	 * @param currentOjectId id de l'objet javanaise courrant
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
	synchronized public int jvnInitObjectId() throws RemoteException {
		return this.currentOjectId;
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
	
	/**
	 * @author Paul Carretero
	 * tache visant à ajouter une machine physique à la map des coordinateur
	 * cette tache étant assez lourde elle est effectuée de manière asynchrone 
	 */
	private class Task implements Runnable{
		
		/**
		 * représentation des coordinateurs
		 */
		private final JvnCoordMap 		localCoordMap;
		
		/**
		 * la nouvelle machine physique à ajouter
		 */
		private final JvnRemotePhysical	phys;

		/**
		 * @param coordMap référence vers une représentation des coordinateurs
		 * @param phys la nouvelle machine physique à ajouter
		 */
		protected Task(JvnCoordMap coordMap, JvnRemotePhysical phys) {
			this.localCoordMap 	= coordMap;
			this.phys 			= phys;
		}
		
		@Override
		public void run() {
			this.localCoordMap.addPhysicalLayer(this.phys);
		}
	}

	@Override
	synchronized public void jvnPhysicalCoordRegister(JvnRemotePhysical phys) throws RemoteException, JvnException {
		this.asyncOps.submit(new Task(this.coordMap, phys));
	}

	@Override
	public void updateJvnCoordMap(JvnCoordMap jcm) throws RemoteException, JvnException {
		throw new JvnException("I'm a Master");
	}
}
