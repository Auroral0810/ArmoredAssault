package com.nau_yyf.model;

import java.util.List;

public class LevelMap {
    private int level;
    private String name;
    private int width;
    private int height;
    private MapPosition playerBase;
    private MapPosition playerSpawn;
    private List<MapElement> elements;
    private List<EnemySpawn> enemies;
    
    // 地图位置类
    public static class MapPosition {
        private int x;
        private int y;
        private int width;
        private int height;
        
        // Getters and setters
        public int getX() { return x; }
        public void setX(int x) { this.x = x; }
        public int getY() { return y; }
        public void setY(int y) { this.y = y; }
        public int getWidth() { return width; }
        public void setWidth(int width) { this.width = width; }
        public int getHeight() { return height; }
        public void setHeight(int height) { this.height = height; }
    }
    
    // 地图元素类
    public static class MapElement {
        private String type; // "brick", "steel", "grass", "water"
        private int x;
        private int y;
        private int width;
        private int height;
        
        // Getters and setters
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public int getX() { return x; }
        public void setX(int x) { this.x = x; }
        public int getY() { return y; }
        public void setY(int y) { this.y = y; }
        public int getWidth() { return width; }
        public void setWidth(int width) { this.width = width; }
        public int getHeight() { return height; }
        public void setHeight(int height) { this.height = height; }
    }
    
    // 敌人生成点类
    public static class EnemySpawn {
        private String type; // "basic", "elite", "boss"
        private MapPosition spawnPoint;
        
        // Getters and setters
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public MapPosition getSpawnPoint() { return spawnPoint; }
        public void setSpawnPoint(MapPosition spawnPoint) { this.spawnPoint = spawnPoint; }
    }
    
    // 类的getters和setters
    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getWidth() { return width; }
    public void setWidth(int width) { this.width = width; }
    public int getHeight() { return height; }
    public void setHeight(int height) { this.height = height; }
    public MapPosition getPlayerBase() { return playerBase; }
    public void setPlayerBase(MapPosition playerBase) { this.playerBase = playerBase; }
    public MapPosition getPlayerSpawn() { return playerSpawn; }
    public void setPlayerSpawn(MapPosition position) {
        this.playerSpawn = position;
    }
    public List<MapElement> getElements() { return elements; }
    public void setElements(List<MapElement> elements) { this.elements = elements; }
    public List<EnemySpawn> getEnemies() { return enemies; }
    public void setEnemies(List<EnemySpawn> enemies) { this.enemies = enemies; }
} 