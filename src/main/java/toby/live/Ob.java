package toby.live;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
				System.out.println(Thread.currentThread().getName() + " " + arg);
			}
		};
		
		IntObservable io = new IntObservable();
		io.addObserver(ob);
		
		ExecutorService es = Executors.newSingleThreadExecutor();
		es.execute(io);
		
		System.out.println(Thread.currentThread().getName() + "   Exit");
		es.shutdown();
	}
}
