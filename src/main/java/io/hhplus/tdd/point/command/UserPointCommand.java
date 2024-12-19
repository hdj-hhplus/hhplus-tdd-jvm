package io.hhplus.tdd.point.command;

import io.hhplus.tdd.point.enumtype.PointErrorCode;
import io.hhplus.tdd.point.error.BusinessException;
import jdk.jfr.Description;

/**
 * 유저 범위 id : 1 ~ 1,000,000,000
 * 충전 금액 범위 : 1 ~ 10,000,000 (1천만원)
 * 사용 금액 범위 : 1 ~ 10,000,000 (1천만원)
 * 잔고 금액 범위 : 0 ~ 10,000,000 (1천만원)
 */

@Description("특정 유저의 포인트 Command")
public record UserPointCommand(
        Long id,
        Long amount
) {
    public UserPointCommand {
        // ID 검증
        if (id == null || id < 1 || id > 1000000000) {
            throw new BusinessException(PointErrorCode.INVALID_USER_ID);
        }

        // Amount 검증
        if (id == null || id < 1 || id > 10000000) {
            throw new BusinessException(PointErrorCode.INVALID_AMOUNT);
        }
    }
}
