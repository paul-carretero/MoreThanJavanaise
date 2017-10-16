package tests;

import static org.junit.Assert.*;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import jvn.jvnExceptions.JvnException;
import jvn.jvnObject.JvnObjectImpl;
import jvn.jvnServer.JvnServerImpl;

public class JvnObjectConcurrencyTest {
	
	static JvnObjectImpl JvnObjectTest1;
	static JvnObjectImpl JvnObjectTest2;
	static JvnObjectImpl JvnObjectTest3;
	static Lock lockAppLevel1 = new ReentrantLock();
	static Lock lockAppLevel2 = new ReentrantLock();
	static Lock lockAppLevel3 = new ReentrantLock();
	static final int NUM_THREAD = 100;
	static CyclicBarrier barrier = new CyclicBarrier(NUM_THREAD);
	
	
	@SuppressWarnings("static-method")
	@Before
	public void initialize() throws Exception {
		JvnObjectTest1 = new JvnObjectImpl(0, "JvnObjectTest1 - ");
		JvnObjectTest2 = new JvnObjectImpl(1, "JvnObjectTest2 - ");
		JvnObjectTest3 = new JvnObjectImpl(2, "JvnObjectTest3 - ");
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
					JvnObjectTest1.jvnLockRead();
					String o1Str = (String) JvnObjectTest1.jvnGetObjectState();
					o1Str = o1Str.concat("-"+this.idThread);
					JvnObjectTest1.jvnLockWrite();
					JvnObjectTest1.setSerializableObject(o1Str);
					JvnObjectTest1.jvnUnLock();
					lockAppLevel1.unlock();
					
					lockAppLevel2.lock();
					JvnObjectTest2.jvnLockRead();
					String o2Str = (String) JvnObjectTest2.jvnGetObjectState();
					o2Str = o2Str.concat("-"+this.idThread);
					JvnObjectTest2.jvnLockWrite();
					JvnObjectTest2.setSerializableObject(o2Str);
					JvnObjectTest2.jvnUnLock();
					lockAppLevel2.unlock();
					
					lockAppLevel3.lock();
					JvnObjectTest3.jvnLockRead();
					String o3Str = (String) JvnObjectTest3.jvnGetObjectState();
					o3Str = o3Str.concat("-"+this.idThread);
					JvnObjectTest3.jvnLockWrite();
					JvnObjectTest3.setSerializableObject(o3Str);
					JvnObjectTest3.jvnUnLock();
					lockAppLevel3.unlock();
				}
			} catch (JvnException | InterruptedException | BrokenBarrierException e) {
				e.printStackTrace();
			}
		}
		
	}

	@Test(timeout=10000)
	public void localConcurrencyTest() throws JvnException {
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
		
		assertEquals("Chaine de même longueur", JvnObjectTest1.jvnGetObjectState().toString().length(),JvnObjectTest2.jvnGetObjectState().toString().length());
		assertEquals("Chaine de même longueur", JvnObjectTest1.jvnGetObjectState().toString().length(),JvnObjectTest3.jvnGetObjectState().toString().length());

	}

}
