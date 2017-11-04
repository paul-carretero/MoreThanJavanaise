package tests.burnTest;

import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.Queue;
import jvn.jvnExceptions.JvnException;
import jvn.proxy.JvnProxy;
import tests.testObjects.CollaborativeBarrier;
import tests.testObjects.CollaborativeBarrierItf;
import tests.testObjects.CollaborativeObject;
import tests.testObjects.CollaborativeObjectItf;

public class AutonomousTesterManager {

	public AutonomousTesterManager(String[] args) throws NumberFormatException, JvnException, InterruptedException {
		int nproc = Integer.parseInt(args[0]);

		CollaborativeBarrierItf startBarrier  		= (CollaborativeBarrierItf) JvnProxy.newInstance(new CollaborativeBarrier(nproc), "startBarrier");
		CollaborativeBarrierItf endBarrier 			= (CollaborativeBarrierItf) JvnProxy.newInstance(new CollaborativeBarrier(nproc), "endBarrier");
		CollaborativeObjectItf collaborativeObject 	= (CollaborativeObjectItf) JvnProxy.newInstance(new CollaborativeObject(), "collaborativeObject");

		startBarrier.reset(nproc);
		startBarrier.reset(nproc);
		collaborativeObject.reset();
		
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
			if(endBarrier.go()) {
				keepDreaming = false;
			}
			k++;
		}

		System.out.println();
		
		boolean success = true;
		Queue<Integer> q = collaborativeObject.getResult();

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
				System.out.print(q.poll()+"-");
			}
		}
		else {
			System.out.println("fail...");
		}
	}

	public static void main(String[] args) throws NumberFormatException, JvnException, InterruptedException, RemoteException, MalformedURLException {
		if(args[0].equals("manager")) {
			new AutonomousTesterManager(Arrays.copyOfRange(args,1,args.length));
		}
		else if(args[0].equals("tester")) {
			new AutonomousTester(Arrays.copyOfRange(args,1,args.length));
		}
	}
}
