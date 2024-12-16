package io.hhplus.tdd.point.repository;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.point.entity.PointHistory;
import io.hhplus.tdd.point.enumtype.TransactionType;
import io.hhplus.tdd.point.repository.impl.PointHistoryRepositoryImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class PointHistoryRepositoryTest {

    @Autowired
    private PointHistoryTable pointHistoryTable;
    @Autowired
    private PointHistoryRepositoryImpl pointHistoryRepository;

    @BeforeEach
    public void init() {
        // 유저 포인트 테이블 초기화 작업
        pointHistoryTable.insert(1, 1000, TransactionType.CHARGE, System.currentTimeMillis());
        pointHistoryTable.insert(1, 500, TransactionType.USE, System.currentTimeMillis());
    }

    @Test
    @DisplayName("PointHistory 삽입 성공")
    void successToInsertPointHistory() {
        // given
        final Long userId = 1L;
        final Long amount = 100L;
        final TransactionType type = TransactionType.USE;
        final PointHistory pointHistory = PointHistory.makeEntity(userId, amount, type, System.currentTimeMillis());

        // when
        PointHistory savedPointHistory = pointHistoryRepository.save(pointHistory);

        // then
        assertThat(savedPointHistory).isNotNull();
        assertThat(savedPointHistory.userId()).isEqualTo(userId);
        assertThat(savedPointHistory.amount()).isEqualTo(amount);
        assertThat(savedPointHistory.type()).isEqualTo(type);
        assertThat(savedPointHistory.updateMillis()).isLessThanOrEqualTo(System.currentTimeMillis());
    }

    @Test
    @DisplayName("특정 유저의 PointHistory 조회 성공")
    void successToFindAllById() {
        // given
        final Long userId = 1L;

        // when
        List<PointHistory> pointHistories = pointHistoryRepository.findAllByUserId(userId);

        // then
        assertThat(pointHistories).isNotNull();
        assertThat(pointHistories).hasSize(2); // 초기화 시 두 개의 기록 삽입
        assertThat(pointHistories).extracting(PointHistory::userId).containsOnly(userId);

        // 각 필드 값 확인
        PointHistory firstHistory = pointHistories.get(0);
        assertThat(firstHistory.amount()).isEqualTo(1000L);
        assertThat(firstHistory.type()).isEqualTo(TransactionType.CHARGE);

        PointHistory secondHistory = pointHistories.get(1);
        assertThat(secondHistory.amount()).isEqualTo(500L);
        assertThat(secondHistory.type()).isEqualTo(TransactionType.USE);
    }


}
