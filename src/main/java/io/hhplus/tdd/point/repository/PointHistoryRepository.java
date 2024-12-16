package io.hhplus.tdd.point.repository;

import io.hhplus.tdd.point.entity.PointHistory;

import java.util.List;

public interface PointHistoryRepository {

    PointHistory save(PointHistory userPoint);
    List<PointHistory> findAllByUserId(Long id);

}
