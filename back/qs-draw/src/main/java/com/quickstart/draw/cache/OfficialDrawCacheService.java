package com.quickstart.draw.cache;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.quickstart.common.domain.draw.Draw;
import com.quickstart.common.domain.draw.vo.DrawSmallVO;
import com.quickstart.draw.constant.DrawConstants;
import com.quickstart.draw.module.draw.mapper.DrawMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 官方抽签列表三级缓存编排层。
 * 【职责】
 * 编排 L1 Caffeine → L2 Redis → L3 MySQL 的读取 / 回填 / 失效，
 * Caffeine 配置（cache name、cache key）集中为私有常量，调用方只调 get() / evict()。
 * 【与 DrawRedisService 的关系】
 * DrawRedisService 是 Redis 操作的统一出口（管 key / 序列化 / TTL）；
 * 本类是缓存策略的组合者，L2 读写委托给 DrawRedisService，L1 Caffeine 与 L3 MySQL 自管。
 * DrawServiceImpl 只依赖本类，不再感知 CacheManager / Cache。
 * 【风格】
 * 对齐 DrawRedisService：构造器注入、私有常量、语义化方法、参数精简。
 */
@Slf4j
@Service
public class OfficialDrawCacheService {

    private final CacheManager cacheManager;
    private final DrawMapper drawMapper;
    private final DrawRedisService drawRedisService;

    // ===== Caffeine 配置集中（不再散落到业务类）=====
    private static final String CACHE_NAME = "officialDraws";
    private static final String CACHE_KEY = "cache:officialDraws";

    public OfficialDrawCacheService(CacheManager cacheManager, DrawMapper drawMapper, DrawRedisService drawRedisService) {
        this.cacheManager = cacheManager;
        this.drawMapper = drawMapper;
        this.drawRedisService = drawRedisService;
    }

    /**
     * 获取官方抽签列表：L1 Caffeine 命中直接返回；
     * 否则 L2 Redis，命中回填 L1；否则 L3 MySQL，回填 L2 + L1。
     */
    public List<DrawSmallVO> get() {
        // L1: Caffeine 本地缓存
        List<DrawSmallVO> l1 = getFromCaffeine();
        if (l1 != null) {
            return l1;
        }

        // L2: Redis 分布式缓存（key / 序列化 / TTL 由 DrawRedisService 管理）
        List<DrawSmallVO> l2 = drawRedisService.getOfficialDrawCache();
        if (l2 != null) {
            putCaffeine(l2);
            return l2;
        }

        // L3: MySQL 回源
        List<DrawSmallVO> data = loadFromDb();
        // 回填 L2 + L1
        drawRedisService.setOfficialDrawCache(data);
        putCaffeine(data);
        return data;
    }

    /**
     * 失效官方抽签缓存（L1 + L2）。
     * 创建 / 修改 / 删除 / 发布抽签后调用，保证读到最新数据。
     */
    public void evict() {
        drawRedisService.evictOfficialDrawCache();
        Cache caffeine = caffeineCache();
        if (caffeine != null) {
            caffeine.evict(CACHE_KEY);
        }
    }

    // ==================== 内部方法 ====================

    @SuppressWarnings("unchecked")
    private List<DrawSmallVO> getFromCaffeine() {
        Cache cache = caffeineCache();
        if (cache == null) {
            return null;
        }
        Cache.ValueWrapper wrapper = cache.get(CACHE_KEY);
        return wrapper == null ? null : (List<DrawSmallVO>) wrapper.get();
    }

    private void putCaffeine(List<DrawSmallVO> list) {
        Cache cache = caffeineCache();
        if (cache != null) {
            cache.put(CACHE_KEY, list);
        }
    }

    private Cache caffeineCache() {
        return cacheManager.getCache(CACHE_NAME);
    }

    /**
     * L3 回源：查官方抽签（发布者=0、进行中、未删除），按创建时间倒序，Entity → VO。
     */
    private List<DrawSmallVO> loadFromDb() {
        LambdaQueryWrapper<Draw> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Draw::getPublisherUserId, 0);
        queryWrapper.eq(Draw::getStatus, DrawConstants.DRAW_STATUS_RUNNING);
        queryWrapper.eq(Draw::getDeletedFlag, 0);
        queryWrapper.orderByDesc(Draw::getCreateTime);

        return drawMapper.selectList(queryWrapper).stream().map(one -> {
            DrawSmallVO vo = new DrawSmallVO();
            BeanUtils.copyProperties(one, vo);
            return vo;
        }).toList();
    }
}
