package jvn.jvnCoord.jvnLoadBalancer;

import java.rmi.RemoteException;
import java.util.LinkedList;
import java.util.List;

import jvn.jvnCoord.JvnRemoteCoord;
import jvn.jvnExceptions.JvnException;

public class JvnLoadBalancerImpl implements JvnLoadBalancer {
	
	/**
	 * Nombre de Coordinateur souhaité
	 * Si le nombre de coordinateur disponible est inférieur alors certain coordinateur
	 * devront lancer d'autre instance de coordinateur
	 * Si le nombre de coordinateur disponible est supèrieur alors ils seront en attente
	 */
	private final int numberOfCoord;
	
	/**
	 * Coordinateur en attente, ils seront initié si l'un des coordinateur utilisé est inaccéssible
	 */
	private final List<JvnRemoteCoord> waitingCoord;

	public JvnLoadBalancerImpl(int numberOfCoord) {
		this.waitingCoord	= new LinkedList<JvnRemoteCoord>();
		this.numberOfCoord	= numberOfCoord;
	}

	@Override
	public int jvnGetObjectId() throws RemoteException, JvnException {
		return 0;
	}

	@Override
	public void jvnIncrementCounter() {
		
	}

	@Override
	public void jvnReSync(String jvnCoord) throws RemoteException, JvnException {
		
	}

	@Override
	public String[] jvnGetCoordName() throws RemoteException, JvnException {
		return null;
	}

	@Override
	public String jvnCoordRegister(JvnRemoteCoord coord) throws RemoteException, JvnException {
		return null;
	}

}
