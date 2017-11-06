package tests.Junits;

import static org.junit.Assert.*;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import jvn.jvnExceptions.JvnConcurrentLockUpgradeException;
import jvn.jvnServer.JvnServerImpl;
import jvn.proxy.JvnProxy;
import tests.testObjects.IntObject;
import tests.testObjects.IntObjectItf;
import tests.testObjects.ReferenceObject;
import tests.testObjects.ReferenceObjectItf;
import tests.testObjects.StringObject;
import tests.testObjects.StringObjectItf;

/**
 * @author Paul Carretero
 * Suite de test permettant de vérifier le bon chargement des références 
 * vers des objets partagé présent dans d'autre objets partagés
 */
@SuppressWarnings("javadoc")
public class JvnObjReferenceTest {
	
	static StringObjectItf str;
	static IntObjectItf n;
	static ReferenceObjectItf referenceObject;
	
	public static void testDefaultValues() throws IllegalArgumentException {
		assertEquals("verification de l'id en reference", 0, referenceObject.getIntRef().get());
		assertEquals("verification de la string en reference", "StringRef", referenceObject.getStringRef().getS());
	}

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		str	= (StringObjectItf) JvnProxy.newInstance(new StringObject("StringRef"), "stringRef");
		n	= (IntObjectItf) JvnProxy.newInstance(new IntObject(0), "intRef");
		referenceObject = (ReferenceObjectItf) JvnProxy.newInstance(new ReferenceObject(42), "referenceObject");
		testDefaultValues();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		JvnServerImpl.jvnGetServer().jvnTerminate();
	}

	@SuppressWarnings("static-method")
	@Test
	public void testCreation() throws IllegalArgumentException {
		assertEquals("verification de l'id de la creation", 42, referenceObject.getId());
	}
	
	@SuppressWarnings("static-method")
	@Test
	public void testUpdatedValues() throws IllegalArgumentException, JvnConcurrentLockUpgradeException {
		str.setS("updated String");
		n.set(42);
		assertEquals("verification de l'id en reference", 42, referenceObject.getIntRef().get());
		assertEquals("verification de la string en reference", "updated String", referenceObject.getStringRef().getS());
	}
	
	@SuppressWarnings("static-method")
	@Test
	public void testUpdateValues() throws IllegalArgumentException, JvnConcurrentLockUpgradeException {
		referenceObject.getIntRef().set(4242);
		referenceObject.getStringRef().setS("JCVD");
		assertEquals("verification de l'id en reference", 4242, referenceObject.getIntRef().get());
		assertEquals("verification de la string en reference", "JCVD", referenceObject.getStringRef().getS());
	}

}
