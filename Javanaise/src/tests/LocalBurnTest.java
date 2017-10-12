package tests;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import jvn.JvnException;
import jvn.JvnObject;
import jvn.JvnServerImpl;

public class LocalBurnTest {
	
	private static final int 	ITERATION 		= 200;
	private final JvnServerImpl js 				= JvnServerImpl.jvnGetServer();
	private final List<Integer> actualObject	= new ArrayList<Integer>();

	@Before
	public void setUp() throws Exception {
		this.js.clearCache(true);
		
		assertNull("Vérification que les serveurs sont vide", this.js.jvnLookupObject("0"));		
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
		populate();
		for (Integer i = 0; i < ITERATION; i++) {
			assertNotNull("Vérification que les objets ont bien été créé", this.js.jvnLookupObject(i.toString()));
		}
	}
	
	@Test(timeout = 1000)
	public void localBurnEqual() throws JvnException {
		populate();
		for (Integer i = 0; i < ITERATION; i++) {
			assertEquals("Vérification que les objets créé sont OK", i.toString(), this.js.jvnLookupObject(i.toString()).jvnGetObjectState().toString());
		}
	}
	
	@Test(timeout = 1000)
	public void localBurnAccess() throws JvnException {
		populate();
		long start;
		long end;
		long tps1;
		long tps2;
		
		// "0" n'est plus en cache
		start = System.currentTimeMillis();
		this.js.jvnLookupObject("0");
		end = System.currentTimeMillis();
		tps1 = end-start;
		
		// "ITERATION" est en cache
		start = System.currentTimeMillis();
		this.js.jvnLookupObject("0");
		end = System.currentTimeMillis();
		tps2 = end-start;
		
		assertTrue("temps d'acces en cache plus petit", tps1 > tps2);
	}

}