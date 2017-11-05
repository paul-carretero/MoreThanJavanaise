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
			Thread.sleep(1000);
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

	public static void main(String[] args) throws IllegalArgumentException, JvnObjectNotFoundException, JvnException, InterruptedException {
		new ExceptionalWorker();
	}

}
