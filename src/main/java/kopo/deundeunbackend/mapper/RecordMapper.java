package kopo.deundeunbackend.mapper;

import java.time.LocalDateTime;
import java.util.List;
import kopo.deundeunbackend.domain.RecordKeyword;
import kopo.deundeunbackend.domain.RecordSession;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/** 기록 화면 조회. 끝난 신고 연습(CALL119)과 시뮬레이션(POSE)만 본다. */
@Mapper
public interface RecordMapper {

    /**
     * 최근 훈련부터 조회
     *
     * @param trainingType 훈련 종류. null 이면 둘 다
     */
    List<RecordSession> selectSessions(@Param("userId") Long userId,
                                       @Param("trainingType") String trainingType,
                                       @Param("limit") int limit);

    /** {@code from} 이후에 시작한 훈련. 주별 횟수와 이번 주 요일별 훈련 시간을 셀 때 쓴다 */
    List<RecordSession> selectSessionsSince(@Param("userId") Long userId,
                                            @Param("from") LocalDateTime from);

    /**
     * 키워드 조회
     *
     * @param sessionIds 세션 번호 목록. null 이면 회원의 끝난 훈련 전체
     */
    List<RecordKeyword> selectKeywords(@Param("userId") Long userId,
                                       @Param("sessionIds") List<Long> sessionIds);
}
