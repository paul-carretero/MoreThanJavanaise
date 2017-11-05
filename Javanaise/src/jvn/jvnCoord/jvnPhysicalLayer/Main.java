package jvn.jvnCoord.jvnPhysicalLayer;

import java.io.IOException;

import jvn.Shared;

/**
 * @author Paul Carretero
 * Classe Main "JvnCoordinateur" lance une jvm physique sui supprotera une ou plusieurs instance de jvnCoord slave ou master (auto-négocié)
 */
public class Main {

	/**
	 * @param args
	 * @throws IOException 
	 * 
	 */
	public static void main(String[] args) throws IOException {
		Shared.setRMITimeout();
		JvnRemotePhysicalImpl.jvnGetLocalPhysical();
	}

}
