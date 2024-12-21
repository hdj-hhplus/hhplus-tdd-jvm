# 동시성 제어 방식에 대한 분석 및 보고서
## 1. 동시성 제어?
- 동시성 제어(Concurrency Control)는 여러 트랜잭션이 동시에 실행될 때 발생할 수 있는 문제들을 해결하기 위한 기술.
- 여러 스레드나 프로세스가 공유 자원에 접근할 때 발생할 수 있는 데이터 불일치나 오류를 방지하기 위해 동시성 제어를 사용한다.

## 2. 동시성 제어 방법
### 2-1. 낙관적 락
- 트랜잭션 대부분 충돌이 발생하지 않는다고 낙관적으로 가정하는 방법이다.
- 테이블에 버전(Version) 컬럼을 추가한다. 트랜잭션 내 처음 조회한 버전과 데이터 `commit`될 때 버전을 비교하고, 일치할 때 `commit`을 진행한다.

### 2-2. 비관적 락
- 충돌을 예상하고 트랜잭션이 시작될 때 락을 걸어서 정합성을 보장하는 방법이다.
- 락을 걸고 데이터를 수정하는 동안, 다른 사용자는 자원을 이용할 수 없기에 대기해야 한다.

### 2-3. `Atomic` 변수
- 멀티 스레드 환경에서 원자성을 보장해주는 변수로, 내부적으로 **Compare-and-swap(CAS)** 알고리즘을 사용하여 동시성을 제어한다.
- **Compare-and-swap(CAS)** 란?
    - 계산 수행 결과값과 수행 이전 값을 비교하여 이전 값이 현재 메모리에 동일하게 유지되고 있는지 확인한다. 일치하면 메모리 변수에 계산 결과를 반영하고, 일치하지 않는다면 실패하고 재시도한다.
    - 다른 lock 기반 방식과는 달리 값이 업데이트 되는 중에도 다른 스레드의 접근을 허용한다.

### 2-4. `synchronized` 키워드
- 멀티 스레드 환경에서 동시성 제어를 위해 공유 객체를 동기화 하는 키워드다.
- 동기화 블록 또는 메서드에 사용되어 **한 스레드가 실행 중일 때 다른 스레드의 접근을 차단**한다.

```java
public synchronized UserPoint chargePoint(UserPointCommand command) {
	// ( 비즈니스 로직 )
}
```

### 2-5. `ReentrantLock` 클래스
- `synchronized`보다 세밀한 락 제어가 가능하다.
- 재진입이 가능하며 락을 명시적으로 획득 및 해제해야 한다.
- **공정 옵션**과 같은 고급 동기화 기능을 제공한다.

```java
private final ReentrantLock lock = new ReentrantLock();

public UserPoint chargePoint(UserPointCommand command) {
    lock.lock(); // 락 획득
    try {
        // ( 비즈니스 로직 )
    } finally {
        lock.unlock(); // 락 해제
    }
}
```

### 2-6. `ConcurrentHashMap` 컬렉션
- 락, CAS 연산, 노드 동기화 등의 기법을 사용하여, 동시성을 지원하는 `HashMap` 자료구조다.
- **특정 키에 대한 동작만 락을 걸어** 높은 성능을 유지한다.
- 읽기 작업은 락 없이 가능하며, 쓰기 작업은 세분화된 락으로 동기화한다.

```java
// ConcurrentHashMap을 사용하여 각 id에 대한 잠금을 저장
private final ConcurrentHashMap<Long, Object> locks = new ConcurrentHashMap<>();

public synchronized UserPoint chargePoint(UserPointCommand command) {
	Object lock = locks.computeIfAbsent(command.id(), key -> new Object());
	// ( 비즈니스 로직 )
}
```

## 3. 동시성 제어 방법 채택

- 이번 과제에는 데이터베이스를 연결하는 대신 List를 데이터 저장소로 사용한다. 또한, `database` 패키지 코드를 수정할 수 없다. 
  - 이런 이유로 `낙관적 락`과 `비관적 락`, `Atomic`은 우선 제외한다.

- `synchronized` 키워드는 동기화 블록 또는 메서드에 사용되어 한 스레드가 실행 중일 때 다른 스레드가 접근할 수 없어 병목현상이 발생한다.
  - 순차적 진행이란 조건에 충족하지 않으니 제외한다.

- `ConcurrentHashMap`을 사용한 이유는 특정 ID에 대해서만 락을 걸어 성능을 최적화할 수 있기 때문이다. 이 방식은 락을 세분화하여 여러 스레드가 동시에 작업할 수 있게 해준다. 또한, `ReentrantLock`의 공정 옵션을 활용하면 락을 공정하게 처리할 수 있어 동기화도 효율적으로 가능하다.


## 참고
- [멀티스레드 환경 동시성 문제 해결](https://f-lab.kr/insight/concurrency-issues-multithreading-20240626)
- [Java에서 동시성 문제 해결하는 다양한 기법과 성능 평가](https://jaeseo0519.tistory.com/399#11.%20Performance-1)
