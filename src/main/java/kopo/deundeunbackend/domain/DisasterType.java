package kopo.deundeunbackend.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DisasterType {

    private String disasterCode;

    private String disasterName;

    private String easyName;

    private String guideText;

    private String icon;

    private Integer sortOrder;
}
