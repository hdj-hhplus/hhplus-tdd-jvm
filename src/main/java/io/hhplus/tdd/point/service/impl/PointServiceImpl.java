package io.hhplus.tdd.point.service.impl;

import io.hhplus.tdd.point.command.UserIdCommand;
import io.hhplus.tdd.point.command.UserPointCommand;
import io.hhplus.tdd.point.entity.PointHistory;
import io.hhplus.tdd.point.entity.UserPoint;
import io.hhplus.tdd.point.enumtype.PointErrorCode;
import io.hhplus.tdd.point.enumtype.TransactionType;
import io.hhplus.tdd.point.error.BusinessException;
import io.hhplus.tdd.point.repository.PointHistoryRepository;
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
    private final PointHistoryRepository pointHistoryRepository;

    @Override
    public UserPoint getPoint(UserIdCommand command) {
        // User 데이터가 존재하는지 검사해야 하지만, User 테이블이 없으므로 생략한다.
        return userPointRepository.findById(command.id())
                .orElse(UserPoint.empty(command.id()));
    }

    @Override
    public List<PointHistory> getHistory(UserIdCommand command) {
        // User 데이터가 존재하는지 검사해야 하지만, User 테이블이 없으므로 생략한다.
        return pointHistoryRepository.findAllByUserId(command.id());
    }

    @Override
    public UserPoint chargePoint(UserPointCommand command) {
        // 기존 UserPoint 호출
        UserPoint userPoint = userPointRepository.findById(command.id())
                .orElse(UserPoint.empty(command.id()));

        // 충전 후 포인트
        Long chargedPoint = userPoint.point() + command.amount();

        // 잔고 최대 금액(10,000,000) 검사
        if(chargedPoint > 10000000) {
            throw new BusinessException(PointErrorCode.MAX_BALANCE_EXCEEDED);
        }

        // userPoint 업데이트
        UserPoint updateUserPoint = userPoint.changePoint(chargedPoint);
        UserPoint savedUserPoint = userPointRepository.save(updateUserPoint);

        // 히스토리 삽입
        PointHistory pointHistory = PointHistory.makeEntity(command.id(), command.amount(), TransactionType.CHARGE, savedUserPoint.updateMillis());
        pointHistoryRepository.save(pointHistory);

        return savedUserPoint;
    }

    @Override
    public UserPoint usePoint(UserPointCommand command) {
        // 기존 UserPoint 호출
        UserPoint userPoint = userPointRepository.findById(command.id())
                .orElse(UserPoint.empty(command.id()));

        // 사용 후 포인트
        Long chargedPoint = userPoint.point() - command.amount();

        // 잔고 최대 금액(10,000,000) 검사
        if(chargedPoint < 0) {
            throw new BusinessException(PointErrorCode.INSUFFICIENT_BALANCE);
        }

        // userPoint 업데이트
        UserPoint updateUserPoint = userPoint.changePoint(chargedPoint);
        UserPoint savedUserPoint = userPointRepository.save(updateUserPoint);

        // 히스토리 삽입
        PointHistory pointHistory = PointHistory.makeEntity(command.id(), command.amount(), TransactionType.USE, savedUserPoint.updateMillis());
        pointHistoryRepository.save(pointHistory);

        return savedUserPoint;
    }


}
