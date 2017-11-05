package jvn.jvnCoord.jvnLoadBalancer;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

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

	private final Map<JvnRemotePhysical, Set<Integer>> masterlists;

	private final Map<JvnRemotePhysical, Set<Integer>> slavelists;

	private final int numberOfCluster;

	private transient JvnLoadBalancer slave;

	private transient Registry rmiRegistry;

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
		this.masterlists		= new LinkedHashMap<>(); // TODO debug seulement (ordre), changer par une hashmap classic en prod
		this.slavelists	 	 	= new LinkedHashMap<>();	
		this.numberOfCluster	= numberOfCluster;
		getSlaveLoadBalancer();
	}
	
	synchronized protected void newSlaveLoadBalancer() throws RemoteException {
		try {
			getLessUsedJVMWithoutLB().jvnNewSlaveLoadBalancer();
		}catch (@SuppressWarnings("unused") NullPointerException e) {
			// there is no jvm available, do nothing
		}
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
			this.slave.ping();
		} catch (@SuppressWarnings("unused") Exception e) {
			this.slave			= null;
			if(this.masterlists.size() > 1) {
				try {
					newSlaveLoadBalancer();
					Thread.sleep(TIMEOUT);
					this.slave = (JvnLoadBalancer) this.rmiRegistry.lookup("JvnLoadBalancerSlave");
				} catch (Exception e1) {
					System.out.println(e1.getMessage());
					this.slave = null;
				}
			}
		}
		return this.slave;
	}

	@Override
	public void run() {
		while(!isInterrupted()) {
			heartbeat();
			try {
				Thread.sleep(REFRESH);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	synchronized private int getMaxInstancePerJVM() {
		assert(this.masterlists.size() == this.slavelists.size());
		return (int) Math.ceil(2 * Float.valueOf(this.numberOfCluster) / Float.valueOf(this.masterlists.size()));
	}

	synchronized private boolean haveSlave(int id) {
		for(Set<Integer> slaves : this.slavelists.values()) {
			if(slaves.contains(id)) {
				return true;
			}
		}
		return false;
	}

	synchronized private Set<Integer> getSetOfNoSlaveMaster() {
		Set<Integer> res = new HashSet<>();
		for(Set<Integer> masterEntries : this.masterlists.values()) {
			for(int idCoord : masterEntries) {
				if(!haveSlave(idCoord)) {
					res.add(idCoord);
				}
			}
		}
		return res;
	}

	synchronized private void launchMissingSlave() {
		if(this.slavelists.size() > 1) {
			Set<Integer> noSlaveMaster = getSetOfNoSlaveMaster();
			for(int coordId : noSlaveMaster) {
				try {
					JvnRemotePhysical jvm = getLessUsedJVMWithoutIdForSlave(coordId);
					jvm.jvnNewSlaveCoordInstance(coordId);
					this.slavelists.get(jvm).add(coordId);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
		}
	}
	

	synchronized protected void reArrangeCoords() {
		launchMissingSlave();
		int max = getMaxInstancePerJVM();
		for(JvnRemotePhysical oldPhysJVM : this.slavelists.keySet()) {
			int amountToMove = (this.masterlists.get(oldPhysJVM).size() + this.slavelists.get(oldPhysJVM).size()) - max;
			
			for (Iterator<Integer> slaveToMoves = this.slavelists.get(oldPhysJVM).iterator(); slaveToMoves.hasNext();){
				int slaveToMove = slaveToMoves.next();
				if(amountToMove > 0) {
					JvnRemotePhysical candidatNewJVM = getLessUsedJVMWithoutIdForSlave(slaveToMove);
					if(candidatNewJVM != null && (this.masterlists.get(candidatNewJVM).size() + this.slavelists.get(candidatNewJVM).size()) < max) {
						try {
							oldPhysJVM.killCoord(slaveToMove);
							candidatNewJVM.jvnNewSlaveCoordInstance(slaveToMove);
							this.slavelists.get(candidatNewJVM).add(slaveToMove);
						} catch (RemoteException e) {
							e.printStackTrace();
						}
						slaveToMoves.remove();
						amountToMove--;
					}
				}
			}
		}
		
		for(JvnRemotePhysical oldPhysJVM : this.masterlists.keySet()) {
			int amountToMove = (this.masterlists.get(oldPhysJVM).size() + this.slavelists.get(oldPhysJVM).size()) - max;
			for (Iterator<Integer> masterToMoves = this.masterlists.get(oldPhysJVM).iterator(); masterToMoves.hasNext();){
				int masterToMove = masterToMoves.next(); 
				if(amountToMove > 0) {
					JvnRemotePhysical candidatNewJVM = getLessUsedJVMWithoutIdForSlave(masterToMove);
					JvnRemotePhysical slaveOfIt = getSlaveOfMaster(masterToMove);
					if(slaveOfIt != null && candidatNewJVM != null && (this.masterlists.get(candidatNewJVM).size() + this.slavelists.get(candidatNewJVM).size()) < max) {
						try {
							oldPhysJVM.killCoord(masterToMove);
							slaveOfIt.upgradeCoord(masterToMove);
							candidatNewJVM.jvnNewSlaveCoordInstance(masterToMove);
							this.slavelists.get(slaveOfIt).remove(masterToMove);
							this.masterlists.get(slaveOfIt).add(masterToMove);
							this.slavelists.get(candidatNewJVM).add(masterToMove);
						} catch (RemoteException e) {
							e.printStackTrace();
						}
						masterToMoves.remove();
						amountToMove--;
					}
				}
			}
		}
	}

	@SuppressWarnings("unused")
	private void printMap() {
		System.out.println("master:");
		for(Set<Integer> s: this.masterlists.values()) {
			System.out.println(s);
		}
		System.out.println("slaves:");
		for(Set<Integer> s: this.slavelists.values()) {
			System.out.println(s);
		}
	}

	synchronized private JvnRemotePhysical getSlaveOfMaster(Integer coordId) {
		for(Entry<JvnRemotePhysical, Set<Integer>> test : this.slavelists.entrySet()) {
			if(test.getValue().contains(coordId)) {
				return test.getKey();
			}
		}
		return null;
	}

	/**
	 * supprime de la base une couche physique (aucune autre action) et reorganize les coordinateur
	 * @param physicalLayer une jvm physique qui n'existe plus
	 */
	synchronized public void removePhysicalLayer(JvnRemotePhysical physicalLayer) {
		this.masterlists.remove(physicalLayer);
		this.slavelists.remove(physicalLayer);
		reArrangeCoords();
		updateSlave();
	}

	/**
	 * Ajoute dans la base une nouvelle JVM physique et reorganize les coordinateurs
	 * @param physicalLayer une nouvelle jvm physique
	 */
	synchronized public void addPhysicalLayer(JvnRemotePhysical physicalLayer) {
		this.masterlists.put(physicalLayer, new HashSet<>());
		this.slavelists.put(physicalLayer, new HashSet<>());
		reArrangeCoords();
		updateSlave();
	}

	/**
	 * 
	 * @param id
	 * @param physRemote
	 * @throws JvnException si il existe déjà un slave pour ce cluster
	 */
	synchronized private void startMasterCoord(int id, JvnRemotePhysical physRemote) throws JvnException {

		// lancement du coordinateur master
		try {
			physRemote.jvnNewMasterCoordInstance(id);
		} catch (RemoteException e) {
			e.printStackTrace();
		}

		// enregistrement du coordinateur
		this.masterlists.get(physRemote).add(id);
	}

	/**
	 * initialize les cluster
	 * Appelé par le permier master loadbalancer
	 * on créer des slave sur la seule JVM qui existe
	 */
	synchronized public void initialize() {
		assert(this.slavelists.size() == 1);
		assert(this.masterlists.size() == 1);

		Entry<JvnRemotePhysical, Set<Integer>> physCoord = this.masterlists.entrySet().iterator().next();
		for(int i = 0; i < this.numberOfCluster; i++) {
			try {
				startMasterCoord(i,physCoord.getKey());
			} catch (JvnException e) {
				e.printStackTrace();
			}
		}
		updateSlave();
	}

	synchronized protected void heartbeat() {
		List<JvnRemotePhysical> tempDead = new LinkedList<>();
		for(JvnRemotePhysical frp : this.masterlists.keySet()) {	
			try {
				frp.ping();
			} catch (@SuppressWarnings("unused") Exception e) {
				tempDead.add(frp);
			}
		}
		
		for(JvnRemotePhysical dead : tempDead) {
			// on récupère la liste des id des coordinateurs qui ont planté
			Set<Integer> toUpgrade = this.masterlists.get(dead);

			//cleanup list
			this.slavelists.remove(dead);
			this.masterlists.remove(dead);

			//on upgrade chaque coordinateur slave correspondant à un id de la liste et on l'ajoute à la masterlist
			for(Entry<JvnRemotePhysical, Set<Integer>> remainingJVM : this.slavelists.entrySet()) {
				for (Iterator<Integer> coordIds = toUpgrade.iterator(); coordIds.hasNext();){
					int coordId = coordIds.next();
					if(remainingJVM.getValue().contains(coordId)) {
						try {
							remainingJVM.getKey().upgradeCoord(coordId);
							this.masterlists.get(remainingJVM.getKey()).add(coordId);
							this.slavelists.get(remainingJVM.getKey()).remove(coordId);
							coordIds.remove();
						} catch (RemoteException e1) {
							e1.printStackTrace();
							// sa peut planter ici!
						}
					}
				}
			}
			// on créer des slave pour ceux qui n'en n'ont pas
			launchMissingSlave();
			updateSlave();
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
					if(getSlaveLoadBalancer() != null) {
						this.slave.updateJvnCoordMap(this);
					}
				}catch (Exception e1) {
					e1.printStackTrace();
					this.slave = null;
				}
			}
		}
	}

	private JvnRemotePhysical getLessUsedJVMWithoutIdForSlave(int id) {
		JvnRemotePhysical res	= null;
		int instanceOnRes 		= Integer.MAX_VALUE;
		int currentInstance 	= 0;
		for(JvnRemotePhysical key : this.masterlists.keySet()) {
			currentInstance 	= this.masterlists.get(key).size() + this.slavelists.get(key).size();
			if(currentInstance < instanceOnRes) {
				if(!this.masterlists.get(key).contains(id) && !this.slavelists.get(key).contains(id)) {
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
			try {
				if(currentInstance < instanceOnRes && !key.isLoadBalancer()) {
					res = key;
					instanceOnRes = currentInstance;
				}
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		return res;
	}
}
