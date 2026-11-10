package kopo.deundeunbackend.dto.response;

import kopo.deundeunbackend.domain.DisasterMessage;

public record DisasterMessageResponse(String id, String region, String text, String step, String sentAt) {

    public static DisasterMessageResponse from(DisasterMessage message) {
        return new DisasterMessageResponse(message.hash(), message.region(), message.text(), message.step(),
                message.createdAt());
    }
}
