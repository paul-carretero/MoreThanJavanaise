package jvn.proxy; 
import java.io.Serializable;
import java.lang.annotation.*;

/**
 * @author Paul Carretero
 * Contrainte : le champs doit être public
 */
@Retention(RetentionPolicy.RUNTIME)  
@Target(ElementType.FIELD)  
public @interface JvnReference {	
	
	String objectReference();
	
	Class<? extends Serializable> objectClass(); 
} 