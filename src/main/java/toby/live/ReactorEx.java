package toby.live;

import org.reactivestreams.Subscriber;
import reactor.core.publisher.Flux;

import java.util.function.Function;

/*
 * Publisher interface를 구현한 유틸리티성 클래스
 * Flux: Publisher의 일종
 */

public class ReactorEx {
    public static void main(String[] args) {
        Flux.<Integer>create(e->{
            e.next(1);
            e.next(2);
            e.next(3);
            e.complete();
        })
        .log() // 내부 동작, Flux.create 에 대한 로그
        .map(s->s*10)
        .log() // map 에 대한 로그
        .reduce(0, (a, b)->a+b)
        .log() // reduce 에 대한 로그
        .subscribe(System.out::println); //s-> System.out.println(s)
    }

}
