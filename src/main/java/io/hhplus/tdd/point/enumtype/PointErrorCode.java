package io.hhplus.tdd.point.enumtype;


import io.hhplus.tdd.point.error.ErrorCodeEnu;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * 오류 코드 종류
 */
@RequiredArgsConstructor
public enum PointErrorCode implements ErrorCodeEnu {
    INVALID_USER_ID(HttpStatus.BAD_REQUEST, "사용자 ID가 유효하지 않습니다."),
    INVALID_AMOUNT(HttpStatus.BAD_REQUEST, "금액이 유효하지 않습니다.");
    private final HttpStatus status;
    private final String msg;

    @Override
    public HttpStatus getStatus() {
        return status;
    }

    @Override
    public String getMsg() {
        return msg;
    }
}