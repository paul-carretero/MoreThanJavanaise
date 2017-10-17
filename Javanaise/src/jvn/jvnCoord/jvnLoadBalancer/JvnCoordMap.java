package jvn.jvnCoord.jvnLoadBalancer;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import jvn.jvnCoord.JvnRemoteCoord;
import jvn.jvnCoord.jvnDatabaseCoord.JvnRemotePhysicalCoord;

public class JvnCoordMap {
	
	// Coordinateurs Logique
	
	/**
	 * Map associant un int à une instance de coordinateur (master).
	 * un objet ayant (numberOfCoord % objectId) == int sera dans ce serveur
	 */
	private final Map<Integer,JvnRemoteCoord> 	masterCoords;
	
	/**
	 * Map associant un int à une instance de coordinateur (slave).
	 * un objet ayant (numberOfCoord % objectId) == int sera sauvegardé dans ce serveur
	 */
	private final Map<Integer,JvnRemoteCoord> 	slaveCoords;
	
	/**
	 *  associe un nom RMI à un id de remoteCoord
	 */
	private final Map<String,Integer>			assocMap;
	
	// Coordinateur Physique
	
	/**
	 * map associant un nom arbitraire d'un serveur physique (quelconque, juste pas en double...)
	 * et une instance d'un Coordinateur physique (pouvant éventuellement gérer)
	 */
	private final Map<String,JvnRemotePhysicalCoord>	physicalCoord;
	
	/**
	 * Map associant la liste des instance de coordinateur (master) avec un coordinateur physique
	 */
	private final Map<String,List<Integer>> 			CoordMasterInstance;
	
	/**
	 * Map associant la liste des instance de coordinateur (slave) avec un coordinateur physique
	 */
	private final Map<String,List<Integer>> 			CoordSlaveInstance;

	/**
	 * Instancie la classe pour gérer les liste des coordinateur master et slave
	 */
	public JvnCoordMap() {
		this.masterCoords	= new ConcurrentHashMap<Integer,JvnRemoteCoord>();
		this.slaveCoords	= new ConcurrentHashMap<Integer,JvnRemoteCoord>();
		this.assocMap		= new ConcurrentHashMap<String,Integer>();
		
		this.physicalCoord			= new ConcurrentHashMap<String,JvnRemotePhysicalCoord>();
		this.CoordMasterInstance	= new ConcurrentHashMap<String,List<Integer>>();
		this.CoordSlaveInstance		= new ConcurrentHashMap<String,List<Integer>>();
	}

}
