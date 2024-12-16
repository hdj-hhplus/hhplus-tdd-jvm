package io.hhplus.tdd.point.service;

import io.hhplus.tdd.point.command.UserIdCommand;
import io.hhplus.tdd.point.entity.PointHistory;
import io.hhplus.tdd.point.entity.UserPoint;
import io.hhplus.tdd.point.enumtype.TransactionType;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

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
}
