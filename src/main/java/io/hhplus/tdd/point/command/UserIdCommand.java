package io.hhplus.tdd.point.command;

import io.hhplus.tdd.point.enumtype.PointErrorCode;
import io.hhplus.tdd.point.error.BusinessException;
import jdk.jfr.Description;
import lombok.Getter;

/**
 * 유저 범위 id : 1 ~ 1,000,000,000
 */

@Description("특정 유저 Command")
public record UserIdCommand(
        Long id
) {

    public UserIdCommand {
        // ID 검증
        if (id == null || id < 1 || id > 1000000000) {
            throw new BusinessException(PointErrorCode.INVALID_USER_ID);
        }
    }
}
