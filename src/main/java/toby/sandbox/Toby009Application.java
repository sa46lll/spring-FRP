package toby.sandbox;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.AsyncRestTemplate;

/**
 * 비동기 RestTemplate과 비동기 MVC의 결합
 */

@SpringBootApplication
public class Toby009Application {

    @RestController
    public static class MyController {
        AsyncRestTemplate rt = new AsyncRestTemplate();

        @GetMapping("/rest")
        public ListenableFuture<ResponseEntity<String>> rest(int idx) {
            return rt.getForEntity("http://localhost:8081/service?req={req}", String.class, "hello" + idx);
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(Toby009Application.class, args);
    }
}
