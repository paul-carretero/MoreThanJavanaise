package tests.RedundancyTest;

import jvn.Shared;
import jvn.jvnExceptions.JvnException;
import jvn.proxy.JvnProxy;
import tests.testObjects.CollaborativeBarrier;
import tests.testObjects.CollaborativeBarrierItf;
import tests.testObjects.CollaborativeObject;
import tests.testObjects.CollaborativeObjectItf;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class RedundancyTester {
	
	private static int iteration = 100;
	private static int id;
	private static int previous;

	public RedundancyTester(String[] args) throws JvnException, InterruptedException, IOException {
		Shared.setRMITimeout();
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
		
		System.out.println("[WORKER "+ id + "]: STARTED @ "+ LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")));
						
		CollaborativeBarrierItf startBarrier = (CollaborativeBarrierItf) JvnProxy.getRemoteInstance(CollaborativeBarrier.class, "startBarrier");
		CollaborativeBarrierItf endBarrier = (CollaborativeBarrierItf) JvnProxy.getRemoteInstance(CollaborativeBarrier.class, "endBarrier");
		
		startBarrier.addMe();
						
		boolean keepDreaming = true;
		int i = 0;
		while(keepDreaming && i < Integer.MAX_VALUE) {
			i++;
			if(startBarrier.go()) {
				keepDreaming = false;				
			}
		}
		
		if(i == Integer.MAX_VALUE ) {
			System.out.println(42/0); // purest mathematical crash
		}
		
		System.out.println("[WORKER "+ id + "]: SYNCHRONIZED @ " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")));
		
		
		int res = doBoringWork();	
		
		endBarrier.addMe();
		System.out.println("[WORKER "+ id + "]: ENDED WITH " + res + " ADDITIONS TO THE LIST");
		
	}

	private static int doBoringWork() throws JvnException, InterruptedException {
		CollaborativeObjectItf collaborativeObject = (CollaborativeObjectItf) JvnProxy.getRemoteInstance(CollaborativeObject.class, "collaborativeObject");
		int res 		= 0;
		
		for (int i = 0; i < iteration; i++) {
			int last = collaborativeObject.getLast();
			if(last == previous || (last == 0 && id == 1)) {
				collaborativeObject.addMe(id);
				res++;
				System.out.println("[WORKER "+ id + "]: READ+WRITE @ " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")));
			}
			else {
				System.out.println("[WORKER "+ id + "]: READ ["+last+"]   @ " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")));
			}
			Thread.sleep(20);
		}
		
		System.out.println();
		return res;
	}
}
