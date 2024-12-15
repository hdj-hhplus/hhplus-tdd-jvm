package io.hhplus.tdd.point.error;

import org.springframework.http.HttpStatus;

public interface ErrorCodeEnu {
    HttpStatus getStatus();
    String getMsg();
}
