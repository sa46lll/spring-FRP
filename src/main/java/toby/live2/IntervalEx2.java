package toby.live2;

import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public class IntervalEx2 {
    public static void main(String[] args) {

        Publisher<Integer> pub = new Publisher<Integer>() {
            @Override
            public void subscribe(Subscriber<? super Integer> sub) {
                System.out.println("start");
                sub.onSubscribe(new Subscription() {
                    int no = 0;
                    volatile boolean canceled = false;

                    @Override
                    public void request(long n) {
                        log.debug("pub.request()");
                    ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
                        exec.scheduleAtFixedRate(() -> { // 일정한 시간 간격을 두고 계속 수행하게 함.
                            if (canceled){
                                exec.shutdown();
                                return;
                            }
                            sub.onNext(no++);
                        }, 0, 300, TimeUnit.MILLISECONDS);
                    }

                    @Override
                    public void cancel() {
                        log.debug("pub.cancel()");
                        canceled = true;
                    }
                });
            }
        };

        Publisher<Integer> takePub = new Publisher<Integer>() {
            @Override
            public void subscribe(Subscriber<? super Integer> sub) {
                pub.subscribe(new Subscriber<Integer>() {
                    int count = 0;
                    Subscription subsc;

                    @Override
                    public void onSubscribe(Subscription s) {
                        log.debug("takePub.onSubscribe()");
                        subsc = s;
                        sub.onSubscribe(s);
                    }

                    @Override
                    public void onNext(Integer integer) {
                        log.debug("takePub.onNext()");
                        sub.onNext(integer);
                        if (++count >= 5) {
                            subsc.cancel();
                        }
                    }

                    @Override
                    public void onError(Throwable t) {
                        log.debug("takePub.onError()");
                        sub.onError(t);
                    }

                    @Override
                    public void onComplete() {
                        log.debug("takePub.onComplete()");
                        sub.onComplete();
                    }
                });
            }
        };

        takePub.subscribe(new Subscriber<Integer>() {
            @Override
            public void onSubscribe(Subscription s) {
                log.debug("subscribe.onSubscribe");
                s.request(Long.MAX_VALUE);
            }

            @Override
            public void onNext(Integer integer) {
                log.debug("subscribe.onNext: {}", integer);
            }

            @Override
            public void onError(Throwable t) {
                log.debug("subscribe.onError: {}", t);
            }

            @Override
            public void onComplete() {
                log.debug("subscribe.onComplete");
            }
        });
    }
}

