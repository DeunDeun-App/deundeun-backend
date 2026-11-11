package kopo.deundeunbackend.dto.response;

import java.util.List;

public record CallResultResponse(int score, List<CallItemResultResponse> results) {

}
