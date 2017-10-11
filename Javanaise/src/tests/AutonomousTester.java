package tests;

import jvn.JvnException;
import jvn.JvnLocalServer;
import jvn.JvnObject;
import jvn.JvnServerImpl;

public class AutonomousTester {
	
	private static final int ITERATION = 100;
	private static int id;
	private static int previous;

	public static void main(String[] args) throws JvnException, InterruptedException {
		id = Integer.parseInt(args[0]);	
		previous = id-1;
		if(previous == 0) {
			previous = Integer.parseInt(args[1]);
		}
		
		System.out.println("WORKER STARTED, ID = "+id + "( previous = " + previous + ")");
		
		JvnLocalServer js = JvnServerImpl.jvnGetServer();
				
		JvnObject startBarrier = js.jvnLookupObject("startBarrier");
		JvnObject endBarrier = js.jvnLookupObject("endBarrier");
						
		startBarrier.jvnLockWrite();
		((AutonomousTestBarrier) startBarrier.jvnGetObjectState()).imHere();
		startBarrier.jvnUnLock();
						
		boolean keepDreaming = true;
		while(keepDreaming) {
						
			startBarrier.jvnLockRead();
			if(((AutonomousTestBarrier) startBarrier.jvnGetObjectState()).isItOkToLeaveNow()) {
				keepDreaming = false;				
			}
			startBarrier.jvnUnLock();
		}
		int res = doBoringWork();	
		
		endBarrier.jvnLockWrite();
		((AutonomousTestBarrier) endBarrier.jvnGetObjectState()).imHere();
		endBarrier.jvnUnLock();
		
		System.out.println("WORKER ENDED, ID = "+id + " WITH " + res + " ADDITION TO THE LIST");
		
	}

	private static int doBoringWork() throws JvnException, InterruptedException {
		JvnLocalServer js = JvnServerImpl.jvnGetServer();
		JvnObject collaborativeObject = js.jvnLookupObject("collaborativeObject");
		int res = 0;
		for (int i = 0; i < ITERATION; i++) {
			collaborativeObject.jvnLockRead();
			int last = ((CollaborativeObject) collaborativeObject.jvnGetObjectState()).getLast();
			System.out.println("tid = "+id+" & last = "+ last + " & previous = " + previous);
			if(last == previous || (last == 0 && id == 1)) {
				collaborativeObject.jvnLockWrite();
				((CollaborativeObject) collaborativeObject.jvnGetObjectState()).addMe(id);
				res++;
			}
			collaborativeObject.jvnUnLock();
			Thread.sleep(50);
		}
		return res;
	}
}
