package io.hhplus.tdd.point.controller;

import io.hhplus.tdd.point.command.UserIdCommand;
import io.hhplus.tdd.point.command.UserPointCommand;
import io.hhplus.tdd.point.entity.PointHistory;
import io.hhplus.tdd.point.entity.UserPoint;
import io.hhplus.tdd.point.enumtype.PointErrorCode;
import io.hhplus.tdd.point.enumtype.TransactionType;
import io.hhplus.tdd.point.error.BusinessException;
import io.hhplus.tdd.point.service.PointService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@AutoConfigureMockMvc
class PointIntegrationTest {
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

        doThrow(new BusinessException(PointErrorCode.MAX_BALANCE_EXCEEDED))
                .when(pointService).chargePoint(any(UserPointCommand.class));

        // when
        MvcResult mvcResult = mockMvc.perform(
                MockMvcRequestBuilders.patch("/point/{id}/charge", id)
                        .contentType("application/json")
                        .content(String.valueOf(amount)) // amount를 body에 포함
        ).andReturn();

        // then
        assertThat(mvcResult.getResponse().getStatus())
                .isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(mvcResult.getResponse().getContentAsString())
                .contains(PointErrorCode.MAX_BALANCE_EXCEEDED.getMsg());
    }

    @Test
    @DisplayName("포인트 충전 성공 - 정상적으로 충전 시 변경된 UserPoint와 200 반환")
    void shouldReturn200WhenChargePointSuccessfully() throws Exception {
        // given
        final long id = 1L; // 정상적인 사용자 ID
        final long amount = 1000000L; // 정상적인 충전 금액

        UserPointCommand command = new UserPointCommand(id, amount);
        when(pointService.chargePoint(command))
                .thenReturn(new UserPoint(id, amount, System.currentTimeMillis()));

        // when
        MvcResult mvcResult = mockMvc.perform(
                MockMvcRequestBuilders.patch("/point/{id}/charge", id)
                        .contentType("application/json")
                        .content(String.valueOf(amount))
        ).andReturn();

        // then
        assertThat(mvcResult.getResponse().getStatus())
                .isEqualTo(HttpStatus.OK.value());
        assertThat(mvcResult.getResponse().getContentAsString())
                .contains("\"id\":" + id)
                .contains("\"point\":" + amount);
    }

    @Test
    @DisplayName("포인트 사용 성공 - 정상적으로 사용 시 변경된 UserPoint와 200 반환")
    void shouldReturn200WhenUsePointSuccessfully() throws Exception {
        // given
        final long id = 1L; // 정상적인 사용자 ID
        final long amount = 1000000L; // 정상적인 사용 금액
        final long changedPoint = 0L; // 정상적인 잔고 금액

        UserPointCommand command = new UserPointCommand(id, amount);
        when(pointService.usePoint(command))
                .thenReturn(new UserPoint(id, changedPoint, System.currentTimeMillis()));

        // when
        MvcResult mvcResult = mockMvc.perform(
                MockMvcRequestBuilders.patch("/point/{id}/use", id)
                        .contentType("application/json")
                        .content(String.valueOf(amount))
        ).andReturn();

        // then
        assertThat(mvcResult.getResponse().getStatus())
                .isEqualTo(HttpStatus.OK.value());
        assertThat(mvcResult.getResponse().getContentAsString())
                .contains("\"id\":" + id)
                .contains("\"point\":" + changedPoint);
    }

    @Test
    @DisplayName("포인트 히스토리 조회 성공 - 정상적으로 사용 시 사용/충전 히스토리 반환")
    void shouldReturn200WhenGetHistorySuccessfully() throws Exception {
        // given
        final long id = 1L; // 정상적인 사용자 ID
        final long amount1 = 1000;  // 금액1
        final long amount2 = 200;  // 금액2
        final TransactionType type1 = TransactionType.CHARGE;  // 타입1
        final TransactionType type2 = TransactionType.USE;  // 타입2

        final List<PointHistory> mockHistoryList = List.of(
                PointHistory.makeEntity(id, amount1, type1, System.currentTimeMillis()),
                PointHistory.makeEntity(id, amount2, type2, System.currentTimeMillis())
        );

        when(pointService.getHistory(any(UserIdCommand.class))).thenReturn(mockHistoryList);

        // when
        MvcResult mvcResult = mockMvc.perform(
                MockMvcRequestBuilders.get("/point/{id}/histories", id)
                        .contentType("application/json")
        ).andReturn();

        // then
        assertThat(mvcResult.getResponse().getStatus())
                .isEqualTo(HttpStatus.OK.value());
        assertThat(mvcResult.getResponse().getContentAsString())
                .contains("\"amount\":1000")
                .contains("\"amount\":200")
                .contains("\"type\":\"CHARGE\"")
                .contains("\"type\":\"USE\"")
                .contains("\"userId\":" + id);
    }


}
