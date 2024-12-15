package io.hhplus.tdd.point.command;

import io.hhplus.tdd.point.enumtype.PointErrorCode;
import io.hhplus.tdd.point.error.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class UserPointCommandTest {

    @Test
    @DisplayName("UserPointCommand 생성 실패 - id가 NULL")
    void shouldFailToTestUserPointCommandWithNullId() {
        // given
        final Long id = null;
        final Long amount = 1000L;

        // when
        final BusinessException exception = assertThrows(BusinessException.class, () -> {
            new UserPointCommand(id, amount);
        });

        // then
        assertThat(exception.getErrorCode()).isEqualTo(PointErrorCode.INVALID_USER_ID);
    }
    @Test
    @DisplayName("UserPointCommand 생성 실패 - id가 1보다 작을 때")
    void shouldFailToTestUserPointCommandWhenIdIsLessThanOne() {
        // given
        final Long id = -1L;
        final Long amount = 5000L;

        // when
        final BusinessException exception = assertThrows(BusinessException.class, () -> {
            new UserPointCommand(id, amount);
        });

        // then
        assertThat(exception.getErrorCode()).isEqualTo(PointErrorCode.INVALID_USER_ID);
    }

    @Test
    @DisplayName("UserPointCommand 생성 실패 - amount가 NULL")
    void shouldFailToTestUserPointCommandWithNullAmount() {
        // given
        final Long id = 1L;
        final Long amount = null;

        // when
        final BusinessException exception = assertThrows(BusinessException.class, () -> {
            new UserPointCommand(id, amount);
        });

        // then
        assertThat(exception.getErrorCode()).isEqualTo(PointErrorCode.INVALID_AMOUNT);
    }
    @Test
    @DisplayName("UserPointCommand 생성 실패 - amount가 1보다 작을 때")
    void shouldFailToTestUserPointCommandWhenAmountIsLessThanOne() {
        // given
        final Long id = 1L;
        final Long amount = 0L;

        // when
        final BusinessException exception = assertThrows(BusinessException.class, () -> {
            new UserPointCommand(id, amount);
        });

        // then
        assertThat(exception.getErrorCode()).isEqualTo(PointErrorCode.INVALID_AMOUNT);
    }
}
