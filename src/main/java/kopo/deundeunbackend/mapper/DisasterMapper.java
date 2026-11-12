package kopo.deundeunbackend.mapper;

import java.util.List;
import kopo.deundeunbackend.domain.DisasterType;
import kopo.deundeunbackend.domain.MessageTranslation;
import kopo.deundeunbackend.domain.Pictogram;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface DisasterMapper {

    List<DisasterType> selectDisasterTypes();

    List<Pictogram> selectActivePictograms();

    MessageTranslation selectTranslationByHash(@Param("msgHash") String msgHash);

    int insertTranslation(MessageTranslation translation);
}
