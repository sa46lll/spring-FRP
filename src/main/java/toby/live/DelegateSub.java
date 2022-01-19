package toby.live;

import java.util.concurrent.Flow;
import java.util.concurrent.Flow.Subscriber;

public class DelegateSub<T, R> implements Subscriber<T> {
    Subscriber sub;

    public DelegateSub(Subscriber<? super R> sub) {
        this.sub = sub;
    }

    @Override
    public void onSubscribe(Flow.Subscription subscription) {
        sub.onSubscribe(subscription); // onSub, onError, onComplete 그대로 넘겨줌
    }

    @Override
    public void onNext(T item) {
        sub.onNext(item); //함수 변환 적용
    }

    @Override
    public void onError(Throwable throwable) {
        sub.onError(throwable);
    }

    @Override
    public void onComplete() {
        sub.onComplete();

    }
}
