package io.hhplus.tdd.point.entity;

import io.hhplus.tdd.point.enumtype.TransactionType;

public record PointHistory(
        Long id,
        Long userId,
        Long amount,
        TransactionType type,
        Long updateMillis
) {
    public static PointHistory makeEntity(long userId,
                                          long amount,
                                          TransactionType type,
                                          long updateMillis) {
        return new PointHistory(null, userId, amount, type, updateMillis);
    }
}
