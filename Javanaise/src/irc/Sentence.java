/***
 * Sentence class : used for representing the text exchanged between users
 * during a chat application
 * Contact: 
 *
 * Authors: 
 */

package irc;

public class Sentence implements SentenceItf {

	private static final long serialVersionUID = -5744335023076980519L;
	String 		data;
  
	public Sentence() {
		this.data = new String("");
	}
	
	@Override
	public void write(String text) {
		this.data = text;
	}
	
	@Override
	public String read() {
		return this.data;	
	}
	
}