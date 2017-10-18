package jvn.jvnCoord.jvnLoadBalancer;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import jvn.jvnCoord.jvnPhysicalLayer.JvnRemotePhysical;

public class JvnCoordMap extends Thread implements Serializable {
	
	// Coordinateurs Logique
	
	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = -7390263206317147214L;

	/**
	 * temps entre deux heartbeats
	 */
	private static final int 	REFRESH_RATE = 1000;
	
	/**
	 * Map associant un int à une instance de coordinateur (master).
	 * un objet ayant (numberOfCoord % objectId) == Integer sera dans ce cluster
	 */
	private final Map<Integer,JvnCoordCluster> clusters;
	
	// Coordinateur Physique
	
	/**
	 * Nombre d'instance de JvnCoord par serveur DB physique
	 */
	private final Map<JvnRemotePhysical, Integer>	physicalCoord;

	/**
	 * Instancie la classe pour gérer les liste des coordinateur master et slave
	 */
	public JvnCoordMap() {
		this.clusters		= new ConcurrentHashMap<Integer,JvnCoordCluster>();	
		this.physicalCoord	= new ConcurrentHashMap<JvnRemotePhysical, Integer>();
	}

	public void addPhysicalLayer(JvnRemotePhysical physicalLayer) {
		this.physicalCoord.put(physicalLayer, 0);
	}
	
	public void addPhysicalLayer(JvnRemotePhysical physicalLayer, int count) {
		this.physicalCoord.put(physicalLayer, count);
	}
}
