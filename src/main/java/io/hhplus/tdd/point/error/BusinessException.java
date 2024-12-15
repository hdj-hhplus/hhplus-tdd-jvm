package io.hhplus.tdd.point.error;

import lombok.Getter;

/*
 * 비즈니스 에러 handler
 */
@Getter
public class BusinessException extends RuntimeException {
    private final ErrorCodeEnu errorCode;

    public BusinessException(ErrorCodeEnu errorCode) {
        super(errorCode.getMsg());
        this.errorCode = errorCode;
    }
}
