package jvn;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.server.RMISocketFactory;

/**
 * @author Paul Carretero
 * methodes static partagées entres les objets
 */
public class Shared {

	protected static final int timeoutMillis = 3000;

	/**
	 * permet de loger des messages en provenance d'objet.
	 * centralize les demande de log
	 * @param what quel objet ecrit le log
	 * @param msg un message de log
	 */
	public static void log(final String what, final String msg) {
		System.out.println("["+what+"] : "+msg);
	}

	public static void setRMITimeout() throws IOException {
		RMISocketFactory.setSocketFactory(new RMISocketFactory(){
			@Override
			public Socket createSocket(String host, int port) throws IOException {
				Socket socket = new Socket(host, port);
				socket.setSoTimeout(timeoutMillis);
				socket.setSoLinger(false, 0);
				return socket;
			}
			@Override
			public ServerSocket createServerSocket(int port) throws IOException {
				return new ServerSocket(port);
			}
		}); 
	}
}


/**
 * TODO
 * refactoring de jvn exception...
 * supprimé les synchros inutile...
 */