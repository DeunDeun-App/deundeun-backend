package kopo.deundeunbackend.mapper;

import kopo.deundeunbackend.domain.AuthToken;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AuthTokenMapper {

    int insertToken(@Param("token") AuthToken token, @Param("validMinutes") int validMinutes);

    AuthToken selectUsableToken(@Param("tokenValue") String tokenValue,
                                @Param("tokenType") String tokenType);

    int markUsed(@Param("tokenId") Long tokenId);
}
