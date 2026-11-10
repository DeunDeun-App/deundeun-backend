ALTER TABLE users
    ADD COLUMN login_id VARCHAR(20) NULL COMMENT '로그인 아이디' AFTER user_id;

UPDATE users
   SET login_id = CONCAT('user', user_id)
 WHERE login_id IS NULL;

ALTER TABLE users
    MODIFY COLUMN login_id VARCHAR(20) NOT NULL COMMENT '로그인 아이디',
    MODIFY COLUMN email VARCHAR(100) NOT NULL COMMENT '이메일',
    ADD UNIQUE KEY uk_users_login_id (login_id);

ALTER TABLE auth_tokens
    MODIFY COLUMN token_type ENUM('EMAIL_VERIFY', 'PASSWORD_RESET', 'LOGIN') NOT NULL COMMENT '토큰종류';
