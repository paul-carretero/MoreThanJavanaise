package jvn.jvnCoord.jvnLoadBalancer;

import java.io.Serializable;

import jvn.jvnCoord.JvnRemoteCoord;

/**
 * @author Paul Carretero
 * Information pour qu'un coordinateur logique puisse s'initializer
 */
public class JvnCoordData implements Serializable{

	private static final long			serialVersionUID = 8934045903559447084L;
	private final int					JvnLoadBalancingId;
	private final JvnCurrentRole		role;
	private final JvnRemoteCoord 		slave;
	private final JvnRemoteCoord 		master;


	/**
	 * @param jvnCoordName
	 * @param role
	 * @param slave
	 * @param master
	 */
	public JvnCoordData(int JvnLoadBalancingId, JvnCurrentRole role, JvnRemoteCoord slave, JvnRemoteCoord master) {
		super();
		this.JvnLoadBalancingId	= JvnLoadBalancingId;
		this.role				= role;
		this.slave				= slave;
		this.master				= master;
	}
	
	/**
	 * @return le nom du coordinateur (RMI)
	 */
	public int getJvnLoadBalancingId() {
		return this.JvnLoadBalancingId;
	}

	/**
	 * @return le role de ce coordinateur
	 */
	public JvnCurrentRole getRole() {
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
