package com.quickstart.draw.module.drawVerify.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.quickstart.common.domain.ErrorCode;
import com.quickstart.common.domain.draw.Draw;
import com.quickstart.common.domain.draw.vo.DrawVerifyCodeVO;
import com.quickstart.common.domain.draw.vo.DrawVerifyVO;
import com.quickstart.common.domain.drawCode.DrawCode;
import com.quickstart.common.domain.winner.Winner;
import com.quickstart.common.exception.BusinessException;
import com.quickstart.draw.constant.DrawConstants;
import com.quickstart.draw.module.draw.mapper.DrawMapper;
import com.quickstart.draw.module.drawCode.mapper.DrawCodeMapper;
import com.quickstart.draw.module.drawCode.mapper.WinnerMapper;
import com.quickstart.draw.module.drawVerify.service.DrawVerifyService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 轻量级 commit-reveal 可验证开奖实现。
 */
@Service
public class DrawVerifyServiceImpl implements DrawVerifyService {

    public static final String VERIFY_ALGORITHM =
            "SHA256(seed + ':' + drawId + ':' + codeValue), sort asc";

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final DrawMapper drawMapper;
    private final DrawCodeMapper drawCodeMapper;
    private final WinnerMapper winnerMapper;

    public DrawVerifyServiceImpl(DrawMapper drawMapper,
                                 DrawCodeMapper drawCodeMapper,
                                 WinnerMapper winnerMapper) {
        this.drawMapper = drawMapper;
        this.drawCodeMapper = drawCodeMapper;
        this.winnerMapper = winnerMapper;
    }

    @Override
    public void initCommitment(Draw draw) {
        if (draw == null) {
            return;
        }
        if (!StringUtils.hasText(draw.getServerSeed())) {
            draw.setServerSeed(generateSeed());
        }
        if (!StringUtils.hasText(draw.getSeedHash())) {
            draw.setSeedHash(sha256(draw.getServerSeed()));
        }
        draw.setVerifyAlgorithm(VERIFY_ALGORITHM);
    }

    @Override
    public void ensureCommitment(Draw draw) {
        if (draw == null) {
            return;
        }
        boolean needUpdate = !StringUtils.hasText(draw.getServerSeed())
                || !StringUtils.hasText(draw.getSeedHash())
                || !StringUtils.hasText(draw.getVerifyAlgorithm());
        initCommitment(draw);
        if (needUpdate) {
            drawMapper.updateById(draw);
        }
    }

    @Override
    public String buildCodesHash(List<DrawCode> codes) {
        List<String> sortedCodes = codes.stream()
                .map(DrawCode::getCodeValue)
                .sorted()
                .toList();
        return sha256(String.join(",", sortedCodes));
    }

    @Override
    public List<DrawCode> sortCodes(Long drawId, String seed, List<DrawCode> codes) {
        return codes.stream()
                .sorted(Comparator
                        .comparing((DrawCode code) -> calculateScore(seed, drawId, code.getCodeValue()))
                        .thenComparing(DrawCode::getCodeValue))
                .toList();
    }

    @Override
    public DrawVerifyVO getVerifyInfo(Long drawId) {
        Draw draw = drawMapper.selectById(drawId);
        if (draw == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "抽签不存在");
        }

        DrawVerifyVO vo = new DrawVerifyVO();
        vo.setDrawId(draw.getDrawId());
        vo.setStatus(draw.getStatus());
        vo.setSeedHash(draw.getSeedHash());
        vo.setCodesHash(draw.getCodesHash());
        vo.setAlgorithm(draw.getVerifyAlgorithm() == null ? VERIFY_ALGORITHM : draw.getVerifyAlgorithm());

        if (draw.getStatus() == null || draw.getStatus() != DrawConstants.DRAW_STATUS_OPENED) {
            vo.setMessage("开奖后公开验证信息");
            vo.setCodes(List.of());
            vo.setWinnerCodes(List.of());
            return vo;
        }

        vo.setSeed(draw.getServerSeed());
        vo.setMessage("可使用公开种子和参与码复算中奖结果");

        LambdaQueryWrapper<DrawCode> codeWrapper = new LambdaQueryWrapper<>();
        codeWrapper.eq(DrawCode::getDrawId, drawId);
        List<DrawCode> codes = drawCodeMapper.selectList(codeWrapper);

        Set<Long> winnerCodeIds = loadWinnerCodeIds(drawId);
        List<DrawVerifyCodeVO> codeVos = new ArrayList<>(codes.size());
        for (DrawCode code : sortCodes(drawId, draw.getServerSeed(), codes)) {
            DrawVerifyCodeVO codeVO = new DrawVerifyCodeVO();
            codeVO.setDrawCodeId(code.getDrawCodeId());
            codeVO.setCodeValue(code.getCodeValue());
            codeVO.setScore(calculateScore(draw.getServerSeed(), drawId, code.getCodeValue()));
            codeVO.setWinner(winnerCodeIds.contains(code.getDrawCodeId()));
            codeVos.add(codeVO);
        }
        vo.setCodes(codeVos);

        List<String> winnerCodes = codes.stream()
                .filter(code -> winnerCodeIds.contains(code.getDrawCodeId()))
                .map(DrawCode::getCodeValue)
                .toList();
        vo.setWinnerCodes(winnerCodes);
        return vo;
    }

    private Set<Long> loadWinnerCodeIds(Long drawId) {
        LambdaQueryWrapper<Winner> winnerWrapper = new LambdaQueryWrapper<>();
        winnerWrapper.eq(Winner::getDrawId, drawId);
        winnerWrapper.isNotNull(Winner::getWinnerCodeId);
        List<Winner> winners = winnerMapper.selectList(winnerWrapper);
        Set<Long> winnerCodeIds = new HashSet<>();
        for (Winner winner : winners) {
            winnerCodeIds.add(winner.getWinnerCodeId());
        }
        return winnerCodeIds;
    }

    private String generateSeed() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }

    private String calculateScore(String seed, Long drawId, String codeValue) {
        return sha256(seed + ":" + drawId + ":" + codeValue);
    }

    private String sha256(String text) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(text.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new IllegalStateException("SHA-256 计算失败", e);
        }
    }
}
