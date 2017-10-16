package jvn.proxy; 
import java.lang.annotation.*; 
/**
 * @author Paul Carretero
 * specify
 */
@Retention(RetentionPolicy.RUNTIME)  
@Target(ElementType.METHOD)  
public @interface LockAsked {
	
	/**
	 * type de verrou pouvant être demandé par une méthode sur l'objet
	 */
	public enum Lock{
		/**
		 * verrou en lecture
		 */
		READ,
		/**
		 * verrou en écriture
		 */
		WRITE
	}
	
	/**
	 * @return le type de verrou souhaité par la méthode
	 */
	Lock lock();  
} 