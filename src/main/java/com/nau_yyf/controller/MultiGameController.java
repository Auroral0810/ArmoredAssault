package com.nau_yyf.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.nau_yyf.model.*;
import com.nau_yyf.util.MapLoader;
import com.nau_yyf.view.GameView;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * 多人游戏控制器
 * 负责管理双人游戏模式下的游戏逻辑
 */
public class MultiGameController implements GameController {
    // 游戏视图引用
    private GameView gameView;

    // 两个玩家的坦克
    private Tank player1Tank;
    private Tank player2Tank;

    // 当前关卡
    private int currentLevel = 1;

    // 地图
    private LevelMap map;

    // 敌方坦克列表
    private List<Tank> enemyTanks = new ArrayList<>();

    // 子弹列表
    private List<Bullet> bullets = new ArrayList<>();

    // 增益道具列表
    private List<PowerUp> powerUps = new ArrayList<>();

    // 被摧毁的敌人数量
    private int defeatedEnemiesCount = 0;

    // 关卡目标
    private int totalEnemyTarget = 10;

    // 游戏事件监听器
    private GameEventListener eventListener;

    // 增益效果图片缓存
    private Map<String, Image> powerUpImages = new HashMap<>();

    // 玩家坦克类型
    private String player1TankType = "MEDIUM";
    private String player2TankType = "LIGHT";

    // 游戏是否通关
    private boolean levelCompleted = false;

    /**
     * 构造函数
     */
    public MultiGameController() {
        // 初始化资源
        loadImages();
    }

    /**
     * 加载图片资源
     */
    private void loadImages() {
        // 加载增益效果图片
        for (Tank.PowerUpType type : Tank.PowerUpType.values()) {
            try {
                String imagePath = "/images/powerups/" + type.getName().toLowerCase() + ".png";
                Image img = new Image(getClass().getResourceAsStream(imagePath));
                powerUpImages.put(type.getName(), img);
            } catch (Exception e) {
                System.err.println("无法加载增益图片: " + type.getName());
            }
        }
    }

    /**
     * 设置游戏视图引用
     */
    @Override
    public void setGameView(GameView gameView) {
        this.gameView = gameView;
    }

    /**
     * 加载关卡
     */
    @Override
    public void loadLevel(int level) {
        this.currentLevel = level;

        // 根据关卡调整目标数量
        this.totalEnemyTarget = 10 + (level * 2);

        // 重置击败敌人计数
        this.defeatedEnemiesCount = 0;

        // 重置关卡完成状态
        this.levelCompleted = false;

        // 清空现有敌人和子弹
        this.enemyTanks.clear();
        this.bullets.clear();
        this.powerUps.clear();

        // 创建敌方坦克
        createEnemyTanks();

        // 创建玩家坦克
        createPlayerTanks();
    }

    /**
     * 创建敌方坦克
     */
    private void createEnemyTanks() {
        // 根据关卡创建适当数量的敌方坦克
        int enemyCount = Math.min(5, 3 + currentLevel);

        for (int i = 0; i < enemyCount; i++) {
            int x = 100 + (i * 150) % 600;
            int y = 50 + (i / 4) * 100;
            Tank enemyTank = new Tank(Tank.TankType.ELITE, x, y);
            enemyTanks.add(enemyTank);
        }
    }

    /**
     * 创建玩家坦克
     */
    private void createPlayerTanks() {
        // 创建玩家1坦克
        if (player1Tank == null) {
            player1Tank = new Tank(Tank.TankType.fromString(player1TankType), 100, 500);
        }

        // 创建玩家2坦克
        if (player2Tank == null) {
            player2Tank = new Tank(Tank.TankType.fromString(player2TankType), 700, 500);
        }
    }

    /**
     * 获取当前关卡
     */
    @Override
    public int getCurrentLevel() {
        return currentLevel;
    }

