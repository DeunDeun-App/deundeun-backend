package kopo.deundeunbackend.common.utils;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** 비밀번호 해싱 규칙 단위 테스트. DB 없이 돈다. */
class PasswordUtilTest {

    private static final String RAW_PASSWORD = "Test1234!";

    @Test
    @DisplayName("솔트는 DB 컬럼 char(32) 에 맞는 16진수 32글자다")
    void saltFitsColumn() {
        String salt = PasswordUtil.newSalt();

        assertThat(salt).hasSize(32).matches("[0-9a-f]{32}");
    }

    @Test
    @DisplayName("해시는 DB 컬럼 char(64) 에 맞는 16진수 64글자다")
    void hashFitsColumn() {
        String hash = PasswordUtil.hash(RAW_PASSWORD, PasswordUtil.newSalt());

        assertThat(hash).hasSize(64).matches("[0-9a-f]{64}");
    }

    @Test
    @DisplayName("솔트를 매번 새로 만들면 같은 비밀번호라도 해시가 달라진다")
    void samePasswordDifferentSaltProducesDifferentHash() {
        String hash1 = PasswordUtil.hash(RAW_PASSWORD, PasswordUtil.newSalt());
        String hash2 = PasswordUtil.hash(RAW_PASSWORD, PasswordUtil.newSalt());

        assertThat(hash1).isNotEqualTo(hash2);
    }

    @Test
    @DisplayName("맞는 비밀번호는 통과하고 틀린 비밀번호는 걸러진다")
    void matches() {
        String salt = PasswordUtil.newSalt();
        String hash = PasswordUtil.hash(RAW_PASSWORD, salt);

        assertThat(PasswordUtil.matches(RAW_PASSWORD, salt, hash)).isTrue();
        assertThat(PasswordUtil.matches("Wrong123!", salt, hash)).isFalse();
        assertThat(PasswordUtil.matches(RAW_PASSWORD, PasswordUtil.newSalt(), hash)).isFalse();
    }

    @Test
    @DisplayName("null 이 들어와도 예외 없이 false 를 준다")
    void nullSafe() {
        assertThat(PasswordUtil.matches(null, "salt", "hash")).isFalse();
        assertThat(PasswordUtil.matches(RAW_PASSWORD, null, "hash")).isFalse();
        assertThat(PasswordUtil.matches(RAW_PASSWORD, "salt", null)).isFalse();
    }
}
