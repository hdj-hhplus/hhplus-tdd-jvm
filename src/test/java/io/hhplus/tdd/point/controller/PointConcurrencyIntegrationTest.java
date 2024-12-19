package io.hhplus.tdd.point.controller;

import io.hhplus.tdd.point.command.UserPointCommand;
import io.hhplus.tdd.point.entity.UserPoint;
import io.hhplus.tdd.point.service.PointService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@AutoConfigureMockMvc
class PointConcurrencyIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PointService pointService;

    @Test
    @DisplayName("동시성 테스트 - 여러 스레드가 동시에 충전 요청 시 순차적으로 처리되는지 확인")
    void shouldHandleConcurrentChargePointRequests() throws InterruptedException {
        // given
        final long id = 1L;
        final long chargeAmount = 100L;
        final int numberOfThreads = 100; // 동시 요청 수

        when(pointService.chargePoint(any(UserPointCommand.class)))
                .thenAnswer(invocation -> {
                    UserPointCommand command = invocation.getArgument(0);
                    return new UserPoint(command.id(), command.amount(), System.currentTimeMillis());
                });

        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);
        AtomicInteger successCount = new AtomicInteger(0); // 성공적으로 충전된 요청 수 카운트

        // when
        for (int i = 0; i < numberOfThreads; i++) {
            executorService.submit(() -> {
                try {
                    MvcResult mvcResult = mockMvc.perform(
                            MockMvcRequestBuilders.patch("/point/{id}/charge", id)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(String.valueOf(chargeAmount))
                    ).andReturn();

                    // 성공 시 successCount 증가
                    if (mvcResult.getResponse().getStatus() == HttpStatus.OK.value()) {
                        successCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        // then
        assertThat(successCount.get()).isEqualTo(numberOfThreads); // 모든 요청이 성공적으로 처리되었는지 확인
        verify(pointService, times(numberOfThreads)).chargePoint(any(UserPointCommand.class)); // 서비스 호출 검증
    }

    @Test
    @DisplayName("동시성 데드락 테스트 - 동일 id에 대해 chargePoint와 usePoint 동시 접근 시 데드락 확인")
    void shouldNotCauseDeadlockWhenUsingSameLock() throws InterruptedException {
        // given
        final long id = 1L;
        final long chargeAmount = 3000L;
        final long useAmount = 2000L;

        when(pointService.chargePoint(any(UserPointCommand.class)))
                .thenAnswer(invocation -> {
                    UserPointCommand command = invocation.getArgument(0);
                    return new UserPoint(command.id(), command.amount(), System.currentTimeMillis());
                });

        when(pointService.usePoint(any(UserPointCommand.class)))
                .thenAnswer(invocation -> {
                    UserPointCommand command = invocation.getArgument(0);
                    return new UserPoint(command.id(), -command.amount(), System.currentTimeMillis());
                });

        ExecutorService executorService = Executors.newFixedThreadPool(2);
        CountDownLatch latch = new CountDownLatch(2);
        AtomicBoolean deadlockDetected = new AtomicBoolean(false);

        // 첫 번째 스레드: charge API 실행
        Runnable chargeTask = () -> {
            try {
                MvcResult mvcResult = mockMvc.perform(
                        MockMvcRequestBuilders.patch("/point/{id}/charge", id)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(String.valueOf(chargeAmount))
                ).andReturn();

                assertThat(mvcResult.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                latch.countDown();
            }
        };

        // 두 번째 스레드: use API 실행
        Runnable useTask = () -> {
            try {
                MvcResult mvcResult = mockMvc.perform(
                        MockMvcRequestBuilders.patch("/point/{id}/use", id)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(String.valueOf(useAmount))
                ).andReturn();

                assertThat(mvcResult.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
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
            // 데드락 발생 시 스레드가 무기한 대기하므로 타임아웃 설정
            // 타임아웃 발생 시 데드락으로 간주
            if (!latch.await(3, TimeUnit.SECONDS)) {
                deadlockDetected.set(true);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }

        // then
        assertThat(deadlockDetected.get()).isFalse();
        verify(pointService, times(1)).chargePoint(any(UserPointCommand.class));
        verify(pointService, times(1)).usePoint(any(UserPointCommand.class));
    }

}
