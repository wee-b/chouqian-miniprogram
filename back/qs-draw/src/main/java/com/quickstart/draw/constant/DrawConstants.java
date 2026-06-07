package com.quickstart.draw.constant;

public final class DrawConstants {
    private DrawConstants() {
    }

    public static final int DRAW_STATUS_DRAFT = 0;
    public static final int DRAW_STATUS_RUNNING = 1;
    public static final int DRAW_STATUS_OPENED = 2;
    public static final int DRAW_STATUS_EMPTY = 3;

    public static final int DEFAULT_CODE_COUNT_PER_USER = 5;
    public static final int MAX_DRAW_EXPIRE_DAYS = 1000;
}
