package io.hhplus.tdd.point.service.impl;

import io.hhplus.tdd.point.service.PointService;
import org.springframework.validation.annotation.Validated;

@Validated
public class PointServiceImpl implements PointService {

    /**
     * 유저 범위 id : 1 ~ 1,000,000,000
     * 충전 금액 범위 : 1 ~ 10,000,000 (1천만원)
     * 사용 금액 범위 : 1 ~ 10,000,000 (1천만원)
     * 잔고 금액 범위 : 0 ~ 10,000,000 (1천만원)
     */

}
