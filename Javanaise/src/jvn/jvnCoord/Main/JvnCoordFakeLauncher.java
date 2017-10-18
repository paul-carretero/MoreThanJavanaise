package jvn.jvnCoord.Main;

import java.util.HashMap;
import java.util.Map;

import jvn.jvnCoord.jvnPhysicalLayer.JvnRemotePhysical;
import jvn.jvnCoord.jvnLoadBalancer.JvnAbstractLoadBalancer;

public class JvnCoordFakeLauncher {
	
	private static final Map<Integer,JvnAbstractLoadBalancer> loadBalancer = new HashMap<Integer,JvnAbstractLoadBalancer>();
	private static final Map<Integer,JvnRemotePhysical> physicalCoord = new HashMap<Integer,JvnRemotePhysical>();

	/**
	 * Lance une simulation
	 * @param args null (menu instead)
	 * @throws Exception if something bad happens lol
	 */
	public static void main(String [] args) throws Exception {

	}

}
