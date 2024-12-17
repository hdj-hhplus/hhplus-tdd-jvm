package io.hhplus.tdd.point.service;

import io.hhplus.tdd.point.command.UserIdCommand;
import io.hhplus.tdd.point.command.UserPointCommand;
import io.hhplus.tdd.point.entity.PointHistory;
import io.hhplus.tdd.point.entity.UserPoint;
import io.hhplus.tdd.point.enumtype.PointErrorCode;
import io.hhplus.tdd.point.enumtype.TransactionType;
import io.hhplus.tdd.point.error.BusinessException;
import io.hhplus.tdd.point.repository.PointHistoryRepository;
import io.hhplus.tdd.point.repository.UserPointRepository;
import io.hhplus.tdd.point.service.impl.PointServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PointServiceTest {

    @Mock
    private UserPointRepository userPointRepository;
    @Mock
    private PointHistoryRepository pointHistoryRepository;

    @InjectMocks
    private PointServiceImpl pointService;

    @Test
    @DisplayName("유저 포인트 조회 - 존재하지 않는 유저 포인트 조회 시 0 출력하여 성공")
    void shouldSuccessToGetUserPointWithNoneExistingId() {
        // given
        final Long id = 1L;
        final UserIdCommand command = new UserIdCommand(id);
        // findById 호출 시 empty 반환하도록 설정
        when(userPointRepository.findById(id)).thenReturn(Optional.empty());

        // when
        final UserPoint userPoint = pointService.getPoint(command);

        // then
        assertThat(userPoint).isNotNull();
        assertThat(userPoint.point()).isZero();
    }

    @Test
    @DisplayName("유저 포인트 조회 - 존재하는 유저 포인트 조회 시 1000 출력하여 성공")
    void shouldSuccessToGetUserPointWithExistingId() {
        // given
        final Long id = 1L;
        final UserIdCommand command = new UserIdCommand(id);
        // findById 호출 시 존재하는 유저 포인트 (1000) 반환하도록 설정
        when(userPointRepository.findById(id))
                .thenReturn(Optional.of(new UserPoint(id, 1000, System.currentTimeMillis())));

        // when
        final UserPoint userPoint = pointService.getPoint(command);

        // then
        assertThat(userPoint).isNotNull();
        assertThat(userPoint.point()).isEqualTo(1000);
    }

    @Test
    @DisplayName("유저 포인트 히스토리 조회 - 히스토리 리스트가 비어있는 경우")
    void shouldReturnEmptyPointHistoryListWhenNoHistoryExists() {
        // given
        final Long userId = 1L;
        final UserIdCommand command = new UserIdCommand(userId);

        // findAllByUserId 호출 시 빈 리스트 반환하도록 설정
        when(pointHistoryRepository.findAllByUserId(userId)).thenReturn(List.of());

        // when
        final List<PointHistory> historyList = pointService.getHistory(command);

        // then
        assertThat(historyList)
                .isNotNull()
                .isEmpty();
    }

    @Test
    @DisplayName("유저 포인트 히스토리 조회 - 히스토리 리스트가 있는 경우")
    void shouldReturnPointHistoryListWhenHistoryExists() {
        // given
        final long userId = 1L;
        final UserIdCommand command = new UserIdCommand(userId);

        // Mock 데이터 준비
        final List<PointHistory> mockHistoryList = List.of(
                PointHistory.makeEntity(userId, 1000, TransactionType.CHARGE, System.currentTimeMillis()),
                PointHistory.makeEntity(userId, 200, TransactionType.USE, System.currentTimeMillis())
        );
        // findAllByUserId 호출 시 Mock 데이터 반환하도록 설정
        when(pointHistoryRepository.findAllByUserId(userId)).thenReturn(mockHistoryList);

        // when
        final List<PointHistory> historyList = pointService.getHistory(command);

        // then
        assertThat(historyList)
                .isNotNull()
                .hasSize(2)
                .containsExactlyElementsOf(mockHistoryList);
    }

    @Test
    @DisplayName("유저 포인트 충전 - 잔고 최대 금액 초과 시 실패")
    void shouldThrowBusinessExceptionWhenChargeExceedsMax() {
        // given
        final Long id = 1L;
        final Long previousPoint = 9999990L;  // 기존 포인트 9,999,990
        final Long chargeAmount = 20000L;     // 충전할 금액 20,000
        final UserPointCommand command = new UserPointCommand(id, chargeAmount);

        // UserPointRepository의 findById 호출 시 기존 포인트를 가진 유저 반환하도록 설정
        UserPoint existingUserPoint = new UserPoint(id, previousPoint, System.currentTimeMillis());
        when(userPointRepository.findById(id)).thenReturn(Optional.of(existingUserPoint));

        // when
        BusinessException exception =
                assertThrows(BusinessException.class, () -> {
                    pointService.chargePoint(command);
                });

        // then
        assertThat(exception.getErrorCode()).isEqualTo(PointErrorCode.MAX_BALANCE_EXCEEDED);
    }

    @Test
    @DisplayName("유저 포인트 사용 - 잔고 부족 시 실패")
    void shouldThrowBusinessExceptionWhenUsePointExceeds() {
        // given
        final Long id = 1L;
        final Long previousPoint = 500L;  // 기존 포인트 500
        final Long useAmount = 1000L;     // 사용할 금액 1000 (잔고 부족)
        final UserPointCommand command = new UserPointCommand(id, useAmount);

        // UserPointRepository의 findById 호출 시 기존 포인트를 가진 유저 반환하도록 설정
        UserPoint existingUserPoint = new UserPoint(id, previousPoint, System.currentTimeMillis());
        when(userPointRepository.findById(id)).thenReturn(Optional.of(existingUserPoint));

        // when
        BusinessException exception =
                assertThrows(BusinessException.class, () -> {
                    pointService.usePoint(command);
                });

        // then
        assertThat(exception.getErrorCode()).isEqualTo(PointErrorCode.INSUFFICIENT_BALANCE);
    }


    @Test
    @DisplayName("동시성 테스트 - 여러 스레드가 동시에 충전 시 순차적으로 처리되는지 확인")
    void shouldHandleConcurrentChargePointRequests() throws InterruptedException {
        // given
        final long id = 1L;
        final long initialPoint = 0L;
        final long chargeAmount = 100L;
        final int numberOfThreads = 20; // 동시에 접근하는 스레드 수

        // UserPointRepository mock 설정: 최초 접근 시 초기 포인트 반환
        UserPoint initialUserPoint = new UserPoint(id, initialPoint, System.currentTimeMillis());
        when(userPointRepository.findById(id)).thenReturn(Optional.of(initialUserPoint));

        // 스레드 풀 설정
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);
        AtomicInteger successCount = new AtomicInteger(0); // 성공적으로 충전된 요청 수 카운트

        // UserPointRepository의 save 메서드 동작 설정
        when(userPointRepository.save(any(UserPoint.class))).thenAnswer(invocation -> {
            UserPoint savedPoint = invocation.getArgument(0);
            successCount.incrementAndGet();
            return savedPoint;
        });

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
        Long expectedTotalPoints = initialPoint + (chargeAmount * numberOfThreads);
        verify(userPointRepository, times(numberOfThreads)).save(any(UserPoint.class)); // save 메서드가 스레드 수만큼 호출되었는지 확인
        assertThat(successCount.get()).isEqualTo(numberOfThreads); // 성공적으로 저장된 횟수 확인
    }

    @Test
    @DisplayName("데드락 테스트 - 동일 ID에 대해 chargePoint와 usePoint 동시 접근 시 데드락 확인")
    void shouldNotCauseDeadlockWhenUsingSameLock() throws InterruptedException {
        // given
        final Long id = 1L;
        final Long initialPoint = 5000L;
        final Long chargeAmount = 3000L;
        final Long useAmount = 2000L;

        UserPoint initialUserPoint = new UserPoint(id, initialPoint, System.currentTimeMillis());

        when(userPointRepository.findById(id)).thenReturn(Optional.of(initialUserPoint));
        when(userPointRepository.save(any(UserPoint.class))).thenAnswer(invocation -> invocation.getArgument(0));

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
        Future<?> chargeFuture = executorService.submit(chargeTask);
        Future<?> useFuture = executorService.submit(useTask);

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
        verify(userPointRepository, atLeastOnce()).save(any(UserPoint.class));
    }
}
