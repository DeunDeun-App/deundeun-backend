package kopo.deundeunbackend.dto.response;

public record CallItemResultResponse(Long itemId, String label, String statLabel,
                                     boolean passed) {

}
