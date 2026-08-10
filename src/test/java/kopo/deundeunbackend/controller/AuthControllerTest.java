package kopo.deundeunbackend.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import kopo.deundeunbackend.common.utils.PasswordUtil;
import kopo.deundeunbackend.domain.User;
import kopo.deundeunbackend.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/**
 * 로그인 API 테스트.
 *
 * <p>DB 를 실제로 붙지 않고 {@link UserMapper} 만 대역으로 바꿔서 돌린다.
 * 팀원 누구나 DB 접속 정보 없이 이 테스트를 돌릴 수 있어야 하기 때문이다.
 * 실제 DB 를 사용하는 확인은 {@code TestAccountSetupTest} 가 담당한다.
 */
@SpringBootTest
class AuthControllerTest {

    private static final String TEST_EMAIL = "test@deundeun.com";
    private static final String TEST_PASSWORD = "Test1234!";

    @Autowired
    private WebApplicationContext context;

    @MockitoBean
    private UserMapper userMapper;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();

        String salt = PasswordUtil.newSalt();
        User user = User.builder()
                .userId(1L)
                .email(TEST_EMAIL)
                .pwSalt(salt)
                .pwHash(PasswordUtil.hash(TEST_PASSWORD, salt))
                .userName("테스트계정")
                .build();

        org.mockito.Mockito.when(userMapper.selectByEmail(TEST_EMAIL)).thenReturn(user);
    }

    private String body(String email, String password) {
        return """
                {"email": "%s", "password": "%s"}
                """.formatted(email, password);
    }

    @Test
    @DisplayName("올바른 이메일과 비밀번호면 200 과 회원 정보를 받는다")
    void loginSuccess() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(body(TEST_EMAIL, TEST_PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.userId").value(1))
                .andExpect(jsonPath("$.data.email").value(TEST_EMAIL))
                .andExpect(jsonPath("$.data.userName").value("테스트계정"));
    }

    @Test
    @DisplayName("응답에 비밀번호 해시와 솔트가 절대 포함되지 않는다")
    void responseNeverLeaksCredentials() throws Exception {
        String responseBody = mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(body(TEST_EMAIL, TEST_PASSWORD)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(responseBody).doesNotContain("pwHash", "pwSalt", "pw_hash", "pw_salt");
    }

    @Test
    @DisplayName("비밀번호가 틀리면 401 을 준다")
    void loginFailsWithWrongPassword() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(body(TEST_EMAIL, "Wrong123!")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("LOGIN_FAILED"))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    @DisplayName("가입되지 않은 이메일도 비밀번호 오류와 똑같은 응답을 준다")
    void unknownEmailIsIndistinguishable() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(body("nobody@deundeun.com", TEST_PASSWORD)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("LOGIN_FAILED"))
                .andExpect(jsonPath("$.message").value("이메일 또는 비밀번호가 올바르지 않습니다."));
    }

    @Test
    @DisplayName("이메일이 비어 있으면 400 을 준다")
    void emailRequired() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(body("   ", TEST_PASSWORD)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("EMAIL_REQUIRED"));
    }

    @Test
    @DisplayName("비밀번호가 비어 있으면 400 을 준다")
    void passwordRequired() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(body(TEST_EMAIL, "")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("PASSWORD_REQUIRED"));
    }

    @Test
    @DisplayName("JSON 이 깨져 있으면 400 을 준다")
    void malformedJson() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content("{ not json "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }
}
