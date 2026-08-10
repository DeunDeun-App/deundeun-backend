package kopo.deundeunbackend;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import kopo.deundeunbackend.common.exception.ApiException;
import kopo.deundeunbackend.common.utils.PasswordUtil;
import kopo.deundeunbackend.domain.User;
import kopo.deundeunbackend.dto.request.LoginRequest;
import kopo.deundeunbackend.dto.response.LoginResponse;
import kopo.deundeunbackend.mapper.UserMapper;
import kopo.deundeunbackend.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class TestAccountSetupTest {

    private static final String TEST_EMAIL = "test@deundeun.com";

    private static final String TEST_PASSWORD = "Test1234!";

    private static final String TEST_NAME = "테스트계정";
    private static final String TEST_PHONE = "010-0000-0000";

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private AuthService authService;

    @Test
    @DisplayName("테스트 계정을 준비하고 실제 DB 로 로그인까지 확인한다")
    void setUpTestAccountAndLogin() {

        // 1. 계정 준비 (없으면 생성, 있으면 비밀번호 재설정)
        String salt = PasswordUtil.newSalt();
        String hash = PasswordUtil.hash(TEST_PASSWORD, salt);

        User existing = userMapper.selectByEmail(TEST_EMAIL);

        if (existing == null) {
            User newUser = User.builder()
                    .email(TEST_EMAIL)
                    .pwHash(hash)
                    .pwSalt(salt)
                    .userName(TEST_NAME)
                    .userPhone(TEST_PHONE)
                    .build();

            int inserted = userMapper.insertUser(newUser);

            assertThat(inserted).isEqualTo(1);
            assertThat(newUser.getUserId()).as("auto_increment PK 가 채번돼야 한다").isNotNull();

            System.out.println("### 테스트 계정 생성됨. userId=" + newUser.getUserId());

        } else {
            int updated = userMapper.updatePassword(TEST_EMAIL, hash, salt);

            assertThat(updated).isEqualTo(1);

            System.out.println("### 테스트 계정 이미 존재. 비밀번호 재설정. userId="
                    + existing.getUserId());
        }

        // 2. 저장된 내용이 스키마 제약에 맞는지 확인
        User saved = userMapper.selectByEmail(TEST_EMAIL);

        assertThat(saved).isNotNull();
        assertThat(saved.getPwHash()).hasSize(64);
        assertThat(saved.getPwSalt()).hasSize(32);

        // 3. 실제 DB 를 거쳐 로그인 성공
        LoginResponse success = authService.login(new LoginRequest(TEST_EMAIL, TEST_PASSWORD));

        assertThat(success.email()).isEqualTo(TEST_EMAIL);
        assertThat(success.userId()).isNotNull();

        System.out.println("### 로그인 성공 확인. userId=" + success.userId()
                + ", userName=" + success.userName());

        // 4. 틀린 비밀번호는 거부
        assertThatThrownBy(() -> authService.login(new LoginRequest(TEST_EMAIL, "Wrong123!")))
                .isInstanceOf(ApiException.class)
                .hasMessage("이메일 또는 비밀번호가 올바르지 않습니다.");

        System.out.println("### 틀린 비밀번호 차단 확인");
        System.out.println("### 테스트 계정 => " + TEST_EMAIL + " / " + TEST_PASSWORD);
    }
}
