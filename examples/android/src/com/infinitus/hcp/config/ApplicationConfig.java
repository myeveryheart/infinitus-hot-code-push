package com.infinitus.hcp.config;

import android.content.Context;
import android.content.res.AssetManager;
import android.text.TextUtils;
import android.util.Log;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Created by M on 16/9/9.
 * <p/>
 * 保存hcp.json文件的信息
 */
public class ApplicationConfig {

    private String jsonString;
    private ContentConfig contentConfig;

    private ApplicationConfig() {
    }

    /**
     * 从json读取，创建实例
     *
     * @param jsonString json
     * @return class 实例
     */
    public static ApplicationConfig fromJson(String jsonString) {
        ApplicationConfig config = new ApplicationConfig();
        try {
            JsonNode json = new ObjectMapper().readTree(jsonString);

            config.setContentConfig(ContentConfig.fromJson(json));

            config.jsonString = jsonString;
        } catch (Exception e) {
            Log.d("HCP", "Failed to convert json string into application config" , e);
            config = null;
        }

        return config;
    }

    /**
     * 从资源文件夹读取配置
     *
     * @param context application context
     * @return config
     */
    public static ApplicationConfig configFromAssets(final Context context, final String configFileName) {
        final AssetManager assetManager = context.getResources().getAssets();
        final StringBuilder returnString = new StringBuilder();
        final String configFilePath = "www/" + configFileName;
        BufferedReader reader = null;
        try {
            InputStreamReader isr = new InputStreamReader(assetManager.open(configFilePath));
            reader = new BufferedReader(isr);
            String line;
            while ((line = reader.readLine()) != null) {
                returnString.append(line);
            }
        } catch (Exception e) {
            Log.d("HCP", "Failed to read hcp.json from assets", e);
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (Exception e2) {
                Log.d("HCP", "Failed to clear resources after reading hcp.json from the assets", e2);
            }
        }

        return ApplicationConfig.fromJson(returnString.toString());
    }

    /**
     * 实例转json
     *
     * @return JSON formatted string
     */
    @Override
    public String toString() {
        if (TextUtils.isEmpty(jsonString)) {
            jsonString = generateJson();
        }

        return jsonString;
    }

    /**
     * 获取配置
     *
     * @return content config
     * @see ContentConfig
     */
    public ContentConfig getContentConfig() {
        return contentConfig;
    }

    private void setContentConfig(ContentConfig config) {
        this.contentConfig = config;
    }

    private String generateJson() {
        JsonNodeFactory nodeFactory = JsonNodeFactory.instance;

        ObjectNode json = (ObjectNode) contentConfig.toJson();

        return json.toString();
    }
}
