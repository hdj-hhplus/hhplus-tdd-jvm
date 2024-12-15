package io.hhplus.tdd.point.repository.impl;

import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.entity.UserPoint;
import io.hhplus.tdd.point.repository.UserPointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserPointRepositoryImpl implements UserPointRepository {

    private final UserPointTable userPointTable;

    @Override
    public UserPoint save(UserPoint userPoint) {
        return userPointTable.insertOrUpdate(userPoint.id(), userPoint.point());
    }

    @Override
    public Optional<UserPoint> findById(Long id) {
        return Optional.ofNullable(userPointTable.selectById(id));
    }

}
