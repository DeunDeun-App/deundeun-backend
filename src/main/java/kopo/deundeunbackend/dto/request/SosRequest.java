package kopo.deundeunbackend.dto.request;

import java.math.BigDecimal;

/**
 * SOS 도움 요청. 회원은 로그인 토큰으로 확인한다
 *
 * @param lat    위도. 위치 권한이 없거나 못 받으면 null
 * @param lng     경도. 위치 권한이 없거나 못 받으면 null
 * @param situation 어떤 재난인지 (FIRE 화재, EARTHQUAKE 지진, FLOOD 홍수, UNKNOWN 모름)
 * @param trapped 갇혀서 못 나가는지
 * @param hurt    다쳤는지
 * @param message 보호자에게 보낼 문자 본문
 */
public record SosRequest(BigDecimal lat, BigDecimal lng,
                         String situation, Boolean trapped, Boolean hurt, String message) {

}
