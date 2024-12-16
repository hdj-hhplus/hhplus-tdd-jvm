package io.hhplus.tdd.point.service;

import io.hhplus.tdd.point.command.UserIdCommand;
import io.hhplus.tdd.point.entity.UserPoint;
import jdk.jfr.Description;
import org.springframework.validation.annotation.Validated;

@Validated
public interface PointService {

    @Description("특정 유저 포인트 조회")
    UserPoint getPoint(UserIdCommand command);

}
