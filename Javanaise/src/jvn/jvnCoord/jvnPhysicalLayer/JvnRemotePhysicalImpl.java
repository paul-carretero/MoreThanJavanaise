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

public class JvnRemotePhysicalImpl extends UnicastRemoteObject implements JvnRemotePhysical{

	/**
	 * serialVersionUID
	 */
	private static final long	serialVersionUID	= -3473522236789019332L;

	private static JvnRemotePhysicalImpl 	jps;
		
	private Registry 						rmiRegistry;
	
	private JvnAbstractLoadBalancer 		myLoadBalancer;
	
	private final Map<Integer,JvnSlaveCoordImpl> 	slaveCoords;
	
	private final Map<Integer,JvnMasterCoordImpl> 	masterCoords;

	protected JvnRemotePhysicalImpl() throws RemoteException {
		super();
		System.out.println("[PHYSICAL] ["+this.hashCode()+"]");
		this.slaveCoords			= new HashMap<>();
		this.masterCoords			= new HashMap<>();
		this.rmiRegistry			= LocateRegistry.getRegistry();
		this.myLoadBalancer 		= null;
		jps 						= this;
		
		try {
			((JvnLoadBalancer) this.rmiRegistry.lookup("JvnLoadBalancer")).jvnPhysicalCoordRegister(this);
			
			// ça aura déjà fail si il n'y a pas de masterLB, on tente donc d'ajouter un slaveLB si besoin
			
			try {
				((JvnLoadBalancer) this.rmiRegistry.lookup("JvnLoadBalancerSlave")).ping();
			} catch (RemoteException | NotBoundException e) {
				try {
					this.myLoadBalancer = new JvnSlaveLoadBalancerImpl();
				} catch (MalformedURLException | JvnException | NotBoundException e1) {
					e.printStackTrace();
					System.err.println("--------------------------");
					e1.printStackTrace();
				}
			}
			
		} catch (JvnException | RemoteException | NotBoundException e) {
			try {
				this.myLoadBalancer = new JvnMasterLoadBalancerImpl();
			} catch (MalformedURLException | JvnException e1) {
				e.printStackTrace();
				System.err.println("--------------------------");
				e1.printStackTrace();
			}
		}
	}
	
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

	/**
	 * pour les tests seulement
	 * @return l'id d'objet sur le loadBalancer de cette JVM
	 * @throws JvnException 
	 * @throws RemoteException 
	 */
	public int jvnGetObjectId() throws RemoteException, JvnException {
		return this.myLoadBalancer.jvnGetObjectId();
	}	
}
