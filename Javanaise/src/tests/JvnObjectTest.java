package tests;

import static org.junit.Assert.*;

import java.io.Serializable;

import org.junit.AfterClass;
import org.junit.Test;

import jvn.jvnExceptions.JvnException;
import jvn.jvnExceptions.JvnObjectNotFoundException;
import jvn.jvnExceptions.JvnProxyException;
import jvn.jvnObject.JvnObjectImpl;
import jvn.jvnObject.LockState;
import jvn.jvnServer.JvnServerImpl;
import jvn.proxy.JvnProxy;
import tests.testObjects.IntObject;
import tests.testObjects.IntObjectItf;

public class JvnObjectTest implements Serializable{

	private static final long serialVersionUID = -5016568459776507331L;

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		JvnServerImpl.jvnGetServer().jvnTerminate();
	}

	@Test(timeout=1000)
	public void localWriteReadAnnotedOKTest() throws IllegalArgumentException, JvnProxyException, JvnObjectNotFoundException, JvnException {
		IntObjectItf o = ((IntObjectItf) JvnProxy.newInstance(new IntObject(), "42_Obj"));
		o.set(42);
		assertEquals("verification de la bonne prise en compte de la modification", 42, o.get());
		
		o = ((IntObjectItf) JvnProxy.getRemoteInstance(IntObject.class, "42_Obj"));
		assertEquals("verification du bon rechargement serveur", 42, o.get());
		o.set(4242);
		assertEquals("verification du bon rechargement serveur", 4242, o.get());
	}
	
	@Test(timeout=1000, expected=JvnObjectNotFoundException.class)
	public void localWriteReadAnnotedFailTest() throws JvnObjectNotFoundException, JvnProxyException, IllegalArgumentException, JvnException {
		JvnProxy.getRemoteInstance(IntObject.class, "notFound");
	}

	/**
	 * @throws JvnException
	 */
	@SuppressWarnings("static-method")
	@Test(timeout=1000)
	public void localLockTest() throws JvnException {
		JvnObjectImpl JvnObjectTest1 = new JvnObjectImpl(1000, "JvnObjectTest1 - ");
		JvnObjectImpl JvnObjectTest2 = new JvnObjectImpl(1001, "JvnObjectTest2 - ");
		JvnObjectImpl JvnObjectTest3 = new JvnObjectImpl(1002, "JvnObjectTest3 - ");
		
		assertEquals("lock write sur jvnObject1", JvnObjectTest1.jvngetLock(), LockState.WRITE);
		assertEquals("lock write sur jvnObject2", JvnObjectTest2.jvngetLock(), LockState.WRITE);
		assertEquals("lock write sur jvnObject3", JvnObjectTest3.jvngetLock(), LockState.WRITE);

		JvnObjectTest1.jvnUnLock();
		JvnObjectTest2.jvnUnLock();
		JvnObjectTest3.jvnUnLock();

		assertEquals("lock write cached sur jvnObject1", JvnObjectTest1.jvngetLock(), LockState.WRITECACHED);
		assertEquals("lock write cached sur jvnObject2", JvnObjectTest2.jvngetLock(), LockState.WRITECACHED);
		assertEquals("lock write cached sur jvnObject3", JvnObjectTest3.jvngetLock(), LockState.WRITECACHED);

		JvnObjectTest1.jvnLockRead();
		assertEquals("lock write cached sur jvnObject1", JvnObjectTest1.jvngetLock(), LockState.WRITECACHEDREAD);

		JvnObjectTest1.jvnUnLock();
		assertEquals("lock write cached sur jvnObject1", JvnObjectTest1.jvngetLock(), LockState.WRITECACHED);
	}

}
