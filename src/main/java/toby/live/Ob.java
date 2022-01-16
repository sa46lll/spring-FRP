package toby.live;

import java.util.*;

@SuppressWarnings("deprecation")
public class Ob {
	
	static class IntObservable extends Observable implements Runnable{

		@Override
		public void run() {
			for (int i=1; i<=10; i++) {
				setChanged();
				notifyObservers(i);		//push
				// int i = it.next();			//pull
			}
		}
	}

	public static void main(String[] args) {
		Observer ob = new Observer() {

			@Override
			public void update(Observable o, Object arg) {
				System.out.println(arg);
			}
		};
		
		IntObservable io = new IntObservable();
		io.addObserver(ob);
		
		io.run();
	}
}
