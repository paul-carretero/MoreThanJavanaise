package jvn.proxy;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import jvn.jvnExceptions.JvnException;
import jvn.jvnExceptions.JvnObjectNotFoundException;
import jvn.jvnExceptions.JvnProxyException;
import jvn.jvnObject.JvnObject;
import jvn.jvnServer.JvnLocalServer;
import jvn.jvnServer.JvnServerImpl;

/**
 * @author Paul Carretero
 * proxy d'interception des appel de méthode sur un objet javanaise
 */
public class JvnProxy implements InvocationHandler {

	/**
	 * objet jvn encapsulant un objet applicatif
	 */
	private final JvnObject jvnObject;

	/**
	 * serveur de cache local
	 */
	private static final JvnLocalServer jvnLocalServer = JvnServerImpl.jvnGetServer();

	/**
	 * @param obj un objet serializable a encapsuler dans un objet Javanaise
	 * @param jon le nom de cet objet serializable
	 * @throws JvnProxyException
	 */
	private JvnProxy(final Serializable obj, final String jon) throws JvnProxyException{
		try {
			JvnObject tempJvnObj = jvnLocalServer.jvnLookupObject(jon);
			if(tempJvnObj == null){
				tempJvnObj = jvnLocalServer.jvnCreateObject(obj);
				jvnLocalServer.jvnRegisterObject(jon, tempJvnObj);
			}
			this.jvnObject = tempJvnObj;
		}catch (Exception e) {
			e.printStackTrace();
			throw new JvnProxyException("Erreur durant le l'initialisation du proxy");
		}
	} 
	
	/**
	 * recherche un objet applicatif existant
	 * @param jon le nom de l'objet serializable
	 * @throws JvnObjectNotFoundException si l'objet applicatif n'existe pas sur le réseau
	 * @throws JvnException 
	 */
	private JvnProxy(final String jon) throws JvnObjectNotFoundException, JvnException{
		this.jvnObject = jvnLocalServer.jvnLookupObject(jon); // celui de classe est "final" car c'est beau
		if(this.jvnObject == null){
			throw new JvnObjectNotFoundException(jon);
		}
	}

	@Override
	public Object invoke(final Object unused, final Method invokedMethod, final Object[] args) throws Throwable {
		if(invokedMethod.isAnnotationPresent(LockAsked.class)){ 
			LockAsked l = invokedMethod.getAnnotation(LockAsked.class); 
			switch (l.lock()) {
			case READ:
				this.jvnObject.jvnLockRead();
				if(jvnLocalServer.isInTransaction()) {
					jvnLocalServer.readRegisterInTransaction(this.jvnObject);
				}
				break;
			case WRITE:
				this.jvnObject.jvnLockWrite();
				if(jvnLocalServer.isInTransaction()) {
					jvnLocalServer.writeRegisterInTransaction(this.jvnObject);
				}
				break;
			default:
				throw new JvnProxyException("Erreur d'annotation du type de verrou utilisé dans la methode");
			} 
		}
		try {
			return invokedMethod.invoke(this.jvnObject.jvnGetObjectState(), args);
		} finally {
			if(invokedMethod.isAnnotationPresent(LockAsked.class)){ 
				if(!jvnLocalServer.isInTransaction()) {
					this.jvnObject.jvnUnLock();
				}
			}
		}
	}


	/**
	 * @param obj la classe de l'objet à créer si non existant
	 * @param jon le nom de l'objet applicatif à rechercher
	 * @return un objet d'interception pour l'objet applicatif
	 * @throws IllegalArgumentException
	 * @throws JvnProxyException
	 */
	public static Object newInstance(final Serializable obj, final String jon) throws IllegalArgumentException, JvnProxyException{ 
		return Proxy.newProxyInstance(obj.getClass().getClassLoader(), obj.getClass().getInterfaces(), new JvnProxy(obj,jon)); 
	}
	
	/**
	 * @param objectClass la classe de l'objet à rechercher
	 * @param jon le nom de l'objet applicatif à rechercher
	 * @return un objet d'interception pour l'objet applicatif
	 * @throws IllegalArgumentException 
	 * @throws JvnObjectNotFoundException 
	 * @throws JvnException 
	 */
	public static Object getRemoteInstance(final Class<? extends Serializable> objectClass, final String jon) throws IllegalArgumentException, JvnObjectNotFoundException, JvnException{ 
		return Proxy.newProxyInstance(objectClass.getClassLoader(), objectClass.getInterfaces(), new JvnProxy(jon)); 
	}

}
