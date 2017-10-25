package tests;

import static org.junit.Assert.*;

import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import jvn.jvnCoord.jvnPhysicalLayer.JvnRemotePhysicalImpl;
import jvn.jvnExceptions.JvnException;
import jvn.jvnExceptions.JvnObjectNotFoundException;
import jvn.jvnExceptions.JvnProxyException;
import jvn.proxy.JvnProxy;

public class JvnRedundantStorageTest {

	private static final Map<Integer,JvnRemotePhysicalImpl> physLayer 	= new HashMap<Integer,JvnRemotePhysicalImpl>();
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		//physLayer.put(0,new JvnRemotePhysicalImpl());
		//physLayer.put(1,new JvnRemotePhysicalImpl());
		Thread.sleep(2000);
	}

	@After
	public void tearDown() throws AccessException, RemoteException, NotBoundException, JvnException {
		for(JvnRemotePhysicalImpl p : physLayer.values()) {
			//p.destroy();
		}
		LocateRegistry.getRegistry().unbind("JvnLoadBalancer");
	}

	@Test(timeout = 5000)
	public void simpleTestNoFail() throws IllegalArgumentException, JvnObjectNotFoundException, JvnException, JvnProxyException {
		TestObjectItf o1 = (TestObjectItf) JvnProxy.newInstance(new TestObject(), "object1");
		TestObjectItf o2 = (TestObjectItf) JvnProxy.newInstance(new TestObject(), "object2");
		o1.setS("o1String");
		o2.setS("o2String");
		assertEquals("o1String", o1.getS());
		assertEquals("o2String", o2.getS());
	}
	
	@Test(timeout = 30000)
	public void massRegisterTestNoFail() throws IllegalArgumentException, JvnObjectNotFoundException, JvnException, JvnProxyException {
		int iter = 1000;
		List<TestObjectItf> list = new ArrayList<>();
		for(int i = 0; i < iter; i++) {
			list.add((TestObjectItf) JvnProxy.newInstance(new TestObject(), String.valueOf(i)));
		}
		for(int i = 0; i < iter; i++) {
			list.get(i).setS("updated-" + i);
		}
		for(int i = 0; i < iter; i++) {
			assertEquals("updated-" + i, list.get(i).getS());
		}
	}

}
