package kopo.deundeunbackend.dto.request;

import java.util.List;

/**
 * 재난 시뮬레이션 결과
 *
 * @param disaster    재난 종류 (EARTHQUAKE, FIRE)
 * @param success     제한 시간 안에 올바른 자세를 유지했는지
 * @param elapsedMs   시작부터 성공(또는 시간 초과)까지 걸린 시간
 * @param passedSteps 한 번이라도 해낸 동작 (CROUCH, COVER_HEAD, COVER_MOUTH)
 */
public record SimulationRequest(String disaster, Boolean success, Long elapsedMs,
                                List<String> passedSteps) {

}
