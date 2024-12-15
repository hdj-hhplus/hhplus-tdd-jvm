package io.hhplus.tdd.point.command;

import io.hhplus.tdd.point.enumtype.PointErrorCode;
import io.hhplus.tdd.point.error.BusinessException;
import jdk.jfr.Description;

@Description("특정 유저 Command")
public record UserIdCommand(
        Long id
) {
    public UserIdCommand {
        // ID 검증
        if (validateValue(id)) {
            throw new BusinessException(PointErrorCode.INVALID_USER_ID);
        }
    }

    private boolean validateValue(Long value) {
        return value == null || value < 1;
    }
}
