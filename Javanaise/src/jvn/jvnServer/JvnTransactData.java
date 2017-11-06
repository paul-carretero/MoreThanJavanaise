package jvn.jvnServer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * @author Paul Carretero
 * représente les données d'un objet d'une transaction
 * comprend notament une copie de celui ci (serializée) en cas de rollback
 */
public class JvnTransactData {

	/**
	 * copie d'un objet applicatif
	 */
	private byte[] backup;

	/**
	 * nombre de fois ou cet objet à été demandé en lecture
	 */
	private int readCount = 0;

	/**
	 * nombre de fois ou cet objet à été demandé en écriture
	 */
	private int writeCount = 0;

	/**
	 * Constructeur par défault
	 */
	public JvnTransactData() {
		this.writeCount	= 0;
		this.readCount	= 0;
	}

	/**
	 * @return le nombre de fois où cet objet à été appelé avec un verrou en lecture ou en écriture
	 */
	public int getTotalLockCount() {
		return this.readCount + this.writeCount;
	}

	/**
	 * @return la sauvegarde de cet objet réalisé avant la première aquisition de verrou en écritures
	 */
	public Serializable getSerializableObject() {
		try {
			return getBackup();
		} catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * incrémente le compteur de lecture
	 */
	public void read() {
		this.readCount++;
	}

	/**
	 * incrémente le compteur d'écriture et sauvegarde l'objet applicatif si il s'agit de la première fois
	 * @param o l'objet applicatif
	 */
	public void write(Serializable o) {
		if(this.backup == null) {
			try {
				createBackup(o);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		this.writeCount++;
	}

	/**
	 * @return la sauvegarde de l'objet restorée
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	private Serializable getBackup() throws IOException, ClassNotFoundException {
		ObjectInputStream steam	= new ObjectInputStream(new ByteArrayInputStream(this.backup));
		Serializable o  		= (Serializable) steam.readObject();
		steam.close();
		return o;
	}

	/**
	 * créer une sauvegarde d'un ojet applicatif ayant seulement la contrainte d'ête serializable (et non clonable)
	 * @param o un objet applicatif à sauvegarder
	 * @throws IOException
	 */
	private void createBackup(Serializable o) throws IOException {
		ByteArrayOutputStream byteSteam	= new ByteArrayOutputStream();
		ObjectOutputStream outputStream = new ObjectOutputStream(byteSteam);
		outputStream.writeObject(o);
		outputStream.close();
		this.backup = byteSteam.toByteArray();
	}

	/**
	 * @return true si l'objet dispose d'une sauvegarde, false sinon
	 */
	public boolean haveBackup() {
		return this.backup != null;
	}
}
