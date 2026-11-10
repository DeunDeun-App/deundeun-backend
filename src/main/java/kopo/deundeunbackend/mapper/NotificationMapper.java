package kopo.deundeunbackend.mapper;

import java.util.List;
import kopo.deundeunbackend.domain.Notification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface NotificationMapper {

    List<Notification> selectRecent(@Param("userId") Long userId, @Param("limit") int limit);
}
