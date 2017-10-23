package jvn.jvnCoord.jvnLoadBalancer;

import java.io.Serializable;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import jvn.jvnCoord.JvnLogicalCoord.JvnRemoteCoord;
import jvn.jvnCoord.jvnPhysicalLayer.JvnRemotePhysical;
import jvn.jvnExceptions.JvnException;

public class JvnCoordMap extends Thread implements Serializable {

	// Coordinateurs Logique

	private static final int REFRESH = 1000;
	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = -7390263206317147214L;

	private final Map<JvnRemotePhysical, Map<Integer,JvnRemoteCoord>> masterlists;

	private final Map<JvnRemotePhysical, Map<Integer,JvnRemoteCoord>> slavelists;

	private final int numberOfCluster;

	/**
	 * Instancie la classe pour gérer les liste des coordinateur master et slave
	 * @param numberOfCluster nombre de cluster
	 */
	public JvnCoordMap(int numberOfCluster) {
		this.masterlists	 		= new HashMap<>();
		this.slavelists	 	 		= new HashMap<>();	
		this.numberOfCluster 		= numberOfCluster;
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
		System.out.println("<1>");
		int max = getMaxInstancePerJVM();
		System.out.println("<2>"+max);
		for(JvnRemotePhysical physJVM : this.slavelists.keySet()) {
			int amountToMove = (this.masterlists.get(physJVM).size() + this.slavelists.get(physJVM).size()) - max;
			System.out.println("<3>"+amountToMove);
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
			System.out.println("<4>"+amountToMove);
			for(Entry<Integer, JvnRemoteCoord> masterToMove : this.masterlists.get(physJVM).entrySet()) {
				if(amountToMove > 0) {
					System.out.println("<5>");
					JvnRemotePhysical candidat = getLessUsedJVMWithoutIdForSlave(masterToMove.getKey());
					JvnRemotePhysical slaveOfIt = getSlaveOfMaster(masterToMove.getKey());
					System.out.println("<6>");
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
	}

	synchronized public void registerMaster(JvnRemotePhysical physicalLayer, JvnRemoteCoord virtualCoord, int id) throws JvnException {
		JvnRemoteCoord mustBeNull = this.masterlists.get(physicalLayer).put(id,virtualCoord);
		assert(mustBeNull == null);
		reArrangeCoords();
	}

	synchronized public void removePhysicalLayer(JvnRemotePhysical physicalLayer) {
		this.masterlists.remove(physicalLayer);
		this.slavelists.remove(physicalLayer);
		reArrangeCoords();
	}

	synchronized public void addPhysicalLayer(JvnRemotePhysical physicalLayer) {
		this.masterlists.put(physicalLayer, new HashMap<>());
		this.slavelists.put(physicalLayer, new HashMap<>());
		reArrangeCoords();
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
				Set<Integer> NoSlaveCoords = new HashSet<Integer>();

				//on upgrade chaque coordinateur slave correspondant à un id de la liste
				for(Entry<JvnRemotePhysical, Map<Integer, JvnRemoteCoord>> remainingJVM : this.slavelists.entrySet()) {
					for(Integer id : toUpgrade) {
						if(remainingJVM.getValue().containsKey(id)) {
							try {
								remainingJVM.getValue().get(id).upgrade();
								remainingJVM.getValue().remove(id);
								NoSlaveCoords.add(id);
							} catch (RemoteException | JvnException e1) {
								e1.printStackTrace();
							}
						}
					}
					if(remainingJVM.getValue().isEmpty()) {
						this.slavelists.remove(remainingJVM.getKey());
					}
				}

				//on lance des slaves pour ces coordinateurs sur les physicalJVM qui ne dispose pas de leur master (et qui sont les plus vide)
				if(this.masterlists.size() > 1) {
					for(int id : NoSlaveCoords) {
						try {
							// ne devrait pas être null car > 1...
							getLessUsedJVMWithoutIdForSlave(id).jvnNewSlaveCoordInstance(id);
							// il s'enregistrera tout seul plus tard

						} catch (RemoteException e1) {
							e1.printStackTrace();
						}
					}
				}
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

	public void killAll(JvnRemotePhysical jvnRemotePhysical) {
		for(Entry<Integer, JvnRemoteCoord> slaveToKill : this.slavelists.get(jvnRemotePhysical).entrySet()) {
			try {
				slaveToKill.getValue().kill();
			} catch (RemoteException | JvnException e) {}
		}
		for(Entry<Integer, JvnRemoteCoord> masterToKill : this.masterlists.get(jvnRemotePhysical).entrySet()) {
			try {
				masterToKill.getValue().kill();
			} catch (RemoteException | JvnException e) {}
		}
	}
}
