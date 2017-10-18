package tests;

import static org.junit.Assert.*;

import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import jvn.jvnCoord.jvnLoadBalancer.JvnLoadBalancer;
import jvn.jvnCoord.jvnPhysicalLayer.JvnRemotePhysicalImpl;
import jvn.jvnExceptions.JvnException;

public class JvnRedundantLoadBalancingTest {
	
	private static final Map<Integer,JvnRemotePhysicalImpl> physLayer = new HashMap<Integer,JvnRemotePhysicalImpl>();
	private static Registry rmiRegistry;
	private static JvnLoadBalancer rmiLoadBalancer;
	
	

	@BeforeClass
	public static void setUpBeforeClass() throws Exception { 
		rmiRegistry = LocateRegistry.getRegistry("localhost");
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		for(JvnRemotePhysicalImpl p : physLayer.values()) {
			p.destroy();
		}
	}

	@Before
	public void setUp() throws Exception {
		physLayer.put(0,new JvnRemotePhysicalImpl());
		physLayer.put(1,new JvnRemotePhysicalImpl());
		physLayer.get(0).jvnNewMasterLoadBalancer();
		physLayer.get(1).jvnNewSlaveLoadBalancer();
		Thread.sleep(400);
		rmiLoadBalancer = (JvnLoadBalancer) JvnRedundantLoadBalancingTest.rmiRegistry.lookup("JvnLoadBalancer");
	}

	@After
	public void tearDown() {
		physLayer.get(0).destroy();
		physLayer.get(1).destroy();
	}

	@Test
	public void synchronizedValueTest() throws RemoteException, JvnException {
		rmiLoadBalancer.jvnGetObjectId();
		rmiLoadBalancer.jvnGetObjectId();
		rmiLoadBalancer.jvnGetObjectId();
		assertEquals(3, physLayer.get(1).jvnGetObjectId());
	}
	
	@Test(expected=NoSuchObjectException.class)
	public void masterFailBasicFailTest() throws NoSuchObjectException, RemoteException, JvnException {
		physLayer.get(0).destroy();
		rmiLoadBalancer.jvnGetObjectId();
	}
	
	@Test
	public void masterFailBasicNoFailTest() throws NoSuchObjectException, RemoteException, JvnException, InterruptedException {
		physLayer.get(0).destroy();
		Thread.sleep(3000);
		rmiLoadBalancer.jvnGetObjectId();
	}

}
