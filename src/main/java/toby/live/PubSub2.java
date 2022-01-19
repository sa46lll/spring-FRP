package toby.live;

import java.util.List;
import java.util.concurrent.Flow.Publisher;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;
import java.util.function.BiFunction;
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
//		Publisher<String> mapPub = mapPub(pub, s -> "[" + s + "]");
//		Publisher<Integer> mapPub2 = mapPub(mapPub, s -> -s);
//		Publisher<Integer> sumPub = sumPub(pub); // 합계를 계산해주는 퍼블리셔
		Publisher<StringBuilder> reducePub = reducePub(pub, new StringBuilder(), (a, b)->a.append(b+","));

		reducePub.subscribe(logSub());

	}

private static <T, R> Publisher<R> reducePub(Publisher<T> pub, R init, BiFunction<R, T, R> bf) {
		return new Publisher<R>() {
			@Override
			public void subscribe(Subscriber<? super R> sub) {
				pub.subscribe(new DelegateSub<T, R>(sub){
					R result = init;

					@Override
					public void onNext(T item) {
						result = bf.apply(result, item);
					}

					@Override
					public void onComplete() {
						sub.onNext(result);
						sub.onComplete();
					}
				});
			}
		};
	}

	/*
	private static Publisher<Integer> sumPub(Publisher<Integer> pub) {
		return new Publisher<Integer>() {
			@Override
			public void subscribe(Subscriber<? super Integer> sub) {
				pub.subscribe(new DelegateSub(sub){
					int sum = 0;

					@Override
					public void onNext(Integer item) {
						sum += item;
					}

					@Override
					public void onComplete() {
						sub.onNext(sum);
						sub.onComplete();
					}
				}); // 구독요청
			}
		};
	}
	 */


	// T -> R
	private static <T, R> Publisher<R> mapPub(Publisher<T> pub, Function<T, R> f) {
		return new Publisher<R>() {
			@Override
			public void subscribe(Subscriber<? super R> sub) { // sub -> 받는쪽이므로 <R>
				pub.subscribe(new DelegateSub<T, R>(sub){
					@Override
					public void onNext(T item) {
						sub.onNext(f.apply(item));
					}
				});
			}
		};
	}


	private static <T> Subscriber<T> logSub() {
		return new Subscriber<T>() {
			@Override
			public void onSubscribe(Subscription subscription) {
				LOG.debug("onSubscribe");
				subscription.request(Long.MAX_VALUE);
			}

			@Override
			public void onNext(T item) {
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
