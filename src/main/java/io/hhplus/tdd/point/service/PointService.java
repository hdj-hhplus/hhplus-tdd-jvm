package io.hhplus.tdd.point.service;

import io.hhplus.tdd.point.command.UserIdCommand;
import io.hhplus.tdd.point.entity.PointHistory;
import io.hhplus.tdd.point.entity.UserPoint;
import jdk.jfr.Description;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
public interface PointService {

    @Description("특정 유저 포인트 조회")
    UserPoint getPoint(UserIdCommand command);

    @Description("특정 유저 포인트 충전/이용 내역 조회")
    List<PointHistory> getHistory(UserIdCommand command);

}
