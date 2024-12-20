package io.hhplus.tdd.point.command;

import io.hhplus.tdd.point.enumtype.PointErrorCode;
import io.hhplus.tdd.point.error.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class UserIdCommandTest {

    @Test
    @DisplayName("UserIdCommand 생성 실패 - id가 NULL")
    void shouldFailToTestUserIdCommandWithNullId() {
        // given
        final Long id = null;

        // when
        final BusinessException exception =
                assertThrows(BusinessException.class, () -> {
                    new UserIdCommand(id);
                });

        // then
        assertThat(exception.getErrorCode()).isEqualTo(PointErrorCode.INVALID_USER_ID);
    }

    @Test
    @DisplayName("UserIdCommand 생성 실패 - id가 1보다 작을 때")
    void shouldFailToTestUserIdCommandWhenIdIsLessThanOne() {
        // given
        final Long id = -1L;

        // when
        final BusinessException exception =
                assertThrows(BusinessException.class, () -> {
                    new UserIdCommand(id);
                });

        // then
        assertThat(exception.getErrorCode()).isEqualTo(PointErrorCode.INVALID_USER_ID);
    }

    @Test
    @DisplayName("UserIdCommand 생성 실패 - id가 1000000000L보다 클 때")
    void shouldFailToTestUserIdCommandWhenIdIsMoreThanMax() {
        // given
        final Long id = 2000000000L;

        // when
        final BusinessException exception =
                assertThrows(BusinessException.class, () -> {
                    new UserIdCommand(id);
                });

        // then
        assertThat(exception.getErrorCode()).isEqualTo(PointErrorCode.INVALID_USER_ID);
    }

}
