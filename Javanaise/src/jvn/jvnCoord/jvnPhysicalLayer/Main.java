package jvn.jvnCoord.jvnPhysicalLayer;

import java.rmi.RemoteException;

public class Main {

	public static void main(String[] args) throws RemoteException {
		JvnRemotePhysicalImpl.jvnGetLocalPhysical();
	}

}
