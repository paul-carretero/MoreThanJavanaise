package deprecatedTests;

import static org.junit.Assert.*;

import java.io.Serializable;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import jvn.jvnExceptions.JvnException;
import jvn.jvnExceptions.JvnObjectNotFoundException;
import jvn.jvnExceptions.JvnProxyException;
import jvn.jvnObject.JvnObjectImpl;
import jvn.jvnServer.JvnServerImpl;
import jvn.proxy.JvnProxy;
import jvn.proxy.LockAsked;
import jvn.proxy.LockAsked.Lock;

public class JvnObjectTest implements Serializable{

	private static final long serialVersionUID = -5016568459776507331L;

	public interface testItf extends Serializable {
		@LockAsked(lock = Lock.WRITE)
		void set(int n);
		@LockAsked(lock = Lock.READ)
		int get();
	}
	
	public class testImpl implements testItf{
		private static final long serialVersionUID = 5658211103101652356L;
		public Integer i;
		@Override
		public void set(int n) {this.i = n;}
		@Override
		public int get() {return this.i;}
	}

	static JvnObjectImpl JvnObjectTest1;
	static JvnObjectImpl JvnObjectTest2;
	static JvnObjectImpl JvnObjectTest3;

	/**
	 * @throws Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		JvnObjectTest1 = new JvnObjectImpl(0, "JvnObjectTest1 - ");
		JvnObjectTest2 = new JvnObjectImpl(1, "JvnObjectTest2 - ");
		JvnObjectTest3 = new JvnObjectImpl(2, "JvnObjectTest3 - ");
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		JvnServerImpl.jvnGetServer().jvnTerminate();
	}

	@Test(timeout=1000)
	public void localWriteReadAnnotedOKTest() throws IllegalArgumentException, JvnProxyException, JvnObjectNotFoundException, JvnException {
		testItf o = ((testItf) JvnProxy.newInstance(new testImpl(), "42_Obj"));
		o.set(42);
		assertEquals("verification de la bonne prise en compte de la modification", 42, o.get());
		
		o = ((testItf) JvnProxy.getRemoteInstance(testImpl.class, "42_Obj"));
		assertEquals("verification du bon rechargement serveur", 42, o.get());
		o.set(4242);
		assertEquals("verification du bon rechargement serveur", 4242, o.get());
	}
	
	@Test(timeout=1000, expected=JvnObjectNotFoundException.class)
	public void localWriteReadAnnotedFailTest() throws JvnObjectNotFoundException, JvnProxyException, IllegalArgumentException, JvnException {
		JvnProxy.getRemoteInstance(testImpl.class, "IRC");
	}

	/**
	 * @throws JvnException
	 */
	@SuppressWarnings("static-method")
	@Test(timeout=1000)
	public void localLockTest() throws JvnException {
		assertEquals("lock write sur jvnObject1", JvnObjectTest1.jvngetLock(), JvnObjectImpl.LockState.WRITE);
		assertEquals("lock write sur jvnObject2", JvnObjectTest2.jvngetLock(), JvnObjectImpl.LockState.WRITE);
		assertEquals("lock write sur jvnObject3", JvnObjectTest3.jvngetLock(), JvnObjectImpl.LockState.WRITE);

		JvnObjectTest1.jvnUnLock();
		JvnObjectTest2.jvnUnLock();
		JvnObjectTest3.jvnUnLock();

		assertEquals("lock write cached sur jvnObject1", JvnObjectTest1.jvngetLock(), JvnObjectImpl.LockState.WRITECACHED);
		assertEquals("lock write cached sur jvnObject2", JvnObjectTest2.jvngetLock(), JvnObjectImpl.LockState.WRITECACHED);
		assertEquals("lock write cached sur jvnObject3", JvnObjectTest3.jvngetLock(), JvnObjectImpl.LockState.WRITECACHED);

		JvnObjectTest1.jvnLockRead();
		assertEquals("lock write cached sur jvnObject1", JvnObjectTest1.jvngetLock(), JvnObjectImpl.LockState.WRITECACHEDREAD);

		JvnObjectTest1.jvnUnLock();
		assertEquals("lock write cached sur jvnObject1", JvnObjectTest1.jvngetLock(), JvnObjectImpl.LockState.WRITECACHED);
	}

}
