package kopo.deundeunbackend.mapper;

import kopo.deundeunbackend.domain.Guardian;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface GuardianMapper {

    Guardian selectPrimary(@Param("userId") Long userId);

    int insertGuardian(Guardian guardian);

    int updateGuardian(Guardian guardian);

    int deleteByUserId(@Param("userId") Long userId);
}
