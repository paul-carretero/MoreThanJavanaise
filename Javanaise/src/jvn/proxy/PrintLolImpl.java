package jvn.proxy;

public class PrintLolImpl implements PrintLolItf{

	private static final long serialVersionUID = -3940213352683095118L;

	@Override
	public void printLol() {
		System.out.println("lol");
	}
	
	public static void main(String[] args) {
		//PrintLolItf foo = (PrintLolItf) JvnProxy.newInstance(new PrintLolImpl()); 
		//foo.printLol(); 
	}

}



