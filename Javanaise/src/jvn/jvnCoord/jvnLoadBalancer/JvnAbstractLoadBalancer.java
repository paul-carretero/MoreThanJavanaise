package jvn.jvnCoord.jvnLoadBalancer;

import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import jvn.jvnCoord.jvnPhysicalLayer.JvnRemotePhysical;
import jvn.jvnExceptions.JvnException;

public abstract class JvnAbstractLoadBalancer extends UnicastRemoteObject implements JvnLoadBalancer {

	/**
	 * host RMI lookup
	 */
	protected static final String HOST = "localhost";
	
	/**
	 * host rmi bind
	 */
	protected static final String HOST_URL = "//"+HOST+"/";
	
	/**
	 * Registre RMI
	 */
	protected final Registry rmiRegistry;
	
	/**
	 * serialVersionUID
	 */
	private static final long 	serialVersionUID 	= 5483513486680036569L;

	/**
	 * Nombre de Coordinateur souhaité
	 * Si le nombre de coordinateur disponible est inférieur alors certain coordinateur
	 * devront lancer d'autre instance de coordinateur
	 * Si le nombre de coordinateur disponible est supèrieur alors ils seront en attente
	 */
	protected static final int NUMBER_OF_COORDS_INSTANCE = 3;

	/**
	 * Coordinateur en attente, ils seront initié si l'un des coordinateur utilisé est inaccéssible
	 */
	protected final JvnCoordMap CoordMap;

	/**
	 * compteur d'id des objet. Le compteur est synchronizé avec le slave 
	 * (donc pas possible d'utiliser un atomicInteger à cause de l'enregistrement du slave)
	 */
	protected int currentOjectId;

	protected final JvnRemotePhysical physicalLayer;

	public JvnAbstractLoadBalancer(JvnRemotePhysical physicalLayer) throws RemoteException, MalformedURLException, JvnException {
		super();
		this.rmiRegistry 	= LocateRegistry.getRegistry();
		this.CoordMap		= new JvnCoordMap(NUMBER_OF_COORDS_INSTANCE);
		this.currentOjectId = 0;
		this.physicalLayer	= physicalLayer;
	}
	
	public JvnAbstractLoadBalancer(JvnRemotePhysical physicalLayer, JvnCoordMap CoordMap, int currentOjectId) throws RemoteException, MalformedURLException, JvnException {
		super();
		this.rmiRegistry 	= LocateRegistry.getRegistry();
		this.CoordMap		= CoordMap;
		this.currentOjectId = currentOjectId;
		this.physicalLayer	= physicalLayer;
	}

	@Override
	public int jvnGetNumberCoord() throws RemoteException, JvnException {
		return JvnAbstractLoadBalancer.NUMBER_OF_COORDS_INSTANCE;
	}

	@Override
	public void ping() throws RemoteException {}

	/**
	 * catch toutes les exception, peut être utilisé plusieurs fois dans les tests junit
	 */
	public void destroy() {
		try {
			UnicastRemoteObject.unexportObject(this,true);
		} catch (Exception e) {}
	}
}
