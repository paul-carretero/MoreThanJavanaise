package tests;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.BeforeClass;
import org.junit.Test;

import junit.framework.Assert;
import jvn.JvnException;
import jvn.JvnObject;
import jvn.JvnObjectImpl;

public class JvnObject_Test {
	
	static JvnObjectImpl JvnObjectTest1;
	static JvnObjectImpl JvnObjectTest2;
	static JvnObjectImpl JvnObjectTest3;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		JvnObjectTest1 = new JvnObjectImpl(0, "JvnObjectTest1 - ");
		JvnObjectTest2 = new JvnObjectImpl(1, "JvnObjectTest2 - ");
		JvnObjectTest3 = new JvnObjectImpl(2, "JvnObjectTest3 - ");
	}
	
	@Test(timeout=1000)
	public void localLockTest() throws InterruptedException, JvnException {
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
