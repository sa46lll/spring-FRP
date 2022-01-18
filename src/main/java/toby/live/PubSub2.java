package toby.live;

import java.util.List;
import java.util.concurrent.Flow.Publisher;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.extern.slf4j.Slf4j;

/*
 * Publisher -> [Data1] -> mapPub -> [Data2] -> Subscriber(LogSub)
 * 										<- subscribe(logSub)
 * 										-> onSubscribe(s)
 * 										-> onNext
 * 										-> onNext
 * 										-> onComplete
 */

@Slf4j
public class PubSub2 { //publisher, subscriber
	
	private static final Logger LOG = LoggerFactory.getLogger(PubSub2.class);


	public static void main(String[] args) {
		
		Publisher<Integer> pub = iterPub(Stream.iterate(1 , a->a+1).limit(10).collect(Collectors.toList()));
		Publisher<Integer> mapPub = mapPub(pub, s -> s * 10);
//		Publisher<Integer> mapPub2 = mapPub(mapPub, s -> -s);
		
		Subscriber<Integer> s = logSub();
		
		mapPub.subscribe(s);
//		mapPub2.subscribe(s);

	}


	private static Publisher<Integer> mapPub(Publisher<Integer> pub, Function<Integer, Integer> f) {
		
		return new Publisher<Integer>() {
			@Override
			public void subscribe(Subscriber<? super Integer> sub) {
				pub.subscribe(new Subscriber<Integer>() {

					@Override
					public void onSubscribe(Subscription subscription) {
						sub.onSubscribe(subscription); // onSub, onError, onComplete 그대로 넘겨줌
					}

					@Override
					public void onNext(Integer item) {
						sub.onNext(f.apply(item)); //함수 변환 적용
					}

					@Override
					public void onError(Throwable throwable) {
						sub.onError(throwable);
					}

					@Override
					public void onComplete() {
						sub.onComplete();
					}
				});
			}
		};
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