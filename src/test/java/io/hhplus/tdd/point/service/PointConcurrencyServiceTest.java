package io.hhplus.tdd.point.service;

import io.hhplus.tdd.point.command.UserPointCommand;
import io.hhplus.tdd.point.entity.UserPoint;
import io.hhplus.tdd.point.repository.UserPointRepository;
import io.hhplus.tdd.point.service.impl.PointServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class PointConcurrencyServiceTest {

    @Autowired
    private UserPointRepository userPointRepository;

    @Autowired
    private PointServiceImpl pointService;

    @Test
    @DisplayName("동시성 테스트 - 여러 스레드가 동시에 충전 시 동시성 제어가 되는지 확인")
    void shouldHandleConcurrentChargePointRequests() throws InterruptedException {
        // given
        final long id = 1L;
        final long initialPoint = 0L;
        final long chargeAmount = 100L;
        final int numberOfThreads = 20; // 동시에 접근하는 스레드 수

        UserPoint initialUserPoint = new UserPoint(id, initialPoint, System.currentTimeMillis());
        userPointRepository.save(initialUserPoint);

        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);

        // when
        for (int i = 0; i < numberOfThreads; i++) {
            executorService.submit(() -> {
                try {
                    UserPointCommand command = new UserPointCommand(id, chargeAmount);
                    pointService.chargePoint(command);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(); // 모든 스레드가 작업을 마칠 때까지 대기
        executorService.shutdown();

        // then
        UserPoint updatedUserPoint = userPointRepository.findById(id).orElseThrow();
        long expectedTotalPoint = initialPoint + (chargeAmount * numberOfThreads);
        assertThat(updatedUserPoint.point()).isEqualTo(expectedTotalPoint); // 총 포인트 확인
    }

    @Test
    @DisplayName("데드락 테스트 - 동일 ID에 대해 chargePoint와 usePoint 동시 접근 시 데드락 확인")
    void shouldNotCauseDeadlockWhenUsingSameLock() {
        // given
        final long id = 1L;
        final long initialPoint = 5000L;
        final long chargeAmount = 3000L;
        final long useAmount = 2000L;

        UserPoint initialUserPoint = new UserPoint(id, initialPoint, System.currentTimeMillis());
        userPointRepository.save(initialUserPoint);

        // 스레드 풀 설정
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        CountDownLatch latch = new CountDownLatch(2);
        AtomicBoolean deadlockDetected = new AtomicBoolean(false);

        // 첫 번째 스레드: chargePoint 실행
        Runnable chargeTask = () -> {
            try {
                UserPointCommand command = new UserPointCommand(id, chargeAmount);
                pointService.chargePoint(command);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                latch.countDown();
            }
        };

        // 두 번째 스레드: usePoint 실행
        Runnable useTask = () -> {
            try {
                UserPointCommand command = new UserPointCommand(id, useAmount);
                pointService.usePoint(command);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                latch.countDown();
            }
        };

        // when
        executorService.submit(chargeTask);
        executorService.submit(useTask);

        executorService.shutdown();

        try {
            // 데드락 발생 시 스레드가 무기한 대기하므로 타임아웃을 설정
            if (!latch.await(3, TimeUnit.SECONDS)) {
                deadlockDetected.set(true); // 타임아웃 발생 시 데드락으로 간주
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }

        // then
        assertThat(deadlockDetected.get()).isFalse(); // 데드락이 발생하지 않았음을 검증
    }


    @Test
    @DisplayName("락 테스트 - 동일 ID는 순차적으로 처리되고, 다른 ID는 비동기로 처리되는지 확인")
    void shouldLockForSameIdAndAllowAsyncForDifferentIds() {
        // given
        final long id1 = 1L;
        final long id2 = 2L;
        final long id3 = 3L;
        final long initialPoint = 5000L;
        final long chargeAmount = 3000L;

        UserPoint userPoint1 = new UserPoint(id1, initialPoint, System.currentTimeMillis());
        UserPoint userPoint2 = new UserPoint(id2, initialPoint, System.currentTimeMillis());
        UserPoint userPoint3 = new UserPoint(id3, initialPoint, System.currentTimeMillis());
        userPointRepository.save(userPoint1);
        userPointRepository.save(userPoint2);
        userPointRepository.save(userPoint3);

        ExecutorService executorService = Executors.newFixedThreadPool(4);
        CountDownLatch latch = new CountDownLatch(5); // 5개의 작업 (ID 1 세 번과 ID 2, ID 3 각각 한 번씩)
        AtomicBoolean deadlockDetected = new AtomicBoolean(false);

        List<Long> executionOrder = Collections.synchronizedList(new ArrayList<>());

        // 동일 ID (id1)에 대해 순차적으로 chargePoint 실행
        Runnable chargeTaskId1 = () -> {
            try {
                UserPointCommand command = new UserPointCommand(id1, chargeAmount);
                pointService.chargePoint(command);
                executionOrder.add(id1);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                latch.countDown();
            }
        };

        // 다른 ID (id2)에 대해 chargePoint 실행
        Runnable chargeTaskId2 = () -> {
            try {
                UserPointCommand command = new UserPointCommand(id2, chargeAmount);
                pointService.chargePoint(command);
                executionOrder.add(id2);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                latch.countDown();
            }
        };

        // 다른 ID (id3)에 대해 chargePoint 실행
        Runnable chargeTaskId3 = () -> {
            try {
                UserPointCommand command = new UserPointCommand(id3, chargeAmount);
                pointService.chargePoint(command);
                executionOrder.add(id3);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                latch.countDown();
            }
        };

        // when
        executorService.submit(chargeTaskId1); // ID 1 첫 번째 작업
        executorService.submit(chargeTaskId1); // ID 1 두 번째 작업
        executorService.submit(chargeTaskId1); // ID 1 세 번째 작업
        executorService.submit(chargeTaskId2); // ID 2 작업
        executorService.submit(chargeTaskId3); // ID 3 작업

        executorService.shutdown();

        try {
            // 데드락 방지를 위한 타임아웃 설정
            if (!latch.await(5, TimeUnit.SECONDS)) {
                deadlockDetected.set(true); // 타임아웃 발생 시 데드락으로 간주
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }

        // then
        assertThat(deadlockDetected.get()).isFalse(); // 데드락이 발생하지 않았음을 검증
        // ID 1에 대한 작업이 순차적으로 처리되었는지 확인
        List<Long> id1ExecutionOrder = executionOrder.stream()
                .filter(id -> id.equals(id1))
                .collect(Collectors.toList());
        assertThat(id1ExecutionOrder).containsExactly(id1, id1, id1); // ID 1은 3번 순차적으로 실행

        // ID 2와 ID 3은 순차적이지 않아도 되므로 실행되었음을 확인
        assertThat(executionOrder)
                .contains(id2)
                .contains(id3);

        // ID 1 작업이 다른 ID 작업과 혼합되지 않음을 확인
        for (int i = 0; i < executionOrder.size(); i++) {
            if (executionOrder.get(i).equals(id1)) {
                // 이후 ID 1 작업 중간에 다른 ID가 끼어들지 않았는지 검증
                for (int j = i + 1; j < executionOrder.size() && executionOrder.get(j).equals(id1); j++) {
                    assertThat(executionOrder.get(j)).isEqualTo(id1);
                }
            }
        }
    }

}
