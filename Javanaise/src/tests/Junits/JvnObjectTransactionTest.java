package tests.Junits;

import static org.junit.Assert.*;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import jvn.jvnExceptions.JvnException;
import jvn.jvnExceptions.JvnPreemptiveInvalidationException;
import jvn.jvnExceptions.JvnTransactionException;
import jvn.jvnServer.JvnServerImpl;
import jvn.proxy.JvnProxy;
import tests.testObjects.StringObject;
import tests.testObjects.StringObjectItf;

/**
 * @author Paul Carretero
 * Suite de test permettant de vérifier le bon comportement d'une transaction (sur plusieurs objets)
 * Cette suite de test vérifie notament que les commits sont bien pris en compte ainsi que les rollback
 */
@SuppressWarnings("javadoc")
public class JvnObjectTransactionTest {
	
	static StringObjectItf JvnObjectTest1;
	static StringObjectItf JvnObjectTest2;
	static StringObjectItf JvnObjectTest3;
	static final Lock lockAppLevel1		= new ReentrantLock();
	static final Lock lockAppLevel2		= new ReentrantLock();
	static final Lock lockAppLevel3		= new ReentrantLock();
	static final int NUM_THREAD			= 50;
	static final CyclicBarrier barrier	= new CyclicBarrier(NUM_THREAD);
	
	
	@SuppressWarnings("static-method")
	@Before
	public void initialize() throws Exception {
		JvnObjectTest1 = ((StringObjectItf) JvnProxy.newInstance(new StringObject("JvnObjectTest1 - "), "JvnObjectTest-1"));
		JvnObjectTest2 = ((StringObjectItf) JvnProxy.newInstance(new StringObject("JvnObjectTest2 - "), "JvnObjectTest-2"));
		JvnObjectTest3 = ((StringObjectItf) JvnProxy.newInstance(new StringObject("JvnObjectTest3 - "), "JvnObjectTest-3"));
		JvnObjectTest1.setS("JvnObjectTest1 - ");
		JvnObjectTest2.setS("JvnObjectTest2 - ");
		JvnObjectTest3.setS("JvnObjectTest3 - ");
	}
	
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		JvnServerImpl.jvnGetServer().jvnTerminate();
	}
	
	class JvnObjectWorker extends Thread{
		private final int idThread;
		public JvnObjectWorker(int idThread) {
			this.idThread = idThread;
		}

		@Override
		public void run() {
			try {
				barrier.await();
				for (int i = 0; i < 100; i++) {
					lockAppLevel1.lock();
					String o1Str = JvnObjectTest1.getS();
					o1Str = o1Str.concat("-"+this.idThread);
					JvnObjectTest1.setS(o1Str);
					lockAppLevel1.unlock();
					
					lockAppLevel2.lock();
					String o2Str = JvnObjectTest2.getS();
					o2Str = o2Str.concat("-"+this.idThread);
					JvnObjectTest2.setS(o2Str);
					lockAppLevel2.unlock();
					
					lockAppLevel3.lock();
					String o3Str = JvnObjectTest3.getS();
					o3Str = o3Str.concat("-"+this.idThread);
					JvnObjectTest3.setS(o3Str);
					lockAppLevel3.unlock();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}

	@Test(timeout=10000)
	public void localConcurrencyTest() {
		List<Thread> runnableList = new LinkedList<Thread>();
		for (int i = 0; i < NUM_THREAD; i++) {
			runnableList.add(new JvnObjectWorker(i));
		}
		
		for (Thread t : runnableList) {
			t.start();
		}
		
		// so beautiful
		for (Thread t : runnableList) {
			while(t.isAlive()) {
				Thread.yield();
			}
		}
		
		assertEquals("Chaine de même longueur", JvnObjectTest1.getS().length(),JvnObjectTest2.getS().length());
		assertEquals("Chaine de même longueur", JvnObjectTest1.getS().length(),JvnObjectTest3.getS().length());
		assertTrue("Chaine de taille conhérente", JvnObjectTest1.getS().length() > NUM_THREAD);
	}
	
	@SuppressWarnings("static-method")
	@Test(timeout=10000)
	public void commitedTransactionTest() throws JvnException {
		JvnObjectTest1.setS("before-1");
		JvnObjectTest2.setS("before-2");
		JvnObjectTest3.setS("before-3");
		JvnServerImpl.jvnGetServer().beginTransaction();
		JvnObjectTest1.setS("random-1");
		JvnObjectTest2.getS();
		JvnObjectTest3.setS("after-3");
		JvnObjectTest1.setS("after-1");
		JvnServerImpl.jvnGetServer().commitTransaction();
		assertEquals("commit verification", "after-1", JvnObjectTest1.getS());
		assertEquals("commit verification", "before-2", JvnObjectTest2.getS());
		assertEquals("commit verification", "after-3", JvnObjectTest3.getS());
	}
	
	@SuppressWarnings("static-method")
	@Test(timeout=10000)
	public void rollbackTransactionTest() throws JvnPreemptiveInvalidationException, JvnException {
		JvnObjectTest1.setS("before-1");
		JvnObjectTest2.setS("before-2");
		JvnObjectTest3.setS("before-3");
		assertEquals("commit verification", "before-1", JvnObjectTest1.getS());
		assertEquals("commit verification", "before-2", JvnObjectTest2.getS());
		assertEquals("commit verification", "before-3", JvnObjectTest3.getS());
		JvnServerImpl.jvnGetServer().beginTransaction();
		JvnObjectTest1.setS("random-1");
		assertEquals("commit verification", "random-1", JvnObjectTest1.getS());
		JvnObjectTest2.getS();
		JvnObjectTest3.setS("after-3");
		assertEquals("commit verification", "after-3", JvnObjectTest3.getS());
		JvnObjectTest1.setS("after-1");
		assertEquals("commit verification", "after-1", JvnObjectTest1.getS());
		JvnServerImpl.jvnGetServer().rollbackTransaction();
		assertEquals("commit verification", "before-1", JvnObjectTest1.getS());
		assertEquals("commit verification", "before-2", JvnObjectTest2.getS());
		assertEquals("commit verification", "before-3", JvnObjectTest3.getS());
	}
	
	@SuppressWarnings("static-method")
	@Test(timeout=10000 , expected=JvnTransactionException.class)
	public void failTransactionTest() throws JvnException {
		JvnServerImpl.jvnGetServer().commitTransaction();
	}

}
