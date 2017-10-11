package tests;

import java.util.Queue;

import jvn.JvnException;
import jvn.JvnObject;
import jvn.JvnServerImpl;

public class AutonomousTesterManager {

	public static void main(String[] args) throws NumberFormatException, JvnException, InterruptedException {
		JvnServerImpl js = JvnServerImpl.jvnGetServer();
		int nproc = Integer.parseInt(args[0]);
				
		JvnObject startBarrier = js.jvnCreateObject(new AutonomousTestBarrier(nproc));
		JvnObject endBarrier = js.jvnCreateObject(new AutonomousTestBarrier(nproc));
		JvnObject collaborativeObject = js.jvnCreateObject(new CollaborativeObject());
				
		js.jvnRegisterObject("startBarrier", startBarrier);
		js.jvnRegisterObject("endBarrier", endBarrier);
		js.jvnRegisterObject("collaborativeObject", collaborativeObject);
		
		startBarrier.jvnUnLock();
		endBarrier.jvnUnLock();
		collaborativeObject.jvnUnLock();
				
		boolean keepDreaming = true;
		
		while(keepDreaming) {
			Thread.sleep(500);
			System.out.println("I'm dreaming...");
			endBarrier.jvnLockRead();
			if(( (AutonomousTestBarrier) endBarrier.jvnGetObjectState()).isItOkToLeaveNow() ) {
				keepDreaming = false;
			}
			endBarrier.jvnUnLock();
		}
		
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
}
