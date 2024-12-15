package io.hhplus.tdd.point.command;

import io.hhplus.tdd.point.error.BusinessException;
import jdk.jfr.Description;
import io.hhplus.tdd.point.enumtype.PointErrorCode;

@Description("특정 유저의 포인트를 충전하는 기능 Command")
public record ChargeUserPointCommand(
        Long id,
        Long amount
) {
    public ChargeUserPointCommand {
        // ID 검증
        if (validateValue(id)) {
            throw new BusinessException(PointErrorCode.INVALID_USER_ID);
        }

        // Amount 검증
        if (validateValue(amount)) {
            throw new BusinessException(PointErrorCode.INVALID_AMOUNT);
        }
    }

    private boolean validateValue(Long value) {
        return value == null || value < 1;
    }
}
