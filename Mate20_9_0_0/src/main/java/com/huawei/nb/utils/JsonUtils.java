package com.huawei.nb.utils;

import android.text.TextUtils;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.huawei.nb.utils.logger.DSLog;

public class JsonUtils {
    public static <T> T parse(String jsonContent, Class<T> beanClass) {
        T t = null;
        if (TextUtils.isEmpty(jsonContent) || beanClass == null) {
            DSLog.e("Failed to parse json file, error: invalid parameter.", new Object[0]);
            return t;
        }
        try {
            return new Gson().fromJson(jsonContent, (Class) beanClass);
        } catch (JsonSyntaxException e) {
            DSLog.e("Failed to parse json file, error: %s.", e.getMessage());
            return t;
        }
    }

    public static JsonElement parse(String json) {
        JsonElement jsonElement = null;
        if (TextUtils.isEmpty(json)) {
            DSLog.e("Failed to parse json string, error: Json is empty.", new Object[0]);
            return jsonElement;
        }
        try {
            return new JsonParser().parse(json);
        } catch (JsonSyntaxException e) {
            return jsonElement;
        }
    }

    public static boolean isValidJson(String content) {
        if (TextUtils.isEmpty(content)) {
            DSLog.e("Failed to parse json string, error: Json is empty.", new Object[0]);
            return false;
        }
        try {
            new JsonParser().parse(content).getAsJsonObject();
            return true;
        } catch (JsonSyntaxException e) {
            return false;
        } catch (IllegalStateException e2) {
            return false;
        }
    }

    public static boolean isValidJsonArray(String data) {
        if (TextUtils.isEmpty(data)) {
            DSLog.e("Failed to parse json string, error: JsonArray is empty.", new Object[0]);
            return false;
        }
        try {
            if (new JsonParser().parse(data).getAsJsonArray().size() >= 0) {
                return true;
            }
            return false;
        } catch (JsonSyntaxException e) {
            return false;
        } catch (IllegalStateException e2) {
            return false;
        }
    }
}
