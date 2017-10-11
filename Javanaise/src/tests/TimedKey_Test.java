package tests;

import static org.junit.Assert.*;
import org.junit.Test;

import jvn.TimedKey;

public class TimedKey_Test {

	@SuppressWarnings("static-method")
	@Test
	public void classTest() {
		TimedKey tk1 = new TimedKey("test1");
		assertNotNull(tk1);
		assertEquals("Vérification du nom", tk1.getKey(), "test1");
	}
	
	// dans un treeset le plus petit element est en premier donc oldTk doit être à la fin! (et newTk au début)
	@SuppressWarnings("static-method")
	@Test
	public void CompareTest() throws InterruptedException {
		TimedKey oldTk = new TimedKey("oldTk");
		Thread.sleep(100);
		TimedKey tk1 = new TimedKey("tk1");
		Thread.sleep(100);
		TimedKey tk2 = new TimedKey("tk2");
		Thread.sleep(100);
		TimedKey newTk = new TimedKey("newTk");
		assertTrue("newTk est plus récent que tk2", newTk.compareTo(tk2) < 0);
		assertTrue("newTk est plus vieux que tk1", newTk.compareTo(tk1) < 0);
		assertTrue("newTk est plus vieux que newTk", newTk.compareTo(oldTk) < 0);
		assertTrue("oldTk est plus vieux que newTk", oldTk.compareTo(newTk) > 0);
		assertTrue("oldTk est plus vieux que tk1", oldTk.compareTo(tk1) > 0);
		assertTrue("oldTk est plus vieux que tk2", oldTk.compareTo(tk2) > 0);
	}
	
	@SuppressWarnings({ "static-method" })
	@Test
	public void EqualTest() throws InterruptedException {
		TimedKey tk1 = new TimedKey("tk1");
		Thread.sleep(100);
		TimedKey tk1bis = new TimedKey("tk1");
		Thread.sleep(100);
		TimedKey tk2 = new TimedKey("tk2");
		assertTrue("tk1 == tk1bis", tk1.equals(tk1bis));
		assertFalse("tk1 != tk2", tk1.equals(tk2));
	}

}
