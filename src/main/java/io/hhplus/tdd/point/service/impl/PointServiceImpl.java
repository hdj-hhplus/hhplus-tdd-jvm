package io.hhplus.tdd.point.service.impl;

import io.hhplus.tdd.point.command.UserIdCommand;
import io.hhplus.tdd.point.entity.PointHistory;
import io.hhplus.tdd.point.entity.UserPoint;
import io.hhplus.tdd.point.enumtype.PointErrorCode;
import io.hhplus.tdd.point.error.BusinessException;
import io.hhplus.tdd.point.repository.UserPointRepository;
import io.hhplus.tdd.point.service.PointService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PointServiceImpl implements PointService {

    /**
     * 유저 범위 id : 1 ~ 1,000,000,000
     * 충전 금액 범위 : 1 ~ 10,000,000 (1천만원)
     * 사용 금액 범위 : 1 ~ 10,000,000 (1천만원)
     * 잔고 금액 범위 : 0 ~ 10,000,000 (1천만원)
     */

    private final UserPointRepository userPointRepository;

    @Override
    public UserPoint getPoint(UserIdCommand command) {
        // User 데이터가 존재하는지 검사해야 하지만, User 테이블이 없으므로 생략한다.
        return userPointRepository.findById(command.id())
                .orElse(UserPoint.empty(command.id()));
    }

    @Override
    public List<PointHistory> getHistory(UserIdCommand command) {
        return null;
    }


}
