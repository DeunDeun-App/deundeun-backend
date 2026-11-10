package kopo.deundeunbackend.mapper;

import kopo.deundeunbackend.domain.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {

    User selectByEmail(@Param("email") String email);

    User selectByLoginId(@Param("loginId") String loginId);

    User selectByUserId(@Param("userId") Long userId);

    int insertUser(User user);

    int updatePasswordByUserId(@Param("userId") Long userId,
                               @Param("pwHash") String pwHash,
                               @Param("pwSalt") String pwSalt);

    int updateProfile(User user);

    int updateSettings(@Param("userId") Long userId,
                       @Param("shareLocation") boolean shareLocation,
                       @Param("shareResult") boolean shareResult,
                       @Param("sosAlert") boolean sosAlert);

    int deleteUser(@Param("userId") Long userId);
}
