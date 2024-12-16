package io.hhplus.tdd.point.service;

import io.hhplus.tdd.point.command.UserIdCommand;
import io.hhplus.tdd.point.entity.UserPoint;
import io.hhplus.tdd.point.repository.UserPointRepository;
import io.hhplus.tdd.point.service.impl.PointServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PointServiceTest {

    @Mock
    private UserPointRepository userPointRepository;

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

}
