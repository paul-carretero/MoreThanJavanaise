package tests.burnTest;

import jvn.jvnExceptions.JvnException;
import jvn.proxy.JvnProxy;
import tests.testObjects.CollaborativeBarrier;
import tests.testObjects.CollaborativeBarrierItf;
import tests.testObjects.CollaborativeObject;
import tests.testObjects.CollaborativeObjectItf;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


/**
 * @author Paul Carretero
 * Programme de test visant à vérifier la robustesse des coordinateurs en charges
 * va effectuer un très grand nombre de requête en lecture/ecriture sur un objet
 */
@SuppressWarnings("javadoc")
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
		int n 			= Math.max(1, iteration/50);
		int toremove 	= 50;
		
		System.out.print("[WORKER "+ id + "]: ░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░");
		
		for (int i = 0; i < iteration; i++) {
			
			if(i % n == 0) {
				for(int j = toremove; j>0; j--) {
					System.out.print("\b");
				}
				toremove--;
				System.out.print("█");
				for(int j = toremove; j>0; j--) {
					System.out.print("░");
				}
			}
						
			int last = collaborativeObject.getLast();
			if(last == previous || (last == 0 && id == 1)) {
				collaborativeObject.addMe(id);
				res++;
			}
			Thread.sleep(1);
		}
		
		System.out.println();
		return res;
	}
}