    /**
     * 渲染游戏场景
     */
    @Override
    public void renderMap(GraphicsContext gc) {
        // 清空画布
        double width = gc.getCanvas().getWidth();
        double height = gc.getCanvas().getHeight();
        gc.clearRect(0, 0, width, height);

        // 绘制背景
        gc.setFill(javafx.scene.paint.Color.rgb(10, 30, 50));
        gc.fillRect(0, 0, width, height);

        // 绘制边界
        gc.setStroke(javafx.scene.paint.Color.CORNFLOWERBLUE);
        gc.strokeRect(5, 5, width - 10, height - 10);

        // 绘制背景网格线
        gc.setStroke(javafx.scene.paint.Color.rgb(30, 50, 70));
        double gridSize = 40;
        for (double x = gridSize; x < width; x += gridSize) {
            gc.strokeLine(x, 0, x, height);
        }
        for (double y = gridSize; y < height; y += gridSize) {
            gc.strokeLine(0, y, width, y);
        }

        // 绘制敌方坦克
        for (Tank tank : enemyTanks) {
            tank.render(gc);
        }

        // 绘制子弹
        for (Bullet bullet : bullets) {
            bullet.render(gc);
        }

        // 绘制增益道具
        for (PowerUp powerUp : powerUps) {
            powerUp.render(gc);
        }

        // 绘制玩家坦克
        if (player1Tank != null) {
            player1Tank.render(gc);
        }

        if (player2Tank != null) {
            player2Tank.render(gc);
        }
    }

    /**
     * 判断关卡是否完成
     */
    @Override
    public boolean isLevelCompleted() {
        return levelCompleted || defeatedEnemiesCount >= totalEnemyTarget;
    }

    /**
     * 保存游戏状态
     */
    @Override
    public boolean saveGame(String saveName) {
        // TODO: 实现游戏存档逻辑
        return false;
    }

    /**
     * 获取玩家1坦克
     */
    public Tank getPlayer1Tank() {
        return player1Tank;
    }

    /**
     * 获取玩家2坦克
     */
    public Tank getPlayer2Tank() {
        return player2Tank;
    }

    /**
     * 设置玩家1坦克
     */
    public void setPlayer1Tank(Tank tank) {
        this.player1Tank = tank;
    }

    /**
     * 设置玩家2坦克
     */
    public void setPlayer2Tank(Tank tank) {
        this.player2Tank = tank;
    }

    /**
     * 玩家1放置炸弹
     */
    public void placeP1Bomb() {
        // 实现玩家1放置炸弹的逻辑
        System.out.println("玩家1放置炸弹");
    }

    /**
     * 玩家2放置炸弹
     */
    public void placeP2Bomb() {
        // 实现玩家2放置炸弹的逻辑
        System.out.println("玩家2放置炸弹");
    }

    /**
     * 更新玩家坦克状态
     */
    public void updatePlayerTanks() {
        if (player1Tank != null && player1Tank.getHealth() <= 0 && eventListener != null) {
            eventListener.onPlayer1Destroyed();
        }

        if (player2Tank != null && player2Tank.getHealth() <= 0 && eventListener != null) {
            eventListener.onPlayer2Destroyed();
        }

        if (player1Tank != null && player2Tank != null
                && player1Tank.getHealth() <= 0 && player2Tank.getHealth() <= 0
                && eventListener != null) {
            eventListener.onBothPlayersDestroyed();
        }
    }

    /**
     * 获取增益道具列表
     */
    public List<PowerUp> getPowerUps() {
        return powerUps;
    }

    /**
     * 更新增益道具
     */
    public void updatePowerUps(double deltaTime) {
        // 更新所有增益效果
        List<PowerUp> powerUpsToRemove = new ArrayList<>();

        for (PowerUp powerUp : powerUps) {
            updatePowerUp(powerUp, deltaTime);

            // 检查玩家1是否获得增益
            if (player1Tank != null && isColliding(powerUp, player1Tank)) {
                player1Tank.applyPowerUp(powerUp.getType());
                powerUpsToRemove.add(powerUp);
            }

            // 检查玩家2是否获得增益
            if (player2Tank != null && isColliding(powerUp, player2Tank)) {
                player2Tank.applyPowerUp(powerUp.getType());
                powerUpsToRemove.add(powerUp);
            }

            // 检查增益效果是否过期
            if (isPowerUpExpired(powerUp)) {
                powerUpsToRemove.add(powerUp);
            }
        }

        // 移除已使用或过期的增益
        powerUps.removeAll(powerUpsToRemove);

        // 随机生成新的增益
        if (Math.random() < 0.005) { // 0.5%几率生成增益
            generateRandomPowerUp();
        }
    }

