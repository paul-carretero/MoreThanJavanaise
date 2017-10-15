package tests;

import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

import jvn.JvnException;
import jvn.jvnCoord.JvnCoordImpl;
import jvn.jvnObject.JvnObject;
import jvn.jvnServer.JvnServerImpl;

public class AutonomousTesterManager {

	public AutonomousTesterManager(String[] args) throws NumberFormatException, JvnException, InterruptedException {
		JvnServerImpl js = JvnServerImpl.jvnGetServer();
		int nproc = Integer.parseInt(args[0]);

		JvnObject startBarrier = js.jvnCreateObject(new AtomicInteger(nproc));
		JvnObject endBarrier = js.jvnCreateObject(new AtomicInteger(nproc));
		JvnObject collaborativeObject = js.jvnCreateObject(new CollaborativeObject());

		js.jvnRegisterObject("startBarrier", startBarrier);
		js.jvnRegisterObject("endBarrier", endBarrier);
		js.jvnRegisterObject("collaborativeObject", collaborativeObject);

		startBarrier.jvnUnLock();
		endBarrier.jvnUnLock();
		collaborativeObject.jvnUnLock();

		boolean keepDreaming = true;

		System.out.println("[MANAGER]: INITIALIZED");
		System.out.print("[MANAGER]: DREAMING :   ");
		int k = 0;

		while(keepDreaming) {
			Thread.sleep(300);
			System.out.print("\b\b\b\b");
			if(k % 4 == 0) {
				System.out.print("▔   ");
			}
			else if(k % 4 == 1) {
				System.out.print("▕   ");
			}
			else if(k % 4 == 2) {
				System.out.print("▁   ");
			}
			else if(k % 4 == 3) {
				System.out.print("▏   ");
			}
			endBarrier.jvnLockRead();
			if(( (AtomicInteger) endBarrier.jvnGetObjectState()).get() <= 0 ) {
				keepDreaming = false;
			}
			endBarrier.jvnUnLock();
			k++;
		}

		System.out.println();
		
		collaborativeObject.jvnLockRead();
		boolean success = true;
		Queue<Integer> q = ((CollaborativeObject) collaborativeObject.jvnGetObjectState()).getResult();

		int previous = 0;
		for(Integer i : q) {
			success = success && (previous == 0 || (i == previous-1 || (previous == nproc && i == 1)));
		}

		if(success) {
			System.out.println("(•_•)");
			Thread.sleep(500);
			System.out.println("( •_•)>⌐■-■");
			Thread.sleep(500);
			System.out.println("(⌐■_■)");
			System.out.println("Successfully created a list of " + q.size() + " Integer cyclique : ");
			for(int i = 0; i < q.size(); i++) {
				System.out.print(q.poll());
			}
		}
		else {
			System.out.println("fail...");
		}
		collaborativeObject.jvnUnLock();
	}

	public static void main(String[] args) throws NumberFormatException, JvnException, InterruptedException, RemoteException, MalformedURLException {
		if(args[0].equals("manager")) {
			new AutonomousTesterManager(Arrays.copyOfRange(args,1,args.length));
		}
		else if(args[0].equals("tester")) {
			new AutonomousTester(Arrays.copyOfRange(args,1,args.length));
		}
		else {
			new JvnCoordImpl();
		}
	}
}
