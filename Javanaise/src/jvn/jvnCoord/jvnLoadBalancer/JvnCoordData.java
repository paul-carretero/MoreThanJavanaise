package jvn.jvnCoord.jvnLoadBalancer;

import java.io.Serializable;

import jvn.jvnCoord.JvnRemoteCoord;

/**
 * @author Paul Carretero
 * Information pour qu'un coordinateur puisse s'initializer
 */
public class JvnCoordData implements Serializable{

	private static final long			serialVersionUID = 8934045903559447084L;
	private final String				JvnCoordName;
	private final JvnCoordCurrentRole	role;
	private final JvnRemoteCoord 		slave;
	private final JvnRemoteCoord 		master;


	/**
	 * @param jvnCoordName
	 * @param role
	 * @param slave
	 * @param master
	 */
	public JvnCoordData(String jvnCoordName, JvnCoordCurrentRole role, JvnRemoteCoord slave, JvnRemoteCoord master) {
		super();
		this.JvnCoordName	= jvnCoordName;
		this.role			= role;
		this.slave			= slave;
		this.master			= master;
	}
	
	/**
	 * @return le nom du coordinateur (RMI)
	 */
	public String getJvnCoordName() {
		return this.JvnCoordName;
	}

	/**
	 * @return le role de ce coordinateur
	 */
	public JvnCoordCurrentRole getRole() {
		return this.role;
	}

	/**
	 * @return le coordinateur esclave de celui ci (nul si this.role == slave)
	 */
	public JvnRemoteCoord getSlave() {
		return this.slave;
	}

	/**
	 * @return le coordinateur esclave de celui ci (nul si this.role == master)
	 */
	public JvnRemoteCoord getMaster() {
		return this.master;
	}

}