    /**
     * 更新单个增益道具
     * 临时方法，用于替代PowerUp.update
     */
    private void updatePowerUp(PowerUp powerUp, double deltaTime) {
        // 检查是否应该闪烁
        if (powerUp.shouldBlink()) {
            if (System.currentTimeMillis() % 400 < 200) {
                powerUp.toggleBlinking();
            }
        }
    }

    /**
     * 检查增益道具是否过期
     * 临时方法，用于替代PowerUp.isExpired
     */
    private boolean isPowerUpExpired(PowerUp powerUp) {
        return powerUp.shouldRemove();
    }

    /**
     * 检查两个游戏对象是否碰撞
     * 临时方法，用于替代isCollidingWith
     */
    private boolean isColliding(PowerUp powerUp, Tank tank) {
        return powerUp.collidesWithTank(tank);
    }

    /**
     * 生成随机增益
     */
    private void generateRandomPowerUp() {
        Tank.PowerUpType[] types = Tank.PowerUpType.values();
        Tank.PowerUpType randomType = types[(int)(Math.random() * types.length)];

        // 随机位置
        int x = (int)(Math.random() * (gameView.getGameCanvas().getWidth() - 30));
        int y = (int)(Math.random() * (gameView.getGameCanvas().getHeight() - 30));
        PowerUp powerUp = new PowerUp(x, y, randomType);

        powerUps.add(powerUp);
    }

    /**
     * 复活玩家1坦克
     */
    public Tank respawnPlayer1Tank(String tankType, int spawnX, int spawnY) {
        // TODO: 实现玩家1坦克复活逻辑
        return player1Tank;
    }

    /**
     * 复活玩家2坦克
     */
    public Tank respawnPlayer2Tank(String tankType, int spawnX, int spawnY) {
        // TODO: 实现玩家2坦克复活逻辑
        return player2Tank;
    }

    /**
     * 寻找玩家1有效的重生位置
     */
    public LevelMap.MapPosition findValidSpawnPositionForPlayer1() {
        // TODO: 实现寻找玩家1重生位置逻辑
        return new LevelMap.MapPosition();
    }

    /**
     * 寻找玩家2有效的重生位置
     */
    public LevelMap.MapPosition findValidSpawnPositionForPlayer2() {
        // TODO: 实现寻找玩家2重生位置逻辑
        return new LevelMap.MapPosition();
    }

    /**
     * 设置玩家1坦克类型
     */
    public void setPlayer1TankType(String tankType) {
        this.player1TankType = tankType;
        if (player1Tank != null) {
            // 使用现有位置创建新坦克
            int x = player1Tank.getX();
            int y = player1Tank.getY();
            player1Tank = new Tank(Tank.TankType.fromString(tankType), x, y);
        } else {
            player1Tank = new Tank(Tank.TankType.fromString(tankType), 100, 500);
        }
    }

    /**
     * 设置玩家2坦克类型
     */
    public void setPlayer2TankType(String tankType) {
        this.player2TankType = tankType;
        if (player2Tank != null) {
            // 使用现有位置创建新坦克
            int x = player2Tank.getX();
            int y = player2Tank.getY();
            player2Tank = new Tank(Tank.TankType.fromString(tankType), x, y);
        } else {
            player2Tank = new Tank(Tank.TankType.fromString(tankType), 700, 500);
        }
    }

    /**
     * 获取被摧毁的敌人数量
     */
    public int getDefeatedEnemiesCount() {
        return defeatedEnemiesCount;
    }

    /**
     * 获取关卡目标
     */
    public int getTotalEnemyTarget() {
        return totalEnemyTarget;
    }

    /**
     * 获取增益道具图片
     */
    public Image getPowerUpImage(String type) {
        return powerUpImages.get(type);
    }

    /**
     * 游戏事件监听器接口
     */
    public interface GameEventListener {
        /**
         * 当玩家1坦克被摧毁时调用
         */
        void onPlayer1Destroyed();

        /**
         * 当玩家2坦克被摧毁时调用
         */
        void onPlayer2Destroyed();

        /**
         * 当两个玩家都被摧毁时调用
         */
        void onBothPlayersDestroyed();
    }

    /**
     * 设置游戏事件监听器
     */
    public void setGameEventListener(GameEventListener listener) {
        this.eventListener = listener;
    }
}