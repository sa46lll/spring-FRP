# 토비의 봄 TV 스프링 리액티브 프로그래밍

> 리액티브? (FRP, RFP)

외부의 이벤트나 데이터가 발생하면 거기에 대응하는 방식으로 동작하는 코드를 작성하는 것.

## DAY 1

iterator관련 → 람다함수

iteratable ↔ observable

`int i = iter.next(); == notifyObservers(i);`
// 메소드와 데이터 위치가 반대이지만, 기능은 같음.

- 별도의 스레드에서 비동기적으로 동작
```java
ExecutorService es = Executors.newSingleThreadExecutor();`
es.execute(io);
es.shutdown();
// 병렬작업 시 여러개의 작업을 효율적으로 처리하기 위해 제공되는 JAVA의 라이브러리
```


### API components

- 프로토콜
```java
publisher.onSubscribe(subscriber);
subscriber.onSubscribe(new Subscription() { }; //구독이 한번 일어나는 액션
onSubscribe onNext* (onError | onComplete)? // 필수, 선택, 둘중하나

Iterator<Integer> iter = Stream.iterate(1 , a→a+1).limit(10).collect(Collectors.toList());
// 숫자를 만들때 무한대로 만들 수 있는 방법, 리스트로 변환
```

- 진행여부나 결과에 상관없을 때
  `es.execute(( ) → { });`

- 진행상황, 비동기 작업결과를 함유하는 오브젝트, 이벤트를 날리기 때문에결과를 받아오지 않아도됨.
  `Feature<?> f = es.submit(( ) → { })`

## DAY 2

publisher subscriber 패턴

- **publisher → [Data1] ( → Operator → [Data2] → Op2 → [Data3] ) → Subscriber**

publisher가 넘겨주는 데이터에 어떤 작업(오퍼레이터, 트랜스포머)을 걸어서 변화를 주고 넘겨줌.

`sub`: mapPub 호출

`mapPub`: pub 호출

중간에 들어가는 오퍼레이터를 만들어보자. (mapPub, sumPub...)

> **Map (mapPub)**
>

map (d1 → function → d2)

`Publisher<Integer> mapPub = mapPub(pub, s -> s * 10);`

// pub의 원소들을 10배로 불림.

// Function<Integer, Integer>: int로 받아서 int로 리턴

`mapSub2`: mapSub를 한번 더 가공해서 넘겨줌.


> **Map2 (sumPub)**
>

mapPub: 10개가 날라오면 모두 sub에게 10개를 넘김.

sumPub: 계산만 하고 있다가 모두 날라오면 그때 데이터를 던짐.

```java
@Override
public void onNext(Integer item) {
   sum += item;
}

@Override
public void onComplete() {
   sub.onNext(sum);
   sub.onComplete();
}
```

> **Map3 (reducePub)**
>

`reducePub`

: 퍼블리셔가 주는 데이터를 계속 가공하다가 onComplete를 받았을 때 한번만 넘겨주긴 할건데, 중간에 데이터는 어떤식으로 축적을 할것인가.. 내가 정하겠다.

첫번째 데이터 결과값 → 두번째 연산 → 두번째 데이터 결과값 → 세번째 연산 ...

최종적인 값 하나만 리턴

`Publisher<Integer> reducePub = *reducePub*(pub, 0, (BiFunction<Integer, Integer, Integer>)(a, b)->a+b);`

// BiFunction<Integer, Integer, Integer>: int와 int를 받아서 int 리턴

> **Generic Type 변환**
>

`private static <T, R> Publisher<R> mapPub(Publisher<T> pub, Function<T, R> f) {`

**source가 되는 Pub가 있고 그것을 넘겨서 Sub가 받는 데이터의 타입을 다르게 가져가고 싶다.**

<T>: source 타입
<R>: 새로 만드는 퍼블리셔의 타입

T를 넣어서 R리턴하는 퍼블리셔

```java
private static Publisher<**String**> reducePub(Publisher<**Integer**> pub, **String** init, BiFunction<**String, Integer, String**> bf) {
   return new Publisher<String>() {
      @Override
      public void subscribe(Subscriber<? super String> sub) {
         pub.subscribe(new DelegateSub<Integer, String>(sub){
            String result = init;

            @Override
            public void onNext(Integer item) {
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
```

// Generic 타입을 한번에 지정하려하지 말고 구체적인 타입으로 바꿔주고 시도해보자.

`private static <T, R> Publisher<R> reducePub(Publisher<T> pub, R init, BiFunction<R, T, R> bf) {`

T → R
리턴하는 값은 Publisher<R>, 인자로 제공받는 publisher<T>,

> **Flux**
>

Flux: Publisher의 일종으로 subscribe도 할 수 있다.

> **.log()**
>

: 내부구조를 잘 보여줌

`reactor.Flux.Create.1`  == Flux.create()

1. onSubscribe: 시작
2. request(unbounded): Long_MAX_Integer 값을 보냄. 제한없이 다 보냄
3. onNext()
4. onComplete

-  Reactor WebFlux API Documents
[https://projectreactor.io/docs/core/release/api/reactor/core/publisher/Flux.html#buffer--](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/Flux.html#buffer--)

## DAY 3

> **로그를 쓰는 이유**
>

스레드 이름을 앞에 찍어주기 때문

> **핵심 스레드가 블로킹 구조라면**
>

하나의 작업(이벤트)를 기다리는 동안에 해당 스레드를 블로킹하고 있으면, 서버가 가지고 있는 스레드풀이 금방 꽉 차서 더이상 추가적인 요청을 받을 수가 없음. 큐마저도 꽉차서 서비스가 가능하지 않다는 에러가 나면서 처리율이 급격히 떨어짐.

그래서 같은스레드 안에서 Pub와 Sub가 직렬적으로 돌아가는 방식의 코드를 작성하지 않는다.

```java
Publisher<Integer> pub = sub -> {
    sub.onSubscribe(new Subscription() { // sub의 onSubscribe 요청 -> 1
        @Override // 하나의 구독이라는 릴레이션에서 sub가 데이터를 요청함.(request)
        public void request(long n) { -> 3
            sub.onNext(1);
            sub.onNext(2);
            sub.onNext(3);
            sub.onNext(4);
            sub.onNext(5);
            sub.onComplete();
        }

        @Override
        public void cancel() {

        }
    });
```

```java
pub.subscribe(new Subscriber<Integer>() {
//sub 따로 안만들고 sub 인터페이스를 구현한 익명클래스를 만들어서 오브젝트를 받아 넘김
    @Override 
    public void onSubscribe(Subscription s) { //subscription 정보를 주면 무조건 request
				log.debug("onSubscribe");
        s.request(Long.MAX_VALUE); -> 2
    }

    @Override
    public void onNext(Integer integer) {
				log.debug("onNext: {}", integer);
    }

    @Override
    public void onError(Throwable t) {
				log.debug("onError: {}", t);
    }

    @Override
    public void onComplete() {
				log.debug("onComplete");
    }
});
```

> **전체적인 구조 설명**
>

넘어온 sub에 onSubscribe 메소드를 호출하면 pub.onSubscription 실행.

subscription 넘어온거에 request 호출했으니까 위에 request 호출

> **request() 부분을 별도의 스레드에 동작하고 메인스레드는 빠져나가게 하려면..**
>

**Scheduler 두가지 방식 →**

1. **subscribeOn**

   : subscribe 하는 시점부터

   하나의 Pub과 Sub 사이에 끼어들어가는 구조, 모든 과정을 subscribeOn에서 지정을 해주는 것. 스케줄러를 통해서 pub(윗부분)이 동작하도록 해달라

   *pub과 sub를 이어주는 오퍼레이터*

    ```java
    // pub
    
    Publisher<Integer> subOnPub = sub -> { //sub는 밑에 new Subscriber 부분
        pub.subscribe(sub);
    };
    
    //sub
    ```

   // 동시에 딱 한개의 스레드만 제공해주는 스레드풀 (코어 스레드 개수와 max 스레드 개수 = 1
   그 이상으로 요청하면 큐에 넣고 대기하는 구조.

   `sub → subOnPub 오퍼 → pub`

   타고 올라가는데 subOnPub 시점에서 싱글스레드를 생성해서 넘겨줌

   **이렇게하면 메인스레드를 블로킹하지 않고 pub, sub 작업을 모두 수행할 수 있다.**

   > **subscribeOn 쓰는 주요한 이유**
   >

   publisher가 아주 느린 경우.. → blocking IO를 사용, 처리하는 consumer 쪽은 훨씬 빠른 경우
   
    데이터 생성하는데 시간이 오래 걸리거나 예측할 수 없는 경우


   - **결과**
     - main 스레드는 Exit까지 하고 탈출, 그 후에 subscribe를 호출해서 → subOnPub → pub 올라가면서 새로운 싱글 스레드만들면서 그 이후의 작업이 실행.


2. **publishOn**

   : subscribe, request까지는 처음에 시작한 스레드(pub)에서 진행하고, onNext, onError, onComplete를 subscriber가 별도의 스레드로 뽑아내는 오퍼레이터

   > **publisherOn 쓰는 주요한 이유**
   >

   데이터 생성(publisher)은 빠르게 동작하지만, subscriber가 상대적으로 느릴 때 사용.

   중계를 해주는 중간에 wrapping하고 있는 subscriber를 만들자.

   onSubscribe~request까지는 main에서 하고 그 이후는 singleThread에서 수행.

   데이터를 빠르게 생성, 데이터 받는쪽이 느리니까 ExecutorService로 non-blocking처리함.

   요청이 동시에 날라와도 singleThread로 해주었기 때문에 코어가 한개라서 순차적으로 사용이 됨. subscription 하나당 singleThreadExecutor 하나씩 사용해야 됨.


3. **두개를 동시에 사용**

    onSubscribe, request(): subOnPub에서 만들어진 스레드풀
    
    그 이후에 퍼블리싱을 할 때는 별개의 스레드에서 작업

> **new ThreadFactory() {}**
>

스레드의 여러가지 값들을 초기화할 수 있는것

> **new CustomizableThreadFactory() {} ⇒ 유용함**
>

손쉽게 ThreadFactory를 만들수 있게 준비된 코드가 있다. ThreadFactory 기본동작 바꿀 수 있음.

> **스레드풀 shutdown**
>

pubOnPub 에서 shutdown 기능을 추가하려면 onError()이나 onComplete()에서 해야하겠죠?
cancel()도 있겠지만.. 나중에


> **FluxScEx**
>

이것만으로는 실행되지 않는다.⇒ sleep()을 걸어라

```java
Flux.interval(Duration.ofMillis(500))
        .subscribe(System.out::println);
```

> **스레드**
>

// 유저가 만든 user는 메인이 종료되도 종료되지 않는다. (main과는 별개)

interval이 만들어내는 timer(or 패러렐)스레드는 일반적으로 만들어쓰는 user 스레드가 아니고 daemon 스레드이다.

**user스레드가 하나도 남지 않고 daemon 스레드만 남아있으면 강제로 종료함.**
JVM에서 user 스레드가 1개라도 남아있으면 종료되지 않는다. 마찬가지로 그동안은 daemon 스레드도 종료되지 않는다.

⇒ sleep()을 발생시켜야 함.

> **interval**
>

데이터를 정신없이 날라오는것을 받는게 아니라 1초에 한개씩만 샘플링해서 받을 경우
그중에서 쭉 날라오는것중 마지막 것만 건지는 경우
데이터를 선별적으로 사용하거나 통계를 내는 경우..
이런경우 시뮬레이션 하는 과정에서 interval 을 걸면 매우 편하다.
짧은 시간동안 많은 숫자를 건져낼 수 있다.

> **take()**
>

상위 몇개만 받아오는 경우


> **IntervalEx**
>
> **Operator이 하는 역활 3가지**
>
1. 데이터를 변환, 조작 (map, reduce)
2. 스케줄링 (subscribeOn, publishOn)
3. 퍼블리싱하는 것을 컨트롤링 (대표적으로 take)

그래서, subscribe이 데이터를 그만 받는 방법도 있다.

## DAY4

> **Future**
>

: 비동기적인 작업에 대한 결과를 가지고 있는 것.

> **new cachedThreadPool()**
>

maximum 제한이 없고 요청할 때마다 스레드를 새로 만들고, 다 사용한 스레드를 반납하면 재사용 가능

> **Future**
>

비동기 작업의 결과물인 es.submit() 부분의 결과를 메인스레드로 가져오고 싶을 때

이 값을 가져오는 get()이 있다.

Future.get():  es.submit() 작업이 완료할 때까지 블로킹이 되어있다. ⇒ Blocking 메소드

나름 블로킹 메소드도 비동기적인 시스템 구성할 때 유용하게 씀.

→ 여기선 스레드를 두개 만들었지만 필요가 없음.

future f 가 비동기 작업의 결과물은 아니지만 방법을 제공해줌. f.get()가 결과물을 가져오듯이..

> **비동기 작업의 결과물을 어떻게 가져올 것이냐**
>
1. Future 핸들러

   : 작업의 결과를 담고있는 Future와 같은 핸들러를 리턴받아서 결과가 완료됐는지도 체크하고, 결과를 달라고도 호출(blocking)

   이경우 예외가 발생했을때 get메소드 자체에서 예외가 발생하기 때문에 trycatch 블럭 생성

   덜 우아함.

2. Callback

   : 미래에 실행될 작업을 갖다가 조건이 충족이 되면 작업해주어라

   > **FutureTask**
   >

   Future 자체를 object로 만드는 방법

   비동기 작업을 내장하고 있는 퓨처 인터페이스를 구현한 클래스

   `es.execute(f);`

   future 를 또 생성해달라 할 필요는 없으니까 submit → execute()

   > **future의 get을 하지 않고도 가져올 수 있는 방법**
   >

   done()

   : future.get()을 하지 않았는데 비동기 작업이 완료됐을때 get()을 수행할 수 있도록 해놓음.
   → Hello 출력

   > **Callback Future 클래스**
   >

   SuccessCallback {}

   : 비동기 작업이 정상적으로 종료가 됐으면 어떤 작업을 해야할지 담을 수 있는 콜백 인터페이스

   [`this.sc](http://this.sc) = sc;`

   콜백함수가 바로 실행이 될게 아니니까 저장해놔야 함.

   > **null이 어떤상황에서도 들어오지 못하게 방지하는 코드**
   >

   `if (sc == null) throw null;`

   `this.sc = Objects.*requireNonNull*(sc);`

   null이면 nullPointException

   > **CallbackFuture(callcable, sc)**
   >

   callable: 내가 수행할 비동기 작업을 담은 callable interface를 구현한 람다식

   sc: 성공했을 때, 어떤 코드를 실행할 것이냐

   `result -> System.*out*.println(result))`

   > **interface ExceptionCallback {} 생성 이유**
   >

   get()을 호출하면 콜백함수 내 비동기작업에서가 아니라 메인에서 자연스럽게 나와줬으면 좋겠다. → 우아하고 깔끔하다.

   비동기 작업에서 에러가 발생하면 successCallback 대신 ExceptionCallback

   > InterruptedException
   >

   현재 스레드에서 다시 interrupt() 하면 충분

   > ExecutionException
   >

   비동기 작업 내에서 예외 발생 → ec.onError(e.getCause()) 포장한걸 까서 넣어라

    ```java
    package toby.live3;
    
    import lombok.extern.slf4j.Slf4j;
    
    import java.util.Objects;
    import java.util.concurrent.*;
    
    @Slf4j
    public class FutureEx {
    
        interface SuccessCallback{
            void onSuccess(String result);
        }
    
        interface ExceptionCallback {
            void onError(Throwable t);
        }
    
        public static class CallbackFutureTask extends FutureTask<String> {
            SuccessCallback sc;
            ExceptionCallback ec;
    
            public CallbackFutureTask(Callable<String> callable, SuccessCallback sc, ExceptionCallback ec) {
                super(callable);
                this.sc = Objects.requireNonNull(sc); // null 이 아니면 그 값을 그대로 리턴함.
                this.ec = Objects.requireNonNull(ec);
            }
    
            @Override
            protected void done() {
                try {
                    sc.onSuccess(get());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException e) {
                    ec.onError(e.getCause());
                }
            }
        }
    
        public static void main(String[] args) throws ExecutionException, InterruptedException {
            ExecutorService es = Executors.newCachedThreadPool();
    
            CallbackFutureTask f = new CallbackFutureTask(() -> {
                Thread.sleep(2000);
                if (1==1) throw new RuntimeException("Async ERROR!!!"); // Error Test
    log.info("Async");
                return "Hello";
            },
                    System.out::println,
                    e -> System.out.println("Error: " + e.getMessage()));
    
            es.execute(f);
            es.shutdown();
        }
    }
    
    ```

   > 성격이 다른 기술적인/비즈니스적인 코드가 짬뽕되어있는건 좋지 않다.
   >

   분리하고 추상화할 수 있어야 한다.

   > **Spring 에서 비동기작업 처리하는 방법**
   >

   `ConfigurableApplicationContext`

   tomcat이 백그라운드에 뜨는데 그거 빼고 바로 실행을 종료할 수 있는거

   `ApplicationRunner`

   springboot application이 뜨면서 바로 실행이 될 코드를 만들때

   `Thread.*sleep*(1000);`

   시간이 오래걸리는 작업

   > **Spring에서 비동기 작업을 수행하려면**
   >

   @Async 적용 → 비동기 작업은 작업을 수행하는 메소드에서 결과값을 바로 줄 수가 없다.

   그래서 Future 사용

   이런 계산을 동시에 시행하는 코드를 짤리는 없지만, 오랜시간 걸리는 장시간의 작업을 수행시키는 경우(배치작업이나 백그라운드 작업)에 쓰인다. 스케줄러를 걸어서 특정 시간이 되면 수행하게 할수 있지만, 클라이언트에서 액션을 하면 시작을 하게 할때 쓰인다.

   > **그러면 결과가 나올때까지 get()에서 대기해야하냐..**
   >

   장시간 수행되는 작업에 대해 리턴하는 방법은,

    1. 결과를 DB에 넣거나 저장매체기술에 넣고 DB를 후에 access해보는 코드
    2. Future라는 핸들러를.. 컨트롤러라고 생각하면 Http 세션에 저장할수 있는데 세션에 future를 저장하고 바로 리턴해라. 작업이 완료됐는지 궁금하면 DB에 굳이 타지않고 다음 컨트롤러 메소드에서 세션에서 future 값을 꺼내와라. isdone()을 수행해봐라 false면 진행중입니다, true면 그때 값을 끌어와서 진행해줘라.

   > **ListenableFuture**
   >

   FutureTask를 우겨넣을 방법이 없나.. 없다

   리스너가 옵저버블

   스프링꺼 (자바표준 아님)

   `f.addCallback`

   successCallback, FailureCallback 받을 수 있다. → 이렇게 비동기 작업 결과를 처리하는 코드 만들 수 있다.

   **장점: 콜백을 대기해야하는게 아니라 바로 빠져나가면 그만이다.**

   > **@Async**
   >

   실제로 이것만 댕강 가져다 놓으면 안된다. 요청할때마다 스레드를 새로 만들기 때문이다. 굉장히 많은 CPU와 메모리 자원을 낭비한다. 일주일에 한번만 실행하면 그래도 되는데 @Bean을 등록하면 좋다.

   > **@Bean 해결책**
   >

   Executor, ExecutorService, ThreadPoolTaskExecutor(기본적으로 얘 쓰면 됨)

   > **ThreadPoolTaskExecutor**
   >

   @Async만 있으면 기본적으로 주는 스레드풀을 사용하지만, Executor, ExecutorService, ThreadPoolTaskExecutor가 포함된 @Bean이 있으면 얘를 쓰게 되어있다.

   비동기 작업이 많으면 `@Async(value = "tp")` 이런식으로 스레드풀을 지정해줄수 있다.

   - `setCorePoolSize` 정할 수 있다. (기본적으로는 몇개의 스레드풀을 생성함.)
   첫번째 스레드 요청이 오면 그때 스레드풀을 생성함.
   - `setQueueCapacity` 대기하는 큐 개수
   - `setMaxPoolSize`
        - CorePoolSize → Queue → MaxPoolSize
        - CoreThread가 다차면 Queue를 채우고 Queue가 다차면 MaxPool까지 채운다.
   - `setKeepAliveSeconds` MaxPoolSize 까지 다 차고 KeepAlive 시간동안 할당이 안되면 제거해나감. (불필요한 메모리 제거)
   - `setTaskDecorator` 스레드 생성, 반환하는 시점에 앞뒤에 콜백을 걸어준다.(로그를 걸어서 스레드가 언제 얼만큼 할당이되고 사용되고 반환되는지 알고싶을 때 분석하고싶을때 사용)
   - `setThreadNamePrefix` 스레드 이름 변경
   - `initialize()` 초기화하고 리턴해주면 됨.
    