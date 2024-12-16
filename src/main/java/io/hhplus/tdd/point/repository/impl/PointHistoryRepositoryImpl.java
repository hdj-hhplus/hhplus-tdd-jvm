package io.hhplus.tdd.point.repository.impl;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.point.entity.PointHistory;
import io.hhplus.tdd.point.repository.PointHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class PointHistoryRepositoryImpl implements PointHistoryRepository {

    private final PointHistoryTable pointHistoryTable;

    @Override
    public PointHistory save(PointHistory userPoint) {
        return pointHistoryTable.insert(userPoint.userId(), userPoint.amount(), userPoint.type(), userPoint.updateMillis());
    }

    @Override
    public List<PointHistory> findAllById(Long id) {
        return pointHistoryTable.selectAllByUserId(id);
    }

}
