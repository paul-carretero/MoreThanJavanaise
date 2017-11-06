package jvn.jvnCoord.jvnPhysicalLayer;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;

import jvn.jvnCoord.JvnLogicalCoord.JvnMasterCoordImpl;
import jvn.jvnCoord.JvnLogicalCoord.JvnSlaveCoordImpl;
import jvn.jvnCoord.jvnLoadBalancer.JvnAbstractLoadBalancer;
import jvn.jvnCoord.jvnLoadBalancer.JvnLoadBalancer;
import jvn.jvnCoord.jvnLoadBalancer.JvnMasterLoadBalancerImpl;
import jvn.jvnCoord.jvnLoadBalancer.JvnSlaveLoadBalancerImpl;
import jvn.jvnExceptions.JvnException;

/**
 * @author Paul Carretero
 * Machine physique
 * Possède 0 ou 1 loadbalancer et 0 ou n coordinateur logique.
 * Lance automatiquement un loadbalancer master ou slave si l'un ou l'autre n'est pas présent
 */
public class JvnRemotePhysicalImpl extends UnicastRemoteObject implements JvnRemotePhysical{

	/**
	 * serialVersionUID
	 */
	private static final long	serialVersionUID	= -3473522236789019332L;

	/**
	 * managé comme un singleton
	 */
	private static JvnRemotePhysicalImpl 	jps;
		
	/**
	 * registre rmi local
	 */
	private Registry 						rmiRegistry;
	
	/**
	 * loadbalancer local, ou null si aucun
	 */
	private JvnAbstractLoadBalancer 		myLoadBalancer;
	
	/**
	 * map des coordinateur slave lancé par cette machine (id => slave)
	 */
	private final Map<Integer,JvnSlaveCoordImpl> 	slaveCoords;
	
	/**
	 * map des coordinateur master lancé par cette machine (id => master)
	 */
	private final Map<Integer,JvnMasterCoordImpl> 	masterCoords;

	/**
	 * Constructeur par default de la machine physique
	 * initialise les loadbalancer si besoin et s'enregistre auprès du loadbalancer master
	 * @throws RemoteException
	 */
	private JvnRemotePhysicalImpl() throws RemoteException {
		super();
		System.out.println("[PHYSICAL] ["+this.hashCode()+"]");
		this.slaveCoords			= new HashMap<>();
		this.masterCoords			= new HashMap<>();
		this.rmiRegistry			= LocateRegistry.getRegistry();
		this.myLoadBalancer 		= null;
		jps 						= this;
		boolean alreadyMaster		= false;
		boolean alreadySlave		= false;
		

		try {
			((JvnLoadBalancer) this.rmiRegistry.lookup("JvnLoadBalancer")).ping();
			alreadyMaster 			= true;
		} catch (@SuppressWarnings("unused") Exception e) {}
		
		try {
			((JvnLoadBalancer) this.rmiRegistry.lookup("JvnLoadBalancerSlave")).ping();
			alreadySlave 			= true;
		} catch (@SuppressWarnings("unused") Exception e) {}
		
		
		if(!alreadyMaster) {
			try {
				this.myLoadBalancer = new JvnMasterLoadBalancerImpl();
			} catch (MalformedURLException | JvnException e) {
				e.printStackTrace();
			}
		}
		else if(!alreadySlave) {
			try {
				JvnLoadBalancer master = ((JvnLoadBalancer) this.rmiRegistry.lookup("JvnLoadBalancer"));
				master.ping();
				master.jvnPhysicalCoordRegister(this);	
				this.myLoadBalancer = new JvnSlaveLoadBalancerImpl();
			} catch (MalformedURLException | JvnException | NotBoundException e) {
				e.printStackTrace();
			}
		}
		else {
			try {
				((JvnLoadBalancer) this.rmiRegistry.lookup("JvnLoadBalancer")).jvnPhysicalCoordRegister(this);
			} catch (JvnException | NotBoundException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * managé comme un singleton
	 * @return l'instance local de la machine physique
	 */
	public static JvnRemotePhysicalImpl jvnGetLocalPhysical() {
		if (jps == null){
			try {
				jps = new JvnRemotePhysicalImpl();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return jps;
	}
	
	// méthodes remote ////////////////////////////////////////////////////////

	@Override
	public void jvnNewSlaveLoadBalancer() throws RemoteException {
		try {
			this.myLoadBalancer = new JvnSlaveLoadBalancerImpl();
		} catch (MalformedURLException | JvnException | NotBoundException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void ping() throws RemoteException {}
	
	@Override
	public void jvnNewSlaveCoordInstance(int id) throws RemoteException {
		try {
			this.slaveCoords.put(id,new JvnSlaveCoordImpl(id));
		} catch (MalformedURLException | JvnException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void jvnNewMasterCoordInstance(int id) throws RemoteException {
		try {
			this.masterCoords.put(id,new JvnMasterCoordImpl(id));
		} catch (MalformedURLException | JvnException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void killCoord(int coordId) throws RemoteException {
		if(this.masterCoords.containsKey(coordId)) {
			this.masterCoords.get(coordId).kill();
			this.masterCoords.remove(coordId);
		}
		if(this.slaveCoords.containsKey(coordId)) {
			this.slaveCoords.get(coordId).kill();
			this.slaveCoords.remove(coordId);
		}
	}
	
	@Override
	public void upgradeCoord(int coordId) throws RemoteException {
		if(this.slaveCoords.containsKey(coordId)) {
			try {
				this.slaveCoords.get(coordId).upgrade();
			} catch (JvnException e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public boolean isLoadBalancer() throws RemoteException {
		return this.myLoadBalancer != null;
	}
	
	// méthodes locales ////////////////////////////////////////////////////////
	
	/**
	 * Méthode d'initiation global
	 * Appelée seulement au lancement du premier serveur physique
	 */
	public void jvnNewMasterLoadBalancer() {
		try {
			this.myLoadBalancer = new JvnMasterLoadBalancerImpl();
		} catch (RemoteException | MalformedURLException | JvnException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Détruit ce serveur physique (et les objets associés)
	 */
	public void destroy() {
		try {
			this.myLoadBalancer.destroy();
			this.slaveCoords.forEach((k,v) -> v.kill());
			this.masterCoords.forEach((k,v) -> v.kill());
			UnicastRemoteObject.unexportObject(this,true);
		} catch (Exception e) {
			System.out.println("please ignore : " + e.getMessage());
		}
		
		try {
			finalize();
		} catch (Throwable e) {
			System.out.println("please ignore : " + e.getMessage());
		}
	}
}
