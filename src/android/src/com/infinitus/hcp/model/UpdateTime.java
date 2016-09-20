package com.infinitus.hcp.model;

/**
 * Created by M on 16/9/9.
 * <p/>
 * 枚举更新类型
 */
public enum UpdateTime {
    /**
     * 未定义
     */
    UNDEFINED(""),

    /**
     * 强制更新
     */
    FORCED("forced"),

    /**
     * 静默更新
     */
    SILENT("silent");

    private String value;

    UpdateTime(String value) {
        this.value = value;
    }

    /**
     * string转enum
     *
     * @param value string value
     * @return enum value
     */
    public static UpdateTime fromString(String value) {
        if ("forced".equals(value)) {
            return FORCED;
        } else if ("silent".equals(value)) {
            return SILENT;
        }

        return UNDEFINED;
    }

    @Override
    public String toString() {
        return value;
    }
}
