package io.hhplus.tdd.point.repository;

import io.hhplus.tdd.point.entity.UserPoint;

import java.util.Optional;

public interface UserPointRepository {

    UserPoint save(UserPoint userPoint);
    Optional<UserPoint> findById(Long id);

}
