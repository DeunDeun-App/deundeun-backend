package kopo.deundeunbackend.dto.response;

import kopo.deundeunbackend.domain.CallItem;

/** 상담원 질문 하나. 통과 키워드는 내려보내지 않는다. */
public record CallItemResponse(Long itemId, Integer itemOrder, String itemType, String label,
                               String question) {

    public static CallItemResponse from(CallItem item) {
        return new CallItemResponse(item.getItemId(), item.getItemOrder(), item.getItemType(),
                item.getLabel(), item.getQuestion());
    }
}
