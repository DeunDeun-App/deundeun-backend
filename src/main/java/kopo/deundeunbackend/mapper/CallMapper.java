package kopo.deundeunbackend.mapper;

import java.util.List;
import kopo.deundeunbackend.domain.CallItem;
import kopo.deundeunbackend.domain.CallScenario;
import kopo.deundeunbackend.domain.SessionResult;
import kopo.deundeunbackend.domain.TrainingSession;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/** 119 신고 연습 시나리오, 세션, 항목별 결과. */
@Mapper
public interface CallMapper {

    boolean existsUser(@Param("userId") Long userId);

    /**
     * 질문이 하나 이상 있는 시나리오 중 하나를 무작위로 조회
     *
     * @return 시나리오. 없으면 null
     */
    CallScenario selectRandomScenario();

    List<CallItem> selectItemsByScenarioId(@Param("callScenarioId") Long callScenarioId);

    CallItem selectItem(@Param("itemId") Long itemId);

    int insertSession(TrainingSession session);

    TrainingSession selectSession(@Param("sessionId") Long sessionId);

    /**
     * 진행 중인 세션을 종료 처리
     *
     * @return 변경된 행 수. 이미 끝난 세션이면 0
     */
    int endSession(@Param("sessionId") Long sessionId, @Param("score") int score);

    SessionResult selectResult(@Param("sessionId") Long sessionId, @Param("callItemId") Long callItemId);

    List<SessionResult> selectResultsBySessionId(@Param("sessionId") Long sessionId);

    int insertResult(SessionResult result);

    int updateResult(SessionResult result);
}
