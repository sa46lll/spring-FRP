package toby.sandbox;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.Callable;

@SpringBootApplication
@Slf4j
@EnableAsync
public class Toby0081Application {

    @RestController
    public static class MyController {

        @GetMapping("/callable")

        //*   // v1. callable 비동기
        public Callable<String> callable() throws InterruptedException {
            log.info("callable");
            return () -> {
                log.info("async");
                Thread.sleep(2000);
                return "hello";
            };
        }
        /*/   // v2.
        public String callable() throws InterruptedException {
            log.info("async");
            Thread.sleep(2000);
            return "hello";
        }
        //*/
    }


    public static void main(String[] args) {
        SpringApplication.run(Toby0081Application.class, args);
    }
}
