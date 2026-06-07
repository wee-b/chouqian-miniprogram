package com.quickstart.common.domain.winner.vo;

import lombok.Data;

@Data
public class WinnerVO {
    private String userName;
    private String avatar;
    private String prizeName;
    private Integer prizeType;
    private String codeValue;
}
