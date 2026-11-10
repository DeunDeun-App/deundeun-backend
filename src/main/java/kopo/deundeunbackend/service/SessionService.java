package kopo.deundeunbackend.service;

import java.time.Duration;
import java.util.UUID;
import kopo.deundeunbackend.common.exception.ApiException;
import kopo.deundeunbackend.domain.AuthToken;
import kopo.deundeunbackend.mapper.AuthTokenMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SessionService {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final int SESSION_VALID_MINUTES = (int) Duration.ofDays(30).toMinutes();

    private final AuthTokenMapper authTokenMapper;

    public String issue(Long userId) {
        AuthToken token = AuthToken.builder()
                .userId(userId)
                .tokenType(AuthToken.TYPE_LOGIN)
                .tokenValue(UUID.randomUUID().toString())
                .build();

        authTokenMapper.insertToken(token, SESSION_VALID_MINUTES);

        return token.getTokenValue();
    }

    public Long requireUserId(String authorization) {
        String tokenValue = authorization != null && authorization.startsWith(BEARER_PREFIX)
                ? authorization.substring(BEARER_PREFIX.length()).trim()
                : null;

        AuthToken token = tokenValue == null || tokenValue.isEmpty() ? null
                : authTokenMapper.selectUsableToken(tokenValue, AuthToken.TYPE_LOGIN);

        if (token == null) {
            throw ApiException.unauthorized("SESSION_EXPIRED", "로그인이 필요합니다. 다시 로그인해 주세요.");
        }

        authTokenMapper.extendExpiry(token.getTokenId(), SESSION_VALID_MINUTES);

        return token.getUserId();
    }
}
