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

/**
 * @author Paul Carretero
 * Représentation logique de l'ensemble des Coordinateurs disponible pour un loadbalancer.
 * Cette représentation contient les master et les slave
 * Les master et les slaves sont associés à la machine physique qui les a lancée
 * En temps que thread, la JvnCoordMapva également envoyer régulièrement un ping à chaqune des machine physique afin
 * de vérifier si celle ci est toujours active
 * En fonction des besoin des methodes permetterons de déployer d'autre 
 * Coordinateurs (master ou slave) sur les machines physique disponible
 */
public class JvnCoordMap extends Thread implements Serializable {

	/**
	 * Délai entre deux vérification des machine physique
	 */
	private static final int REFRESH = 1000;
	
	/**
	 * Délai avant une nouvelle tentative en cas d'echec de connexion
	 */
	private static final int TIMEOUT = 3000;
	
	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = -7390263206317147214L;

	/**
	 * Map associant une machine physique avec un ensemble d'id 
	 * auxquels des coordinateur master de cette machine physique sont associé
	 */
	private final Map<JvnRemotePhysical, Set<Integer>> masterlists;

	/**
	 * Map associant une machine physique avec un ensemble d'id 
	 * auxquels des coordinateur slave de cette machine physique sont associé
	 */
	private final Map<JvnRemotePhysical, Set<Integer>> slavelists;

	/**
	 * Nombre de coordinateur (nombre d'id)
	 */
	private final int numberOfCluster;

	/**
	 * Slave du loadbalancer sur lequelle dupliquer les opérations
	 */
	private transient JvnLoadBalancer slave;

	/**
	 * Registre rmi local
	 */
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
	
	/**
	 * Tente de lancer un loadbalancer slave sur une machine physique
	 * Selectionne de préférence une peu chargé en coordinateur
	 * @throws RemoteException
	 */
	synchronized protected void newSlaveLoadBalancer() throws RemoteException {
		try {
			getLessUsedJVMWithoutLB().jvnNewSlaveLoadBalancer();
		}catch (@SuppressWarnings("unused") NullPointerException e) {}
	}

	/**
	 * recherche et re-défini le loadbalancer slave (par exemple en cas d'echec des communications)
	 * @return le loadbalancer slave mis à jour
	 */
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

	/**
	 * Calcul le nombre maximum de coordinateur par jvm en fonction du nombre de coordinateur souhaité
	 * @return le nombre maximum d'instance de coordinateur 
	 * d'une machine physique peut supporté afin de garantir un bon équilibrage des charges
	 */
	synchronized private int getMaxInstancePerJVM() {
		assert(this.masterlists.size() == this.slavelists.size());
		return (int) Math.ceil(2 * Float.valueOf(this.numberOfCluster) / Float.valueOf(this.masterlists.size()));
	}

	/**
	 * @param id un id de coordinateur master
	 * @return true si ce coordinateur à un slave, false sinon
	 */
	synchronized private boolean haveSlave(int id) {
		for(Set<Integer> slaves : this.slavelists.values()) {
			if(slaves.contains(id)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @return un Set des id de coordinateur master n'ayant aucun slave (sans redondance)
	 */
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

	/**
	 * Lance si possible des slaves pour les coordinateur master sans slave (sans redondance)
	 * Ne fait rien si seulement une machine physique est disponible
	 */
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
	

	/**
	 * Methode permettant de re-organiser les coordinateur sur les différentes machine physique
	 * Si possible, chaque coordinateur sera en double (master+slave)
	 * le nombre de coordinateur entre chaque machine physique sera réparti au mieux afin d'équilibrer la charge
	 */
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

	/**
	 * @param coordId un id de coordinateur master
	 * @return le JvnRemotePhysical associé au slave de ce coordinateur master (son id)
	 */
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
	 * Lance un coordinateur master ayant l'id spécifié sur une machine physique
	 * @param id un id de coordinateur master
	 * @param physRemote une machine physique
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

	/**
	 * Envoie un ping à chaque machine physique.
	 * Si le ping echoue alors supprime la machine physique des liste des coordinateur slave et master
	 * Les slaves des masters supprimés seront upgrader en master afin de continuer les opérations
	 * Les slaves manquant aux masters seront également lancé sur d'autre machine physique si possible
	 */
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

	/**
	 * Envoie au loadbalancer slave une copie de cette classe afin de garantir la redondance
	 * En cas d'echec tentera de rechercher ou créer un autre loadbalancer slave
	 */
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

	/**
	 * Recherche la machine physique la moins utilisé et ne possédant pas de master pour un id de coordinateur donné
	 * @param id un id de coordinateur
	 * @return une machine physique sur laquelle lancer un slave ayant l'id spécifié
	 */
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

	/**
	 * @return une machine physique sans loadbalancer la moins utilisé
	 */
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
