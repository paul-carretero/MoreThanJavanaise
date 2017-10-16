package tests;

import static org.junit.Assert.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import jvn.jvnExceptions.JvnException;
import jvn.jvnObject.JvnObject;
import jvn.jvnServer.JvnServerImpl;

public class JvnLocalServerTest implements Serializable{

	private static final long serialVersionUID = -6778422767501106604L;
	private static final int 	ITERATION 		= 200;
	private final JvnServerImpl js 				= JvnServerImpl.jvnGetServer();
	private final List<Integer> actualObject	= new ArrayList<Integer>();

	@Before
	public void setUp() throws Exception {
		this.js.clearCache(true);
		assertNull("Vérification que les serveurs sont vide", this.js.jvnLookupObject("0"));
		populate();
	}
	
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		JvnServerImpl.jvnGetServer().jvnTerminate();
	}

	private void populate() throws JvnException {
		for (Integer i = 0; i < ITERATION; i++) {
			this.actualObject.add(i);
			JvnObject o = this.js.jvnCreateObject(i.toString());
			this.js.jvnRegisterObject(i.toString(), o);
			o.jvnUnLock();
		}
	}

	@Test(timeout = 1000)
	public void localBurnNotNull() throws JvnException {
		for (Integer i = 0; i < ITERATION; i++) {
			assertNotNull("Vérification que les objets ont bien été créé", this.js.jvnLookupObject(i.toString()));
		}
	}

	@Test(timeout = 1000)
	public void localBurnEqual() throws JvnException {
		for (Integer i = 0; i < ITERATION; i++) {
			assertEquals("Vérification que les objets créé sont OK", i.toString(), this.js.jvnLookupObject(i.toString()).jvnGetObjectState().toString());
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
		this.js.jvnLookupObject("0");
		end = System.nanoTime();
		tps1 = end-start;

		// "ITERATION" est en cache
		start = System.nanoTime();
		this.js.jvnLookupObject(String.valueOf(ITERATION));
		end = System.nanoTime();
		tps2 = end-start;

		assertTrue("temps d'acces en cache plus petit", tps1 > tps2);

		// "1" n'est plus en cache
		start = System.nanoTime();
		this.js.jvnLookupObject("1");
		end = System.nanoTime();
		tps1 = end-start;

		// "ITERATION - 1" est en cache
		start = System.nanoTime();
		this.js.jvnLookupObject(String.valueOf(ITERATION-1));
		end = System.nanoTime();
		tps2 = end-start;

		assertTrue("temps d'acces en cache plus petit", tps1 > tps2);

		// "2" n'est plus en cache
		start = System.nanoTime();
		this.js.jvnLookupObject("2");
		end = System.nanoTime();
		tps1 = end-start;

		// "ITERATION - 2" est en cache
		start = System.nanoTime();
		this.js.jvnLookupObject(String.valueOf(ITERATION-2));
		end = System.nanoTime();
		tps2 = end-start;

		assertTrue("temps d'acces en cache plus petit", tps1 > tps2);
	}

}