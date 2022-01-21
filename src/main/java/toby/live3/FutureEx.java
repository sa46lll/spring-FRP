package toby.live3;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Slf4j
public class FutureEx {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        ExecutorService es = Executors.newCachedThreadPool();

        Future<String> f = es.submit(() -> {
            Thread.sleep(2000);
            log.info("Async");
            return "Hello";
        });

        System.out.println(f.isDone()); // 작업이 완료되면 true
        Thread.sleep(2100);
        log.info("Exit");
        System.out.println(f.isDone());
        System.out.println(f.get()); // Blocking


    }
}
