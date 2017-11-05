/***
 * Sentence class : used for representing the text exchanged between users
 * during a chat application
 * Contact: 
 *
 * Authors: 
 */

package tests.irc;

public class Sentence implements SentenceItf {

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = -5744335023076980519L;
	String 		data;
  
	/**
	 * Constructeur par défault d'une Sentence
	 */
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
	
	@Override
	public String toString() {
		return this.data;
	}
	
}