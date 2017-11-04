package tests;

import static org.junit.Assert.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import jvn.jvnExceptions.JvnException;
import jvn.jvnServer.JvnServerImpl;
import jvn.proxy.JvnProxy;
import tests.testObjects.StringObject;
import tests.testObjects.StringObjectItf;

public class JvnLocalServerTest implements Serializable{

	private static final long serialVersionUID 	= -6778422767501106604L;
	private static final int 	ITERATION 		= 200;
	private final static List<Integer> actualObject	= new ArrayList<Integer>();

	@BeforeClass
	public static void setUp() throws Exception {
		populate();
	}
	
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		JvnServerImpl.jvnGetServer().jvnTerminate();
	}

	private static void populate() throws JvnException {
		for (Integer i = 0; i < ITERATION; i++) {
			JvnProxy.newInstance(new StringObject(i.toString()), i.toString());
			actualObject.add(i);
		}
	}

	@Test(timeout = 1000)
	public void localBurnNotNull() throws JvnException {
		for (Integer i = 0; i < ITERATION; i++) {
			String res = ((StringObjectItf) JvnProxy.getRemoteInstance(StringObject.class, i.toString())).getS();
			assertNotNull("Vérification que les objets ont bien été créé", res);
		}
	}

	@Test(timeout = 1000)
	public void localBurnEqual() throws JvnException {
		for (Integer i = 0; i < ITERATION; i++) {
			String res = ((StringObjectItf) JvnProxy.getRemoteInstance(StringObject.class, i.toString())).getS();
			assertEquals("Vérification que les objets créé sont OK", i.toString(), res);
		}
	}

	@Test(timeout = 1000)
	public void localLRUCacheTest() throws JvnException {
		long start;
		long end;
		long tps1;
		long tps2;

		// "0" n'est plus en cache
		start = System.nanoTime();
		JvnProxy.getRemoteInstance(StringObject.class, "0");
		end = System.nanoTime();
		tps1 = end-start;

		// "ITERATION" est en cache
		start = System.nanoTime();
		JvnProxy.getRemoteInstance(StringObject.class, String.valueOf(ITERATION-1));
		end = System.nanoTime();
		tps2 = end-start;

		assertTrue("temps d'acces en cache plus petit", tps1 > tps2);

		// "1" n'est plus en cache
		start = System.nanoTime();
		JvnProxy.getRemoteInstance(StringObject.class, "1");
		end = System.nanoTime();
		tps1 = end-start;

		// "ITERATION - 1" est en cache
		start = System.nanoTime();
		JvnProxy.getRemoteInstance(StringObject.class, String.valueOf(ITERATION-2));
		end = System.nanoTime();
		tps2 = end-start;

		assertTrue("temps d'acces en cache plus petit", tps1 > tps2);

		// "2" n'est plus en cache
		start = System.nanoTime();
		JvnProxy.getRemoteInstance(StringObject.class, "2");
		end = System.nanoTime();
		tps1 = end-start;

		// "ITERATION - 2" est en cache
		start = System.nanoTime();
		JvnProxy.getRemoteInstance(StringObject.class, String.valueOf(ITERATION-3));
		end = System.nanoTime();
		tps2 = end-start;

		assertTrue("temps d'acces en cache plus petit", tps1 > tps2);
	}

}