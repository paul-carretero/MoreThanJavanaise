package tests;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import jvn.jvnExceptions.JvnConcurrentLockUpgradeException;
import jvn.jvnExceptions.JvnException;
import jvn.jvnExceptions.JvnObjectNotFoundException;
import jvn.jvnServer.JvnServerImpl;
import jvn.proxy.JvnProxy;
import tests.testObjects.StringObjectItf;
import tests.testObjects.StringObject;
/**
 * @author Paul Carretero
 * Test permettant, si lancé 2 fois, de mettre en évidence un cas de deadlock
 * les 2 possède un verrou en lecture et tente de l'upgrader en ecriture
 * le second à faire la tentative aura une levé d'exception
 */
@SuppressWarnings("javadoc")
public class ExceptionalWorker {

	public ExceptionalWorker() throws IllegalArgumentException, JvnObjectNotFoundException, JvnException, InterruptedException {
		System.out.println("[ExceptionalWorker]: STARTED  @ "+ LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS")));
		StringObjectItf aString;
		try {
			aString = (StringObjectItf) JvnProxy.getRemoteInstance(StringObject.class, "stringObj");
		}
		catch (@SuppressWarnings("unused") JvnObjectNotFoundException e) {
			aString = (StringObjectItf) JvnProxy.newInstance(new StringObject(), "stringObj");
		}

		JvnServerImpl.jvnGetServer().beginTransaction();
		aString.getS();
		System.out.println("[ExceptionalWorker]: busy (" + 0 + "% completed)");
		for(int i = 1; i <= 10; i++) {
			Thread.sleep(500);
			System.out.println("[ExceptionalWorker]: busy (" + i*10 + "% completed)");
		}
		try {
			aString.setS("finished");
		} catch (JvnConcurrentLockUpgradeException e) {
			System.err.println(e.getMessage());
		}
		
		JvnServerImpl.jvnGetServer().commitTransaction();

		System.out.println("[ExceptionalWorker]: FINISHED @ "+ LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS")));
	}

	@SuppressWarnings("unused")
	public static void main(String[] args) throws IllegalArgumentException, JvnObjectNotFoundException, JvnException, InterruptedException {
		new ExceptionalWorker();
	}

}
