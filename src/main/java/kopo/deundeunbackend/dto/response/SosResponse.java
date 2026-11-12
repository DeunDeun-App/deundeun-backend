package kopo.deundeunbackend.dto.response;

import java.util.List;

/**
 * SOS 저장 결과
 *
 * @param sosId  SOS 번호. 문자앱으로 넘긴 뒤 발송을 알릴 때 쓴다
 * @param phones 문자를 받을 보호자 전화번호 (우선순위 순)
 */
public record SosResponse(Long sosId, List<String> phones) {

}
