package kopo.deundeunbackend.mapper;

import kopo.deundeunbackend.domain.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {

    /**
     * 회원 조회
     *
     * @param email 조회할 이메일
     * @return 회원. 없으면 null
     */
    User selectByEmail(@Param("email") String email);

    /**
     * 회원 등록
     *
     * @param user 등록할 회원
     * @return 등록된 행 수
     */
    int insertUser(User user);

    /**
     * 회원 비밀번호 해시, 솔트 업데이트
     *
     * @param email  대상 회원의 이메일
     * @param pwHash 새 해시
     * @param pwSalt 새 솔트
     * @return 변경된 행 수
     */
    int updatePassword(@Param("email") String email,
                       @Param("pwHash") String pwHash,
                       @Param("pwSalt") String pwSalt);

    int updatePasswordByUserId(@Param("userId") Long userId,
                               @Param("pwHash") String pwHash,
                               @Param("pwSalt") String pwSalt);
}
