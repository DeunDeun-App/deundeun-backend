package kopo.deundeunbackend.dto.response;

import java.util.List;

public record SimulationResultResponse(Long sessionId, int score, List<KeywordResponse> keywords) {

}
