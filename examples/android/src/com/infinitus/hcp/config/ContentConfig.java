package com.infinitus.hcp.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import com.infinitus.hcp.model.UpdateTime;

/**
 * Created by M on 16/9/9.
 * <p/>
 * hcp.json文件对应的配置
 */
public class ContentConfig {

    // JSON keys to parse hcp.json
    private static class JsonKeys {
        public static final String VERSION = "release";
        public static final String MINIMUM_NATIVE_VERSION = "min_native_interface";
        public static final String UPDATE = "update";
        public static final String CONTENT_URL = "content_url";
    }

    /**
     * 从JSON node实例化
     *
     * @param node hcp.json文件的JSON node
     * @return 实例
     * @see JsonNode
     */
    static ContentConfig fromJson(JsonNode node) {
        ContentConfig config = new ContentConfig();
        try {
            config.setReleaseVersion(node.get(JsonKeys.VERSION).asText());
            config.setContentUrl(node.get(JsonKeys.CONTENT_URL).asText());

            // minimum native version
            if (node.has(JsonKeys.MINIMUM_NATIVE_VERSION)) {
                config.setMinimumNativeVersion(node.get(JsonKeys.MINIMUM_NATIVE_VERSION).asInt());
            } else {
                config.setMinimumNativeVersion(0);
            }

            // when to perform update
            if (node.has(JsonKeys.UPDATE)) {
                config.setUpdateTime(UpdateTime.fromString(node.get(JsonKeys.UPDATE).asText()));
            } else {
                config.setUpdateTime(UpdateTime.SILENT);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return config;
    }

    private String releaseVersion;
    private int minimumNativeVersion;
    private String contentUrl;
    private UpdateTime updateTime;
    private JsonNode jsonNode;

    private ContentConfig() {
    }

    /**
     * 获取www版本
     *
     * @return www版本
     */
    public String getReleaseVersion() {
        return releaseVersion;
    }

    /**
     * 要求的最低的app版本
     *
     * @return 获取最低的app版本
     */
    public int getMinimumNativeVersion() {
        return minimumNativeVersion;
    }

    /**
     * 获取资源文件在web端的url
     *
     * @return 资源文件在web端的url
     */
    public String getContentUrl() {
        return contentUrl;
    }

    /**
     * 获取更新类型
     *
     * @return 更新类型
     * @see UpdateTime
     */
    public UpdateTime getUpdateTime() {
        return updateTime;
    }

    JsonNode toJson() {
        if (jsonNode == null) {
            jsonNode = generateJson();
        }

        return jsonNode;
    }


    private void setReleaseVersion(String releaseVersion) {
        this.releaseVersion = releaseVersion;
    }

    private void setMinimumNativeVersion(int minimumNativeVersion) {
        this.minimumNativeVersion = minimumNativeVersion;
    }

    private void setContentUrl(String contentUrl) {
        this.contentUrl = contentUrl;
    }

    private void setUpdateTime(UpdateTime updateTime) {
        this.updateTime = updateTime;
    }

    private JsonNode generateJson() {
        JsonNodeFactory nodeFactory = JsonNodeFactory.instance;

        ObjectNode node = nodeFactory.objectNode();
        node.set(JsonKeys.CONTENT_URL, nodeFactory.textNode(contentUrl));
        node.set(JsonKeys.MINIMUM_NATIVE_VERSION, nodeFactory.numberNode(minimumNativeVersion));
        node.set(JsonKeys.VERSION, nodeFactory.textNode(releaseVersion));
        node.set(JsonKeys.UPDATE, nodeFactory.textNode(updateTime.toString()));

        return node;
    }

}
