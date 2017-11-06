/***
 * Irc class : simple implementation of a chat using JAVANAISE
 * Contact: 
 *
 * Authors: 
 */

package tests.irc;

import java.awt.*;
import java.awt.event.*;
import jvn.jvnExceptions.JvnException;
import jvn.jvnExceptions.JvnObjectNotFoundException;
import jvn.jvnExceptions.JvnProxyException;
import jvn.proxy.JvnProxy;


/**
 * Exemple d'application de chat
 * @author Paul Carretero
 */
@SuppressWarnings("javadoc")
public class Irc {
	public final TextArea		text;
	public final TextField		data;
	public final Frame			frame;
	public final SentenceItf	sentence;


	/**
	 * main method
	 * create a JVN object nammed IRC for representing the Chat application
	 **/
	@SuppressWarnings("unused")
	public static void main(String argv[]) {
		try {
			new Irc((SentenceItf) JvnProxy.getRemoteInstance(Sentence.class, "IRC"));
		} catch (JvnObjectNotFoundException e) {
			try {
				new Irc((SentenceItf) JvnProxy.newInstance(new Sentence(), "IRC"));
			} catch (IllegalArgumentException | JvnProxyException e1) {
				e1.printStackTrace();
			}
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (JvnException e) {
			e.printStackTrace();
		}
	}

	/**
	 * IRC Constructor
   @param stce the sentence representing the Chat
	 **/
	public Irc(SentenceItf stce) {
		this.sentence = stce;
		this.frame=new Frame();
		this.frame.setLayout(new GridLayout(1,1));
		this.text=new TextArea(10,60);
		this.text.setEditable(false);
		this.text.setForeground(Color.red);
		this.frame.add(this.text);
		this.data=new TextField(40);
		this.frame.add(this.data);
		Button read_button = new Button("read");
		read_button.addActionListener(new readListener(this));
		this.frame.add(read_button);
		Button write_button = new Button("write");
		write_button.addActionListener(new writeListener(this));
		this.frame.add(write_button);
		//Button unlock_button = new Button("unlock");
		//unlock_button.addActionListener(new unlockListener(this));
		//this.frame.add(unlock_button);
		this.frame.setSize(500,200);
		this.text.setBackground(Color.black); 
		this.frame.setVisible(true);
		this.frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent we) {
				System.exit(42);
			}
		});
	}
}


/**
 * Internal class to manage user events (read) on the CHAT application
 **/
@SuppressWarnings("javadoc")
class readListener implements ActionListener {
	Irc irc;

	public readListener (Irc i) {
		this.irc = i;
	}

	/**
	 * Management of user events
	 **/
	@Override
	public void actionPerformed (ActionEvent e) {
		// invoke the method
		String s = this.irc.sentence.read();

		// display the read value
		this.irc.data.setText(s);
		this.irc.text.append(s+"\n");
	}
}

/*class unlockListener implements ActionListener {
	Irc irc;

	public unlockListener (Irc i) {
		this.irc = i;
	}

	/**
 * Management of user events
 **
	@Override
	public void actionPerformed (ActionEvent e) {
		try {
			// unlock the object
			this.irc.sentence.jvnUnLock();
		} catch (JvnException je) {
			System.out.println("IRC problem : " + je.getMessage());
		}
	}
}*/

/**
 * Internal class to manage user events (write) on the CHAT application
 **/
@SuppressWarnings("javadoc")
class writeListener implements ActionListener {
	Irc irc;

	public writeListener (Irc i) {
		this.irc = i;
	}

	/**
	 * Management of user events
	 **/
	@Override
	public void actionPerformed (ActionEvent e) {
		// get the value to be written from the buffer
		String s = this.irc.data.getText();

		// invoke the method
		this.irc.sentence.write(s);
	}
}



