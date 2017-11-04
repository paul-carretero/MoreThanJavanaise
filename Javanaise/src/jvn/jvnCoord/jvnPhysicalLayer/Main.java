package jvn.jvnCoord.jvnPhysicalLayer;

import java.rmi.RemoteException;

/**
 * @author Paul Carretero
 * Classe Main "JvnCoordinateur" lance une jvm physique sui supprotera une ou plusieurs instance de jvnCoord slave ou master (auto-négocié)
 */
public class Main {

	/**
	 * @param args
	 * @throws RemoteException
	 * 
	 */
	public static void main(String[] args) throws RemoteException {
		JvnRemotePhysicalImpl.jvnGetLocalPhysical();
	}

}
