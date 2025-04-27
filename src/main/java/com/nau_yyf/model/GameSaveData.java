package com.nau_yyf.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameSaveData {
    // 游戏基本信息
    private int currentLevel;
    private String playerTankType;
    private long gameTime;
    private int score;
    private int playerLives;
    private int bulletCount;

    // 玩家坦克信息
    private TankData playerTank;

    // 敌方坦克信息
    private List<TankData> enemyTanks = new ArrayList<>();

    // 子弹信息
    private List<BulletData> bullets = new ArrayList<>();

    // 增益效果信息
    private List<PowerUpData> powerUps = new ArrayList<>();

    // 战斗统计
    private int enemyTanksDestroyed;
    private int totalEnemyTanksToGenerate;
    private int enemyTanksGenerated;

    // 内部类：坦克数据
    public static class TankData {
        private String type;
        private int x;
        private int y;
        private int direction;
        private int health;
        private Map<String, Double> activeEffects = new HashMap<>();

        // 默认构造函数（用于JSON反序列化）
        public TankData() {
        }

        // 从Tank对象创建TankData的构造函数
        public TankData(Tank tank) {
            this.type = tank.getTypeString();
            this.x = tank.getX();
            this.y = tank.getY();
            this.direction = tank.getDirectionValue();
            this.health = tank.getHealth();

            // 复制激活效果
            for (Map.Entry<Tank.PowerUpType, Double> entry : tank.getActiveEffects().entrySet()) {
                activeEffects.put(entry.getKey().getName(), entry.getValue());
            }
        }

        // Getter和Setter方法
        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public int getX() {
            return x;
        }

        public void setX(int x) {
            this.x = x;
        }

        public int getY() {
            return y;
        }

        public void setY(int y) {
            this.y = y;
        }

        public int getDirection() {
            return direction;
        }

        public void setDirection(int direction) {
            this.direction = direction;
        }

        public int getHealth() {
            return health;
        }

        public void setHealth(int health) {
            this.health = health;
        }

        public Map<String, Double> getActiveEffects() {
            return activeEffects;
        }

        public void setActiveEffects(Map<String, Double> activeEffects) {
            this.activeEffects = activeEffects;
        }
    }

    // 内部类：子弹数据
    public static class BulletData {
        private int x;
        private int y;
        private int direction;
        private String bulletType;
        private double speed;
        private int damage;
        private boolean fromPlayer;

        public BulletData() {
        }

        public BulletData(Bullet bullet) {
            this.x = bullet.getX();
            this.y = bullet.getY();
            this.direction = bullet.getDirection().ordinal();
            this.bulletType = bullet.getBulletType();
            this.speed = bullet.getSpeed();
            this.damage = bullet.getDamage();
            this.fromPlayer = bullet.isFromPlayer();
        }

        public String getBulletType() {
            return bulletType;
        }

        public void setBulletType(String bulletType) {
            this.bulletType = bulletType;
        }

        public int getX() {
            return x;
        }

        public void setX(int x) {
            this.x = x;
        }

        public int getY() {
            return y;
        }

        public void setY(int y) {
            this.y = y;
        }

        public int getDirection() {
            return direction;
        }

        public void setDirection(int direction) {
            this.direction = direction;
        }

        public double getSpeed() {
            return speed;
        }

        public void setSpeed(int speed) {
            this.speed = speed;
        }

        public int getDamage() {
            return damage;
        }

        public void setDamage(int damage) {
            this.damage = damage;
        }

        public boolean isFromPlayer() {
            return fromPlayer;
        }

        public void setFromPlayer(boolean fromPlayer) {
            this.fromPlayer = fromPlayer;
        }
    }

    // 内部类：增益效果数据
    public static class PowerUpData {
        private String type;
        private int x;
        private int y;
        private long creationTime;

        public PowerUpData() {
        }

        public PowerUpData(PowerUp powerUp) {
            this.type = powerUp.getType().getName();
            this.x = powerUp.getX();
            this.y = powerUp.getY();
            // 存储剩余时间，加载时需要计算新的创建时间
            this.creationTime = System.currentTimeMillis() -
                    (5000 - powerUp.getRemainingTime()); // 假设总生命周期为5000毫秒
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public int getX() {
            return x;
        }

        public void setX(int x) {
            this.x = x;
        }

        public int getY() {
            return y;
        }

        public void setY(int y) {
            this.y = y;
        }

        public long getCreationTime() {
            return creationTime;
        }

        public void setCreationTime(long creationTime) {
            this.creationTime = creationTime;
        }
    }

    public int getCurrentLevel() {
        return currentLevel;
    }

    public void setCurrentLevel(int currentLevel) {
        this.currentLevel = currentLevel;
    }

    public String getPlayerTankType() {
        return playerTankType;
    }

    public void setPlayerTankType(String playerTankType) {
        this.playerTankType = playerTankType;
    }

    public long getGameTime() {
        return gameTime;
    }

    public void setGameTime(long gameTime) {
        this.gameTime = gameTime;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getPlayerLives() {
        return playerLives;
    }

    public void setPlayerLives(int playerLives) {
        this.playerLives = playerLives;
    }

    public int getBulletCount() {
        return bulletCount;
    }

    public void setBulletCount(int bulletCount) {
        this.bulletCount = bulletCount;
    }

    public TankData getPlayerTank() {
        return playerTank;
    }

    public void setPlayerTank(TankData playerTank) {
        this.playerTank = playerTank;
    }

    public List<TankData> getEnemyTanks() {
        return enemyTanks;
    }

    public void setEnemyTanks(List<TankData> enemyTanks) {
        this.enemyTanks = enemyTanks;
    }

    public List<BulletData> getBullets() {
        return bullets;
    }

    public void setBullets(List<BulletData> bullets) {
        this.bullets = bullets;
    }

    public List<PowerUpData> getPowerUps() {
        return powerUps;
    }

    public void setPowerUps(List<PowerUpData> powerUps) {
        this.powerUps = powerUps;
    }

    public int getEnemyTanksDestroyed() {
        return enemyTanksDestroyed;
    }

    public void setEnemyTanksDestroyed(int enemyTanksDestroyed) {
        this.enemyTanksDestroyed = enemyTanksDestroyed;
    }

    public int getTotalEnemyTanksToGenerate() {
        return totalEnemyTanksToGenerate;
    }

    public void setTotalEnemyTanksToGenerate(int totalEnemyTanksToGenerate) {
        this.totalEnemyTanksToGenerate = totalEnemyTanksToGenerate;
    }

    public int getEnemyTanksGenerated() {
        return enemyTanksGenerated;
    }

    public void setEnemyTanksGenerated(int enemyTanksGenerated) {
        this.enemyTanksGenerated = enemyTanksGenerated;
    }
}