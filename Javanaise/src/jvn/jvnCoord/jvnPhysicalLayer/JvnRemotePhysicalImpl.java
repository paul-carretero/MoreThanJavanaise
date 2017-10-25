package jvn.jvnCoord.jvnPhysicalLayer;

import java.net.MalformedURLException;
import java.rmi.AccessException;
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
	
	private Map<Integer,JvnSlaveCoordImpl> 	slaveCoords;

	protected JvnRemotePhysicalImpl() throws RemoteException {
		super();
		System.out.println("[PHYSICAL] ["+this.hashCode()+"]");
		this.slaveCoords			= new HashMap<>();
		this.rmiRegistry			= LocateRegistry.getRegistry();
		this.myLoadBalancer 		= null;
		boolean shouldCreateSlaveLB	= false;
		jps 						= this;
		
		try {
			shouldCreateSlaveLB 	= ((JvnLoadBalancer) this.rmiRegistry.lookup("JvnLoadBalancer")).jvnPhysicalCoordRegister(this);
		} catch (JvnException | RemoteException | NotBoundException e) {
			try {
				this.myLoadBalancer = new JvnMasterLoadBalancerImpl();
			} catch (MalformedURLException | JvnException e1) {
				e.printStackTrace();
				System.err.println("--------------------------");
				e1.printStackTrace();
			}
		}
		
		if(shouldCreateSlaveLB) {
			try {
				this.myLoadBalancer = new JvnSlaveLoadBalancerImpl();
			} catch (MalformedURLException | JvnException | NotBoundException e) {
				e.printStackTrace();
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
	 * @throws NotBoundException 
	 * @throws JvnException 
	 * @throws RemoteException 
	 * @throws AccessException 
	 */
	public void destroy() {
		try {
			//((JvnLoadBalancer) this.rmiRegistry.lookup("JvnLoadBalancer")).jvnPhysicalCoordDestroy(this);
			this.myLoadBalancer.destroy();
			this.slaveCoords.forEach((k,v) -> v.kill());
			UnicastRemoteObject.unexportObject(this,true);
		} catch (Exception e) {
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

	@Override
	public void jvnNewMasterCoordInstance(int id) throws RemoteException {
		try {
			new JvnMasterCoordImpl(id);
		} catch (MalformedURLException | JvnException e) {
			e.printStackTrace();
		}
	}	
}
