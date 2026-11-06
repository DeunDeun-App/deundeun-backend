package kopo.deundeunbackend.dto.response;

import java.util.List;

public record CallSessionResponse(Long sessionId, String scenarioTitle, String situationText,
                                  List<CallItemResponse> items) {

}
