package kopo.deundeunbackend.mapper;

import java.util.List;
import kopo.deundeunbackend.domain.PoseStep;
import kopo.deundeunbackend.domain.SessionResult;
import kopo.deundeunbackend.domain.TrainingSession;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SimulationMapper {

    /**
     * @return 시나리오 번호. 없으면 null
     */
    Long selectScenarioIdByTitle(@Param("poseTitle") String poseTitle);

    List<PoseStep> selectStepsByScenarioId(@Param("poseScenarioId") Long poseScenarioId);

    /** 이미 끝난 훈련 1회를 저장. 시작 시각은 걸린 시간만큼 앞으로 잡는다 */
    int insertSession(@Param("session") TrainingSession session, @Param("elapsedSec") long elapsedSec);

    int insertResult(SessionResult result);
}
