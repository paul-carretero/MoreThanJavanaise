/***
 * Irc class : simple implementation of a chat using JAVANAISE
 * Contact: 
 *
 * Authors: 
 */

package irc;

import java.awt.*;
import java.awt.event.*;
import jvn.*;


public class Irc {
	public TextArea		text;
	public TextField	data;
	Frame 			frame;
	JvnObject       sentence;


	/**
	 * main method
	 * create a JVN object nammed IRC for representing the Chat application
	 **/
	public static void main(String argv[]) {
		try {
			// initialize JVN
			JvnServerImpl js = JvnServerImpl.jvnGetServer();

			// look up the IRC object in the JVN server
			// if not found, create it, and register it in the JVN server
			JvnObject jo = js.jvnLookupObject("IRC");
			if (jo == null) {
				jo = js.jvnCreateObject(new Sentence());
				// after creation, I have a write lock on the object
				jo.jvnUnLock();
				js.jvnRegisterObject("IRC", jo);
			}
			// create the graphical part of the Chat application
			new Irc(jo);

		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("IRC problem : " + e.getMessage());
		}
	}

	/**
	 * IRC Constructor
   @param jo the JVN object representing the Chat
	 **/
	public Irc(JvnObject jo) {
		this.sentence = jo;
		this.frame=new Frame();
		this.frame.setLayout(new GridLayout(1,1));
		this.text=new TextArea(10,60);
		this.text.setEditable(false);
		this.text.setForeground(Color.red);
		this.frame.add(this.text);
		this.data=new TextField(40);
		this.frame.add(this.data);
		Button read_button = new Button("read and readlock");
		read_button.addActionListener(new readListener(this));
		this.frame.add(read_button);
		Button write_button = new Button("write and writelock");
		write_button.addActionListener(new writeListener(this));
		this.frame.add(write_button);
		Button unlock_button = new Button("unlock");
		unlock_button.addActionListener(new unlockListener(this));
		this.frame.add(unlock_button);
		this.frame.setSize(700,200);
		this.text.setBackground(Color.black); 
		this.frame.setVisible(true);
		this.frame.addWindowListener(new WindowAdapter() {
	        @Override
			public void windowClosing(WindowEvent we) {
	            System.exit(42);
	         }
	     }
	);
	}
}


/**
 * Internal class to manage user events (read) on the CHAT application
 **/
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
		try {
			// lock the object in read mode
			this.irc.sentence.jvnLockRead();

			// invoke the method
			String s = ((Sentence)(this.irc.sentence.jvnGetObjectState())).read();

			// unlock the object
			//this.irc.sentence.jvnUnLock();

			// display the read value
			this.irc.data.setText(s);
			this.irc.text.append(s+"\n");
		} catch (JvnException je) {
			System.out.println("IRC problem : " + je.getMessage());
		}
	}
}

class unlockListener implements ActionListener {
	Irc irc;

	public unlockListener (Irc i) {
		this.irc = i;
	}

	/**
	 * Management of user events
	 **/
	@Override
	public void actionPerformed (ActionEvent e) {
		try {
			// unlock the object
			this.irc.sentence.jvnUnLock();
		} catch (JvnException je) {
			System.out.println("IRC problem : " + je.getMessage());
		}
	}
}

/**
 * Internal class to manage user events (write) on the CHAT application
 **/
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
		try {	
			// get the value to be written from the buffer
			String s = this.irc.data.getText();

			// lock the object in write mode
			this.irc.sentence.jvnLockWrite();

			// invoke the method
			((Sentence)(this.irc.sentence.jvnGetObjectState())).write(s);

			// unlock the object
			//this.irc.sentence.jvnUnLock();
		} catch (JvnException je) {
			System.out.println("IRC problem  : " + je.getMessage());
		}
	}
}



