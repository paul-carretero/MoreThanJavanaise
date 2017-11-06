package jvn.proxy; 
import java.io.Serializable;
import java.lang.annotation.*;

/**
 * @author Paul Carretero
 * annotation permettant de spécifier un champs référencant un autre objet javanaise
 * contrainte : ce champs doit être transient et public
 */
@Retention(RetentionPolicy.RUNTIME)  
@Target(ElementType.FIELD)  
public @interface JvnReference {	
	
	/**
	 * @return le nom de l'objet applicatif référencé par ce field
	 */
	public String objectReference();
	
	/**
	 * @return la classe de l'objet applicatif référencé par ce field
	 */
	public Class<? extends Serializable> objectClass(); 
} 