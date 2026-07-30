package com.quickstart.draw.constant;

public class RedisConstant {

    public static final String PassCodePrefix = "client:draw:passcode";

    // com.quickstart.draw.module.draw.service.impl;
    private static final String OFFICIAL_DRAW_CACHE_KEY = "cache:officialDraws";
    private static final String CAFFEINE_NAME = "officialDraws";

    /** 每人参与次数上限：draw:partLimit:{drawId} → partLimit 值 */
    public static final String PART_LIMIT_PREFIX = "draw:partLimit";

    /** 每人已参与次数：draw:partCount:{drawId} → hash { userId: count } */
    public static final String PART_COUNT_PREFIX = "draw:partCount";

    /** 抽签码生成分布式锁 */
    public static final String CODE_GENERATOR_LOCK = "draw:lock:codeGenerator";
}
