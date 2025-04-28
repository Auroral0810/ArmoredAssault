package com.nau_yyf.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nau_yyf.model.LevelMap;

import java.io.InputStream;

public class MapLoader {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static LevelMap loadLevel(int level) {
        try {
            String resourcePath = "/maps/level" + level + ".json";
            InputStream is = MapLoader.class.getResourceAsStream(resourcePath);
            if (is == null) {
                throw new IllegalArgumentException("找不到地图文件: " + resourcePath);
            }

            return objectMapper.readValue(is, LevelMap.class);
        } catch (Exception e) {
            
            e.printStackTrace();
            return null;
        }
    }
} 