package toby.live;

import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.Flow.Publisher;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;

public class PubSub { //publisher, subscriber
	public static void main(String[] args) {
		Iterable<Integer> itr = Arrays.asList(1, 2, 3, 4, 5);
		
		Publisher p = new Publisher() {

			@Override
			public void subscribe(Subscriber subscriber) {
				// TODO Auto-generated method stub
				subscriber.onSubscribe(new Subscription() {
					Iterator<Integer> it = itr.iterator();

					@Override
					public void request(long n) {
						try {
							while(n-- > 0) {
								if (it.hasNext()) {
									subscriber.onNext(it.next());
								} else {
									subscriber.onComplete();
									break;
								}
							}
						}
						catch (RuntimeException e) {
							subscriber.onError(e);
						}
						
					}

					@Override
					public void cancel() {
						
					}
				});
			}
		};
		
		Subscriber<Integer> s = new Subscriber<Integer>() {

			Subscription subscription;

			@Override
			public void onSubscribe(Subscription subscription) {
				System.out.println("onSubscribe");
				this.subscription = subscription;
				this.subscription.request(1);
			}

			@Override
			public void onNext(Integer item) {
				System.out.println("onNext" + item);
				this.subscription.request(1);
			}

			@Override
			public void onError(Throwable throwable) {
				System.out.println("onError");				
			}

			@Override
			public void onComplete() {
				System.out.println("onComplete");				
			}
		};
		
		p.subscribe(s);
	}
}
