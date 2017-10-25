package jvn.jvnCoord.jvnLoadBalancer;

import java.io.Serializable;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import jvn.jvnCoord.JvnLogicalCoord.JvnRemoteCoord;
import jvn.jvnCoord.jvnPhysicalLayer.JvnRemotePhysical;
import jvn.jvnExceptions.JvnException;

public class JvnCoordMap extends Thread implements Serializable {

	// Coordinateurs Logique

	private static final int REFRESH = 1000;
	private static final int TIMEOUT = 3000;
	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = -7390263206317147214L;

	private final Map<JvnRemotePhysical, Map<Integer,JvnRemoteCoord>> masterlists;

	private final Map<JvnRemotePhysical, Map<Integer,JvnRemoteCoord>> slavelists;

	private final int numberOfCluster;

	private transient JvnLoadBalancer slave;

	private transient Registry rmiRegistry;

	private transient JvnRemotePhysical myPhysJVM;

	/**
	 * Instancie la classe pour gérer les liste des coordinateur master et slave
	 * @param numberOfCluster nombre de cluster
	 */
	public JvnCoordMap(int numberOfCluster) {
		try {
			this.rmiRegistry	= LocateRegistry.getRegistry();
		} catch (RemoteException e) {
			this.rmiRegistry	= null;
			e.printStackTrace();
		}
		this.masterlists		= new ConcurrentHashMap<>();
		this.slavelists	 	 	= new ConcurrentHashMap<>();	
		this.numberOfCluster	= numberOfCluster;
		getSlaveLoadBalancer();
	}

	synchronized public void setMyPhysJVM(JvnRemotePhysical myPhysJVM) {
		this.myPhysJVM = myPhysJVM;
	}

	synchronized protected JvnLoadBalancer getSlaveLoadBalancer() {
		if(this.rmiRegistry == null) {
			try {
				this.rmiRegistry	= LocateRegistry.getRegistry();
			} catch (RemoteException e) {
				this.rmiRegistry	= null;
				e.printStackTrace();
			}
		}

		try {
			this.slave			= (JvnLoadBalancer) this.rmiRegistry.lookup("JvnLoadBalancerSlave");
		} catch (@SuppressWarnings("unused") Exception e) {
			this.slave			= null;
			if(this.masterlists.size() > 1 && this.myPhysJVM != null) {
				try {
					getLessUsedJVMWithoutLB().jvnNewSlaveLoadBalancer();
					Thread.sleep(TIMEOUT);
					this.slave = (JvnLoadBalancer) this.rmiRegistry.lookup("JvnLoadBalancerSlave");
				} catch (Exception e1) {
					this.slave = null;
				}
			}
		}
		return this.slave;
	}

