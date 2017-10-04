/***
 * Sentence class : used for representing the text exchanged between users
 * during a chat application
 * Contact: 
 *
 * Authors: 
 */

package irc;

public class Sentence implements java.io.Serializable {

	private static final long serialVersionUID = 1L;
	String 		data;
  
	public Sentence() {
		this.data = new String("");
	}
	
	public void write(String text) {
		this.data = text;
	}
	public String read() {
		return this.data;	
	}
	
}