package com.quickstart.draw.module.temporaryCode.service;

import com.quickstart.common.domain.temporaryCode.dto.GenerateCodeDTO;
import com.quickstart.common.domain.draw.vo.DrawSmallVO;
import com.quickstart.common.domain.temporaryCode.vo.PassCodeVO;

public interface TemporaryCode {


    void generatePassCode(Long userId, GenerateCodeDTO dto);

    void banPassCode(Long userId, String passCode);

    PassCodeVO queryPassCode(Long userId, Long drawId);

    DrawSmallVO queryDrawByPC(Long userId, String passCode);

}