	@Override
	public void run() {
		heartbeat();
		try {
			Thread.sleep(REFRESH);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	synchronized private int getMaxInstancePerJVM() {
		assert(this.masterlists.size() == this.slavelists.size());
		return Math.floorDiv((this.numberOfCluster), this.masterlists.size()) + 1;
	}

	synchronized private boolean containSlave(int id) {
		for(Entry<JvnRemotePhysical, Map<Integer, JvnRemoteCoord>> slaveEntries : this.slavelists.entrySet()) {
			if(slaveEntries.getValue().containsKey(id)) {
				return true;
			}
		}
		return false;
	}

	synchronized private Set<Integer> getSetOfNoSlaveMaster() {
		Set<Integer> res = new HashSet<>();
		for(Entry<JvnRemotePhysical, Map<Integer, JvnRemoteCoord>> masterEntries : this.masterlists.entrySet()) {
			for(int idCoord : masterEntries.getValue().keySet()) {
				if(!containSlave(idCoord)) {
					res.add(idCoord);
				}
			}
		}
		return res;
	}

	synchronized private void launchMissingSlave() {
		if(this.slavelists.size() > 1) {
			for(int coordId : getSetOfNoSlaveMaster()) {
				try {
					getLessUsedJVMWithoutIdForSlave(coordId).jvnNewSlaveCoordInstance(coordId);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
		}
	}

	synchronized private void reArrangeCoords() {
		launchMissingSlave();
		int max = getMaxInstancePerJVM();
		for(JvnRemotePhysical physJVM : this.slavelists.keySet()) {
			int amountToMove = (this.masterlists.get(physJVM).size() + this.slavelists.get(physJVM).size()) - max;
			for(Entry<Integer, JvnRemoteCoord> slaveToMove : this.slavelists.get(physJVM).entrySet()) {
				if(amountToMove > 0) {
					JvnRemotePhysical candidat = getLessUsedJVMWithoutIdForSlave(slaveToMove.getKey());
					if(candidat != null && (this.masterlists.get(candidat).size() + this.slavelists.get(candidat).size()) <= max) {
						try {
							slaveToMove.getValue().kill();
							candidat.jvnNewSlaveCoordInstance(slaveToMove.getKey());
						} catch (RemoteException | JvnException e) {
							e.printStackTrace();
						}
						this.masterlists.get(physJVM).remove(slaveToMove.getKey());
						amountToMove--;
					}
				}
			}
			for(Entry<Integer, JvnRemoteCoord> masterToMove : this.masterlists.get(physJVM).entrySet()) {
				if(amountToMove > 0) {
					JvnRemotePhysical candidat = getLessUsedJVMWithoutIdForSlave(masterToMove.getKey());
					JvnRemotePhysical slaveOfIt = getSlaveOfMaster(masterToMove.getKey());
					if(slaveOfIt != null && candidat != null && (this.masterlists.get(candidat).size() + this.slavelists.get(candidat).size()) <= max) {
						try {
							masterToMove.getValue().kill();
							this.slavelists.get(slaveOfIt).get(masterToMove.getKey()).upgrade();
							candidat.jvnNewSlaveCoordInstance(masterToMove.getKey());
						} catch (RemoteException | JvnException e) {
							e.printStackTrace();
						}
						this.masterlists.get(physJVM).remove(masterToMove.getKey());
						amountToMove--;
					}
				}
			}
		}
	}

	synchronized private JvnRemotePhysical getSlaveOfMaster(Integer coordId) {
		for(Entry<JvnRemotePhysical, Map<Integer, JvnRemoteCoord>> test : this.slavelists.entrySet()) {
			if(test.getValue().containsKey(coordId)) {
				return test.getKey();
			}
		}
		return null;
	}

	synchronized public void registerSlave(JvnRemotePhysical physicalLayer, JvnRemoteCoord virtualCoord, int id) throws JvnException {
		JvnRemoteCoord mustBeNull = this.slavelists.get(physicalLayer).put(id,virtualCoord);
		assert(mustBeNull == null);
		reArrangeCoords();
		updateSlave();
	}

	synchronized public void registerMaster(JvnRemotePhysical physicalLayer, JvnRemoteCoord virtualCoord, int id) throws JvnException {
		JvnRemoteCoord mustBeNull = this.masterlists.get(physicalLayer).put(id,virtualCoord);
		assert(mustBeNull == null);
		reArrangeCoords();
		updateSlave();
	}

	synchronized public void removePhysicalLayer(JvnRemotePhysical physicalLayer) {
		this.masterlists.remove(physicalLayer);
		this.slavelists.remove(physicalLayer);
		reArrangeCoords();
		updateSlave();
	}

	synchronized public void addPhysicalLayer(JvnRemotePhysical physicalLayer) {
		this.masterlists.put(physicalLayer, new ConcurrentHashMap<>());
		this.slavelists.put(physicalLayer, new ConcurrentHashMap<>());
		reArrangeCoords();
		updateSlave();
	}

	/**
	 * 
	 * @param id
	 * @param physRemote
	 * @throws JvnException si il existe déjà un slave pour ce cluster
	 */
	synchronized private void startNewCoord(int id, JvnRemotePhysical physRemote) throws JvnException {

		// lancement du coordinateur master
		try {
			physRemote.jvnNewMasterCoordInstance(id);
		} catch (RemoteException e) {
			e.printStackTrace();
		}

		// enregistrement du coordinateur
		try {
			this.masterlists.get(physRemote).put(id,(JvnRemoteCoord) LocateRegistry.getRegistry().lookup("JvnCoord_"+id));
		} catch (RemoteException | NotBoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * initialize les cluster
	 * Appelé par le permier master loadbalancer
	 * on créer des slave sur la seule JVM qui existe
	 */
	synchronized public void initialize() {
		assert(this.slavelists.size() == 1);
		assert(this.masterlists.size() == 1);

		Entry<JvnRemotePhysical, Map<Integer, JvnRemoteCoord>> physCoord = this.masterlists.entrySet().iterator().next();
		for(int i = 0; i < this.numberOfCluster; i++) {
			try {
				startNewCoord(i,physCoord.getKey());
			} catch (JvnException e) {
				e.printStackTrace();
			}
		}
		updateSlave();
	}

	synchronized private void heartbeat() {
		for(JvnRemotePhysical frp : this.masterlists.keySet()) {
			try {
				frp.ping();
			} catch (@SuppressWarnings("unused") RemoteException e) {
				// on récupère la liste des id des coordinateurs qui ont planté
				Set<Integer> toUpgrade = this.masterlists.get(frp).keySet();

				//cleanup list
				this.slavelists.remove(frp);
				this.masterlists.remove(frp);

				//on upgrade chaque coordinateur slave correspondant à un id de la liste et on l'ajoute à la masterlist
				for(Entry<JvnRemotePhysical, Map<Integer, JvnRemoteCoord>> remainingJVM : this.slavelists.entrySet()) {
					for(Integer idCoord : toUpgrade) {
						if(remainingJVM.getValue().containsKey(idCoord)) {
							try {
								remainingJVM.getValue().get(idCoord).upgrade();
								this.masterlists.get(remainingJVM.getKey()).put(idCoord, remainingJVM.getValue().get(idCoord));
								remainingJVM.getValue().remove(idCoord);
							} catch (RemoteException | JvnException e1) {
								e1.printStackTrace();
							}
						}
					}
					if(remainingJVM.getValue().isEmpty()) {
						this.slavelists.remove(remainingJVM.getKey());
					}
				}
				// on créer des slave pour ceux qui n'en n'ont pas
				launchMissingSlave();
				updateSlave();
			}
		}
	}

	synchronized protected void updateSlave() {
		if(this.slave == null) {
			getSlaveLoadBalancer();
		}
		if(this.slave != null) {
			try {
				this.slave.updateJvnCoordMap(this);
			} catch (@SuppressWarnings("unused") Exception e) {
				try {Thread.sleep(TIMEOUT);} catch (@SuppressWarnings("unused") Exception e1) {}
				try {
					System.out.println("<1>");
					getSlaveLoadBalancer();
					System.out.println("<2>");
					if(this.slave != null) {
						System.out.println("<3>");
						this.slave.updateJvnCoordMap(this);
						System.out.println("<4>");
					}
					System.out.println("<5>");
				}catch (Exception e1) {this.slave = null;}
			}
		}
	}

	private JvnRemotePhysical getLessUsedJVMWithoutIdForSlave(int id) {
		JvnRemotePhysical res = null;
		int instanceOnRes = Integer.MAX_VALUE;
		int currentInstance = 0;
		for(JvnRemotePhysical key : this.masterlists.keySet()) {
			currentInstance = this.masterlists.get(key).size() + this.slavelists.get(key).size();
			if(currentInstance < instanceOnRes) {
				if(!this.masterlists.get(key).containsKey(id)) {
					res = key;
					instanceOnRes = currentInstance;
				}
			}
		}
		return res;
	}

	private JvnRemotePhysical getLessUsedJVMWithoutLB() {
		JvnRemotePhysical res = null;
		int instanceOnRes = Integer.MAX_VALUE;
		int currentInstance = 0;
		for(JvnRemotePhysical key : this.masterlists.keySet()) {
			currentInstance = this.masterlists.get(key).size() + this.slavelists.get(key).size();
			if(this.myPhysJVM != key && currentInstance < instanceOnRes) {
				res = key;
				instanceOnRes = currentInstance;
			}
		}
		return res;
	}
}
