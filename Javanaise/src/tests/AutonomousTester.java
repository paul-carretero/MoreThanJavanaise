package tests;

import jvn.JvnException;
import jvn.JvnLocalServer;
import jvn.JvnObject;
import jvn.JvnServerImpl;
import java.util.concurrent.atomic.AtomicInteger;


public class AutonomousTester {
	
	private static int iteration = 100;
	private static int id;
	private static int previous;

	public AutonomousTester(String[] args) throws JvnException, InterruptedException {
		if(args.length != 3) {
			System.out.println("Usage:");
			System.out.println("exec <PID> <MAX_PID> <NB ITERATION>");
			System.exit(42);
		}
		
		
		id = Integer.parseInt(args[0]);	
		previous = id-1;
		if(previous == 0) {
			previous = Integer.parseInt(args[1]);
		}
		iteration = Integer.parseInt(args[2]);	
		
		System.out.println("[WORKER "+ id + "]: STARTED");
		
		JvnLocalServer js = JvnServerImpl.jvnGetServer();
				
		JvnObject startBarrier = js.jvnLookupObject("startBarrier");
		JvnObject endBarrier = js.jvnLookupObject("endBarrier");
						
		startBarrier.jvnLockWrite();
		((AtomicInteger) startBarrier.jvnGetObjectState()).decrementAndGet();
		startBarrier.jvnUnLock();
						
		boolean keepDreaming = true;
		while(keepDreaming) {
			startBarrier.jvnLockRead();
			System.out.println("[WORKER "+ id + "]: startBarrier : " + startBarrier);
			if(((AtomicInteger) startBarrier.jvnGetObjectState()).get() <= 0) {
				keepDreaming = false;				
			}
			startBarrier.jvnUnLock();
			//Thread.sleep(50);
		}
		
		System.out.println("[WORKER "+ id + "]: SYNCHRONIZED");
		
		int res = doBoringWork();	
		
		endBarrier.jvnLockWrite();
		((AtomicInteger) endBarrier.jvnGetObjectState()).decrementAndGet();
		endBarrier.jvnUnLock();
		System.out.println("[WORKER "+ id + "]: ENDED WITH " + res + " ADDITIONS TO THE LIST");
		
	}

	private static int doBoringWork() throws JvnException, InterruptedException {
		JvnLocalServer js = JvnServerImpl.jvnGetServer();
		JvnObject collaborativeObject = js.jvnLookupObject("collaborativeObject");
		int res = 0;
		for (int i = 0; i < iteration; i++) {
			collaborativeObject.jvnLockRead();
			int last = ((CollaborativeObject) collaborativeObject.jvnGetObjectState()).getLast();
			System.out.println("tid = "+id+" & last = "+ last + " & previous = " + previous);
			if(last == previous || (last == 0 && id == 1)) {
				collaborativeObject.jvnLockWrite();
				((CollaborativeObject) collaborativeObject.jvnGetObjectState()).addMe(id);
				res++;
			}
			collaborativeObject.jvnUnLock();
			//Thread.sleep(50);
		}
		return res;
	}
}
