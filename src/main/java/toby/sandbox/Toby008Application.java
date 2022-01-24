package toby.sandbox;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;

import java.util.concurrent.Future;

@SpringBootApplication
@Slf4j
@EnableAsync
public class Toby008Application {

    @Component
    public static class MyService {
        @Async
        public Future<String> hello() throws InterruptedException {
            log.info("hello()");
            Thread.sleep(2000);
            return new AsyncResult<>("Hello");
        }
    }

    public static void main(String[] args) {
        try (ConfigurableApplicationContext c = SpringApplication.run(Toby008Application.class, args)){
        }
    }

    @Autowired MyService myService;

    @Bean
    ApplicationRunner run(){
        return args -> {
            log.info("run()");
            Future<String> f = myService.hello();
            log.info("exit: " + f.isDone());
            log.info("result: " + f.get());
        };
    }


}
