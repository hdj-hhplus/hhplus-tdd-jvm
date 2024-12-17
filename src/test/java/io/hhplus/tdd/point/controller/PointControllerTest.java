package io.hhplus.tdd.point.controller;

import io.hhplus.tdd.point.command.UserIdCommand;
import io.hhplus.tdd.point.command.UserPointCommand;
import io.hhplus.tdd.point.enumtype.PointErrorCode;
import io.hhplus.tdd.point.error.BusinessException;
import io.hhplus.tdd.point.service.PointService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(PointController.class)
class PointControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PointService pointService;

    @Test
    @DisplayName("포인트 조회 실패 - 유효하지 않은 사용자 ID로 조회 시 400 반환")
    void shouldReturn400WhenInvalidUserId() throws Exception {
        // given
        final long id = -1L; // 비정상적인 ID

        // when
        MvcResult mvcResult = mockMvc.perform(
                MockMvcRequestBuilders.get("/point/{id}", id)
                        .contentType("application/json")
        ).andReturn();

        // then
        assertThat(mvcResult.getResponse().getStatus())
                .isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(mvcResult.getResponse().getContentAsString())
                .contains(PointErrorCode.INVALID_USER_ID.getMsg());
    }

    @Test
    @DisplayName("포인트 충전 실패 - 최대 잔고 초과 시 400 반환")
    void shouldReturn400WhenMaxBalanceExceeded() throws Exception {
        // given
        final long id = 1L; // 정상적인 사용자 ID
        final long amount = 1000001L; // 최대 금액을 초과하는 충전 금액

        // 서비스가 MAX_BALANCE_EXCEEDED 예외를 던지도록 설정
        doThrow(new BusinessException(PointErrorCode.MAX_BALANCE_EXCEEDED))
                .when(pointService).chargePoint(any(UserPointCommand.class));

        // when
        MvcResult mvcResult = mockMvc.perform(
                MockMvcRequestBuilders.patch("/point/{id}/charge", id)
                        .contentType("application/json")
                        .content(String.valueOf(amount)) // amount를 body에 포함
        ).andReturn();

        // then
        // 상태 코드 검증
        assertThat(mvcResult.getResponse().getStatus())
                .isEqualTo(HttpStatus.BAD_REQUEST.value());
        // 예외 메시지 검증
        assertThat(mvcResult.getResponse().getContentAsString())
                .contains(PointErrorCode.MAX_BALANCE_EXCEEDED.getMsg());
    }

}
