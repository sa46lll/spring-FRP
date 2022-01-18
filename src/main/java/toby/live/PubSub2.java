package toby.live;

import java.util.List;
import java.util.concurrent.Flow.Publisher;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PubSub2 { //publisher, subscriber
	
	private static final Logger LOG = LoggerFactory.getLogger(PubSub2.class);


	public static void main(String[] args) {
		
		Publisher<Integer> p = iterPub(Stream.iterate(1 , a->a+1).limit(10).collect(Collectors.toList()));
		
		Subscriber<Integer> s = logSub();
		
		p.subscribe(s);
		

	}


	private static Subscriber<Integer> logSub() {
		return new Subscriber<Integer>() {
			@Override
			public void onSubscribe(Subscription subscription) {
				LOG.debug("onSubscribe");
				subscription.request(Long.MAX_VALUE);
			}

			@Override
			public void onNext(Integer item) {
				LOG.debug("onNext:{}", item);
			}

			@Override
			public void onError(Throwable throwable) {
				LOG.debug("onError:{}", throwable);		
			}

			@Override
			public void onComplete() {
				LOG.debug("onComplete");			
			}
		};
	}


	private static Publisher<Integer> iterPub(List<Integer> iter) {
		return new Publisher<Integer>() {

			@Override
			public void subscribe(Subscriber<? super Integer> subscriber) {
				subscriber.onSubscribe(new Subscription() {
					@Override
					public void request(long n) {
						try {
							iter.forEach(s -> subscriber.onNext(s));
							subscriber.onComplete();
						}
						catch(Throwable t) {
							subscriber.onError(t);
						}
					}
					@Override
					public void cancel() {
						
					}
				});
			}
		};
	}
}
