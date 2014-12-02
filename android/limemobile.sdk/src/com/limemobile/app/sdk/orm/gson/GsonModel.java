package com.limemobile.app.sdk.orm.gson;

import java.sql.Timestamp;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

public class GsonModel<T> {
    protected final Class<T> mClazz;
    protected final Gson mGson;

    public GsonModel(Class<T> clazz) {
        super();
        mClazz = clazz;

        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Timestamp.class,
                new TimestampDeserializer());
        mGson = gsonBuilder.create();
    }

    public T parseObject(JSONObject json) {
        return parseObject(json.toString());
    }

    public T parseObject(String json) {
        return mGson.fromJson(json, mClazz);
    }

    public List<T> parseObjects(JSONObject json) {
        return parseObjects(json.toString());
    }

    public List<T> parseObjects(JSONArray json) {
        return parseObjects(json.toString());
    }

    public List<T> parseObjects(String json) {
        return mGson.fromJson(json, new TypeToken<List<T>>() {
        }.getType());
    }
}
