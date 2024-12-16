package io.hhplus.tdd.point.repository;

import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.entity.UserPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class UserPointRepositoryTest {

    @Autowired
    private UserPointRepository userPointRepository;
    @Autowired
    private UserPointTable userPointTable;

    @BeforeEach
    public void init() {
        // 유저 포인트 테이블 초기화 작업
        userPointTable.insertOrUpdate(1, 1000);
    }

    @Test
    @DisplayName("UserPoint 생성 성공")
    void successToInsertUserPoint() {
        // given
        UserPoint point = UserPoint.empty(2);

        // when
        UserPoint savedUserPoint = userPointRepository.save(point);

        // then
        assertThat(savedUserPoint).isNotNull();
        assertThat(savedUserPoint.id()).isEqualTo(2);
        assertThat(savedUserPoint.point()).isZero();
    }

    @Test
    @DisplayName("UserPoint 수정 성공")
    void successToUpdateUserPoint() {
        // given
        Long id = 1L;
        Long point = 5000L;
        Long updateMillis = System.currentTimeMillis();
        UserPoint userPoint = new UserPoint(id, point, updateMillis);

        // when
        UserPoint savedUserPoint = userPointRepository.save(userPoint);

        // then
        assertThat(savedUserPoint).isNotNull();
        assertThat(savedUserPoint.id()).isEqualTo(id);
        assertThat(savedUserPoint.point()).isEqualTo(point);
        assertThat(savedUserPoint.updateMillis()).isLessThanOrEqualTo(System.currentTimeMillis());
    }

}
