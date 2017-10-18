package jvn.jvnCoord.jvnLoadBalancer;

import jvn.jvnCoord.JvnRemoteCoord;
import jvn.jvnCoord.jvnPhysicalLayer.JvnRemotePhysical;

public class JvnCoordCluster {
	
	/**
	 * Id de loadBalancing (pour le modulo)
	 */
	private final int loadId;
	/**
	 * Coordinateur principal
	 */
	private JvnRemoteCoord master;
	/**
	 * Coordinateur de fall-back
	 */
	private JvnRemoteCoord slave;
	/**
	 * Coordinateur principal
	 */
	private JvnRemotePhysical PhysicalMasterDB;
	/**
	 * Coordinateur de fall-back
	 */
	private JvnRemotePhysical PhysicalSlaveDB;
	/**
	 * @param loadId
	 * @param master
	 * @param slave
	 * @param physicalMasterDB
	 * @param physicalSlaveDB
	 */
	public JvnCoordCluster(int loadId, JvnRemoteCoord master, JvnRemoteCoord slave,	JvnRemotePhysical physicalMasterDB, JvnRemotePhysical physicalSlaveDB) {
		super();
		this.loadId	= loadId;
		this.setMaster(master);
		this.setSlave(slave);
		this.setPhysicalMasterDB(physicalMasterDB);
		this.setPhysicalSlaveDB(physicalSlaveDB);
	}
	
	/**
	 * @return le serveur DB physique supportant le coordinateur slave
	 */
	public JvnRemotePhysical getPhysicalSlaveDB() {
		return this.PhysicalSlaveDB;
	}
	
	/**
	 * @param physicalSlaveDB un serveur physique supportant un coordinateur slave
	 */
	public void setPhysicalSlaveDB(JvnRemotePhysical physicalSlaveDB) {
		this.PhysicalSlaveDB = physicalSlaveDB;
	}
	
	public JvnRemotePhysical getPhysicalMasterDB() {
		return this.PhysicalMasterDB;
	}
	
	public void setPhysicalMasterDB(JvnRemotePhysical physicalMasterDB) {
		this.PhysicalMasterDB = physicalMasterDB;
	}
	
	public JvnRemoteCoord getSlave() {
		return this.slave;
	}
	
	public void setSlave(JvnRemoteCoord slave) {
		this.slave = slave;
	}
	
	public JvnRemoteCoord getMaster() {
		return this.master;
	}
	
	public void setMaster(JvnRemoteCoord master) {
		this.master = master;
	}
	
	public int getLoadId() {
		return this.loadId;
	}

	

}
