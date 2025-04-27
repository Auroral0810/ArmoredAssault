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

public class GameController {
    // 定义事件监听器接口
    public interface GameEventListener {
        void onPlayerDestroyed();
    }

    // 添加eventListener变量
    private GameEventListener eventListener;

    // 添加设置事件监听器的方法
    public void setGameEventListener(GameEventListener listener) {
        this.eventListener = listener;
    }

    private int currentLevel = 1;
    private LevelMap levelMap;
    private Tank playerTank;
    private List<Tank> enemyTanks = new ArrayList<>();
    private final Map<String, Image> elementImages = new HashMap<>();

    public Map<String, Image[]> getTankImages() {
        return tankImages;
    }

    private final Map<String, Image[]> tankImages = new HashMap<>();
    private List<Bullet> bullets = new ArrayList<>();
    private boolean[][] grid; // 地图的网格表示

    // 添加炮弹对象池
    private List<Bullet> bulletPool = new ArrayList<>();
    private static final int INITIAL_POOL_SIZE = 50;

    // 添加成员变量跟踪坦克是否在水池中
    private boolean playerInWater = false;

    // 添加新的成员变量来跟踪水池状态
    private boolean inWaterLastFrame = false;

    // 添加水池冷却时间相关变量
    private long lastWaterDamageTime = 0;
    private static final long WATER_DAMAGE_COOLDOWN = 2000; // 2秒冷却时间

    // 添加以下成员变量
    private int totalEnemyTanksToGenerate; // 当前关卡需要生成的坦克总数
    private int maxConcurrentEnemies; // 场上最多同时存在的敌方坦克数
    private int enemyTanksGenerated; // 已经生成的坦克数量
    private int enemyTanksDestroyed; // 已经摧毁的坦克数量
    private List<Long> tankRespawnTimes = new ArrayList<>(); // 坦克重生时间列表
    private List<Tank.TankType> enemyTypesToGenerate = new ArrayList<>(); // 待生成的坦克类型

    // 修改子弹渲染部分，预加载所有子弹图像
    private final Map<String, Image> bulletImages = new HashMap<>();

    // 新增成员变量
    private List<PowerUp> powerUps = new ArrayList<>();
    private Map<String, Image> powerUpImages = new HashMap<>();
    private long lastPowerUpSpawnTime = 0;
    private Bomb activeBomb = null; // 当前激活的炸弹

    private GameView gameView; // 你需要有这个引用，或者用事件回调


    public GameController() {
        // 预加载地图元素图片
        loadElementImages();

        // 预加载坦克图片
        loadTankImages();

        // 预加载子弹图片
        loadBulletImages();

        // 加载增益效果图片
        loadPowerUpImages();
    }

    private void loadElementImages() {
        try {
            elementImages.put("brick", new Image(getClass().getResourceAsStream("/images/map/brick.png")));
            elementImages.put("steel", new Image(getClass().getResourceAsStream("/images/map/steel.png")));
            elementImages.put("grass", new Image(getClass().getResourceAsStream("/images/map/grass.png")));
            elementImages.put("water", new Image(getClass().getResourceAsStream("/images/map/water.png")));
            elementImages.put("base", new Image(getClass().getResourceAsStream("/images/map/base.png")));
        } catch (Exception e) {
        }
    }

    private void loadTankImages() {
        try {
            // 加载玩家坦克图片 - 每个方向一张图片
            for (String type : new String[]{"light", "standard", "heavy"}) {
                Image[] dirImages = new Image[4];
                for (int i = 0; i < 4; i++) {
                    String path = "/images/tanks/friendly/" + type + "/" + i + ".png";
                    dirImages[i] = new Image(getClass().getResourceAsStream(path));
                }
                tankImages.put("player_" + type, dirImages);
            }

            // 加载敌方坦克图片
            Image[] basicTank = new Image[4];
            Image[] eliteTank = new Image[4];
            Image[] bossTank = new Image[4];

            for (int i = 0; i < 4; i++) {
                String basicPath = "/images/tanks/enemy/basic/" + i + ".png";
                String elitePath = "/images/tanks/enemy/elite/" + i + ".png";
                String bossPath = "/images/tanks/enemy/boss/" + i + ".png";

                basicTank[i] = new Image(getClass().getResourceAsStream(basicPath));

                eliteTank[i] = new Image(getClass().getResourceAsStream(elitePath));

                bossTank[i] = new Image(getClass().getResourceAsStream(bossPath));
            }

            tankImages.put("enemy_basic", basicTank);
            tankImages.put("enemy_elite", eliteTank);
            tankImages.put("enemy_boss", bossTank);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadBulletImages() {
        try {
            bulletImages.put("player_bullet", new Image(getClass().getResourceAsStream("/images/bullets/player_bullet.png")));
            bulletImages.put("enemy_basic_bullet", new Image(getClass().getResourceAsStream("/images/bullets/enemy_basic_bullet.png")));
            bulletImages.put("enemy_elite_bullet", new Image(getClass().getResourceAsStream("/images/bullets/enemy_elite_bullet.png")));
            bulletImages.put("enemy_boss_bullet", new Image(getClass().getResourceAsStream("/images/bullets/enemy_boss_bullet.png")));

            // 添加默认子弹作为后备
            bulletImages.put("default_bullet", new Image(getClass().getResourceAsStream("/images/bullets/player_bullet.png")));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadLevel(int level) {
        this.currentLevel = level;
        this.levelMap = MapLoader.loadLevel(level);

        // 设置当前关卡敌方坦克参数
        configureEnemyTanksForLevel(level);

        // 清空敌方坦克和重生计时器
        enemyTanks.clear();
        tankRespawnTimes.clear();
        enemyTanksGenerated = 0;
        enemyTanksDestroyed = 0;

        // 加载初始敌方坦克
        if (levelMap != null && levelMap.getEnemies() != null) {
            for (LevelMap.EnemySpawn enemySpawn : levelMap.getEnemies()) {
                // 确保不超过最大同时存在数量
                if (enemyTanks.size() >= maxConcurrentEnemies) break;

                LevelMap.MapPosition pos = enemySpawn.getSpawnPoint();
                if (pos != null) {
                    // 将字符串转换为TankType枚举
                    Tank.TankType type = Tank.TankType.valueOf(enemySpawn.getType().toUpperCase());

                    // 验证敌人出生点是否有效
                    if (!isPositionValid(pos.getX(), pos.getY(), 40, 40)) {
                        // 寻找有效的替代位置
                        LevelMap.MapPosition validPos = findValidPosition();
                        if (validPos != null) {
                            Tank enemyTank = new Tank(type, validPos.getX(), validPos.getY());
                            enemyTanks.add(enemyTank);
                            enemyTanksGenerated++;
                        }
                    } else {
                        Tank enemyTank = new Tank(type, pos.getX(), pos.getY());
                        enemyTanks.add(enemyTank);
                        enemyTanksGenerated++;
                    }
                }
            }

            // 将剩余需要生成的坦克类型添加到队列中
            for (int i = enemyTanksGenerated; i < totalEnemyTanksToGenerate; i++) {
                // 随机选择坦克类型，保持基础类型多一些
                double random = Math.random();
                if (random < 0.7) {
                    enemyTypesToGenerate.add(Tank.TankType.BASIC);
                } else if (random < 0.9) {
                    enemyTypesToGenerate.add(Tank.TankType.ELITE);
                } else {
                    enemyTypesToGenerate.add(Tank.TankType.BOSS);
                }
            }
        }

        // 初始化网格
        initializeGrid();

        // 创建玩家坦克（如果地图加载成功）
        if (levelMap != null && levelMap.getPlayerSpawn() != null) {
            LevelMap.MapPosition playerPos = levelMap.getPlayerSpawn();

            // 验证玩家出生点是否有效
            if (!isPositionValid(playerPos.getX(), playerPos.getY(), 40, 40)) {

                // 寻找有效的替代位置
                LevelMap.MapPosition validPos = findValidPosition();
                if (validPos != null) {
                    // 更新地图中的玩家出生点
                    levelMap.setPlayerSpawn(validPos);
                    playerPos = validPos;
                }
            }

            if (playerTank == null) {
                // 默认使用STANDARD坦克
                playerTank = new Tank(Tank.TankType.STANDARD, playerPos.getX(), playerPos.getY());
            } else {
                // 只更新位置
                playerTank.setX(playerPos.getX());
                playerTank.setY(playerPos.getY());
            }
        }

        // 如果初始敌方坦克数量小于最大同时存在数，立即生成更多坦克
        while (enemyTanks.size() < maxConcurrentEnemies && enemyTanksGenerated < totalEnemyTanksToGenerate) {
            spawnNewEnemyTank();
        }
    }

    private void initializeGrid() {
        if (levelMap == null) return;

        int gridWidth = levelMap.getWidth() / 40;
        int gridHeight = levelMap.getHeight() / 40;
        grid = new boolean[gridHeight][gridWidth];

        // 默认所有格子都是可行走的
        for (int y = 0; y < gridHeight; y++) {
            for (int x = 0; x < gridWidth; x++) {
                grid[y][x] = false;
            }
        }

        // 标记不可行走的格子 (障碍物)
        for (LevelMap.MapElement element : levelMap.getElements()) {
            String type = element.getType();

            // 水和钢墙不可通过
            if (type.equals("water") || type.equals("steel")) {
                int gridX = element.getX() / 40;
                int gridY = element.getY() / 40;

                // 确保坐标在范围内
                if (gridX >= 0 && gridX < gridWidth && gridY >= 0 && gridY < gridHeight) {
                    grid[gridY][gridX] = true;
                }
            }
        }

    }

    public void setPlayerTankType(String tankType) {
        // 将字符串转换为TankType枚举
        Tank.TankType type = Tank.TankType.valueOf(tankType.toUpperCase());

        // 完全创建一个新坦克，不保留任何旧状态
        int x = 380, y = 480; // 默认位置
        if (levelMap != null && levelMap.getPlayerSpawn() != null) {
            x = levelMap.getPlayerSpawn().getX();
            y = levelMap.getPlayerSpawn().getY();
        }

        // 创建全新的坦克对象
        playerTank = new Tank(type, x, y);

        // 确保坦克面向上方向（这是默认值，但为了清晰，我们显式设置）
        playerTank.setDirection(Tank.Direction.UP);

    }

    public void renderMap(GraphicsContext gc) {
        if (levelMap == null) return;

        // 渲染所有地图元素
        for (LevelMap.MapElement element : levelMap.getElements()) {
            Image img = elementImages.get(element.getType());
            if (img != null) {
                gc.drawImage(img, element.getX(), element.getY(), element.getWidth(), element.getHeight());
            }
        }

        // 渲染敌方坦克
        for (Tank enemy : enemyTanks) {
            if (enemy != null) {
                String imageKey = "enemy_" + enemy.getType().name().toLowerCase();
                Image[] tankImgs = tankImages.get(imageKey);

                // 修正方向索引，确保不会越界
                int dirIndex = Math.min(enemy.getDirection().ordinal(), 3);

                if (tankImgs != null && tankImgs.length > dirIndex && tankImgs[dirIndex] != null) {
                    gc.drawImage(tankImgs[dirIndex], enemy.getX(), enemy.getY(), 40, 40);
                } else {
                    // 如果找不到图像，使用红色方块代替
                    gc.setFill(Color.RED);
                    gc.fillRect(enemy.getX(), enemy.getY(), 40, 40);

                    // 仅打印一次错误，避免日志过多
                    if (Math.random() < 0.01) { // 1%概率打印日志
                        System.err.println("未找到敌方坦克图片: " + imageKey + ", 方向: " + dirIndex);
                    }
                }
            }
        }

        // 渲染玩家坦克
        if (playerTank != null) {
            String imageKey = "player_" + playerTank.getType().name().toLowerCase();
            Image[] tankImgs = tankImages.get(imageKey);
            if (tankImgs != null && playerTank.getDirection().ordinal() < tankImgs.length) {
                gc.drawImage(tankImgs[playerTank.getDirection().ordinal()], playerTank.getX(), playerTank.getY(), 40, 40);
            } else {
                System.err.println("未找到玩家坦克图片: " + imageKey + ", 方向: " + playerTank.getDirection());
                gc.setFill(Color.GREEN);
                gc.fillRect(playerTank.getX(), playerTank.getY(), 40, 40);
            }
        }

        // 渲染子弹
        for (Bullet bullet : bullets) {
            try {
                String bulletType = bullet.getBulletType();
                Image bulletImage = bulletImages.get(bulletType);

                // 如果找不到对应类型的子弹图像，使用默认子弹图像
                if (bulletImage == null) {
                    bulletImage = bulletImages.get("default_bullet");

                    // 只在1%的概率下打印日志，避免过多输出
                    if (Math.random() < 0.01) {
                        
                    }
                }

                // 渲染子弹图像
                if (bulletImage != null) {
                    gc.drawImage(bulletImage, bullet.getX(), bullet.getY(), bullet.getWidth(), bullet.getHeight());
                } else {
                    // 如果所有图像都找不到，使用简单的形状
                    if (bullet.isFromPlayer()) {
                        gc.setFill(Color.YELLOW); // 玩家子弹为黄色
                    } else {
                        gc.setFill(Color.RED); // 敌方子弹为红色
                    }
                    gc.fillOval(bullet.getX(), bullet.getY(), bullet.getWidth(), bullet.getHeight());
                }
            } catch (Exception e) {
                // 捕获任何渲染错误，使用简单的替代显示
                if (bullet.isFromPlayer()) {
                    gc.setFill(Color.YELLOW);
                } else {
                    gc.setFill(Color.RED);
                }
                gc.fillOval(bullet.getX(), bullet.getY(), bullet.getWidth(), bullet.getHeight());
            }
        }

        // 优化增益效果渲染，使用缓存
        for (PowerUp powerUp : powerUps) {
            // 如果处于闪烁状态且当前不可见，则跳过渲染
            if (powerUp.shouldBlink() && !powerUp.isVisible()) {
                continue;
            }

            String imageName = powerUp.getType().getName();
            Image img = powerUpImages.get(imageName);

            if (img != null) {
                // 避免图片加载错误造成的卡顿
                gc.drawImage(img, powerUp.getX(), powerUp.getY(), powerUp.getWidth(), powerUp.getHeight());
            } else {
                // 如果找不到图像，显示紫色方块作为替代
                gc.setFill(Color.PURPLE);
                gc.fillRect(powerUp.getX(), powerUp.getY(), powerUp.getWidth(), powerUp.getHeight());
            }
        }

        // 渲染放置的炸弹
        if (activeBomb != null) {
            Image bombImg = powerUpImages.get("bomb_placed");
            if (bombImg != null) {
                gc.drawImage(bombImg, activeBomb.getX(), activeBomb.getY(), 30, 30);
            } else {
                // 如果找不到图像，显示红色方块作为替代
                gc.setFill(Color.RED);
                gc.fillRect(activeBomb.getX(), activeBomb.getY(), 30, 30);
            }
        }
    }

    public LevelMap getLevelMap() {
        return levelMap;
    }

    public int getCurrentLevel() {
        return currentLevel;
    }

    public Tank getPlayerTank() {
        return playerTank;
    }

    /**
     * 获取剩余敌人数量
     */
    public int getRemainingEnemies() {
        return enemyTanks.size(); // 直接返回当前敌方坦克列表的大小
    }

    public void addBullet(Bullet bullet) {
        bullets.add(bullet);
    }

    public void updateBullets() {
        Iterator<Bullet> iterator = bullets.iterator();
        while (iterator.hasNext()) {
            Bullet bullet = iterator.next();
            bullet.move();

            // 检查是否出界
            if (bullet.isOutOfBounds(levelMap.getWidth(), levelMap.getHeight())) {
                iterator.remove();
                continue;
            }

            // 这里可以添加碰撞检测等逻辑
        }
    }

    public int getBulletCount() {
        return bullets.size();
    }

    public boolean[][] getGrid() {
        return grid;
    }

    /**
     * 更新敌方坦克状态，包括生成新坦克
     */
    public void updateEnemyTanks() {
        // 创建所有需要的临时列表，避免并发修改
        List<Tank> tanksToRemove = new ArrayList<>();
        List<Tank> enemyTanksCopy = new ArrayList<>(enemyTanks);
        List<Bullet> newBullets = new ArrayList<>();

        // 1. 第一步：处理现有坦克AI和子弹生成
        for (Tank enemyTank : enemyTanksCopy) {
            if (enemyTank.isDestroyed()) {
                tanksToRemove.add(enemyTank);
                continue;
            }

            if (playerTank != null) {
                Bullet firedBullet = enemyTank.updateAI(grid, playerTank, this);
                if (firedBullet != null) {
                    newBullets.add(firedBullet);
                }
            }
        }

        // 2. 第二步：从集合中移除要删除的坦克
        synchronized (enemyTanks) {
            enemyTanks.removeAll(tanksToRemove);
        }

        // 3. 第三步：添加新子弹
        for (Bullet bullet : newBullets) {
            addBullet(bullet);
        }

        // 4. 第四步：处理坦克重生逻辑（独立于第一步的循环）
        long currentTime = System.currentTimeMillis();
        List<Long> respawnTimesToRemove = new ArrayList<>();
        List<Long> respawnTimesToAdd = new ArrayList<>();

        // 当前敌方坦克数量
        int currentEnemyCount = enemyTanks.size();

        // 检查重生时间队列
        for (Long respawnTime : tankRespawnTimes) {
            if (currentTime >= respawnTime) {
                respawnTimesToRemove.add(respawnTime);

                if (currentEnemyCount < maxConcurrentEnemies &&
                        enemyTanksGenerated < totalEnemyTanksToGenerate) {
                    // 标记需要生成新坦克，而不是立即生成
                    currentEnemyCount++;
                } else {
                    respawnTimesToAdd.add(currentTime + 500);
                }
            }
        }

        // 5. 第五步：更新重生队列
        synchronized (tankRespawnTimes) {
            tankRespawnTimes.removeAll(respawnTimesToRemove);
            tankRespawnTimes.addAll(respawnTimesToAdd);
        }

        // 6. 第六步：生成新坦克（在所有迭代完成后）
        for (int i = 0; i < respawnTimesToRemove.size() - respawnTimesToAdd.size(); i++) {
            if (currentEnemyCount <= maxConcurrentEnemies &&
                    enemyTanksGenerated < totalEnemyTanksToGenerate) {
                spawnNewEnemyTank();
            }
        }

        // 7. 第七步：如果没有足够的敌方坦克，安排生成新坦克
        if (enemyTanks.size() < maxConcurrentEnemies &&
                enemyTanksGenerated < totalEnemyTanksToGenerate &&
                tankRespawnTimes.isEmpty()) {
            tankRespawnTimes.add(currentTime + 1000);
        }

        // 8. 第八步：游戏开始时生成初始坦克
        if (enemyTanks.isEmpty() && enemyTanksGenerated == 0 && tankRespawnTimes.isEmpty()) {
            int initialTanks = Math.min(5, totalEnemyTanksToGenerate);
            for (int i = 0; i < initialTanks; i++) {
                spawnNewEnemyTank();
            }
        }

        for (Tank tank : enemyTanks) {
            if (!tank.isFriendly() && !tank.isDestroyed()) {
                // 检查坦克是否保持静止，如果是，重置其AI状态
                if (tank.getCurrentSpeed() < 0.1) {
                    // 记录当前位置
                    int oldX = tank.getX();
                    int oldY = tank.getY();

                    // 重置AI并强制移动
                    tank.resetAIState();
                    tank.updateAI(grid, playerTank, this);

                    // 如果仍未移动，尝试随机移动
                    if (oldX == tank.getX() && oldY == tank.getY()) {
                        // 随机设置方向并强制移动
                        tank.setAccelerating(true);
                        tank.move(this);
                    }
                }

                // 正常的AI更新
                Bullet bullet = tank.updateAI(grid, playerTank, this);
                if (bullet != null) {
                    bullets.add(bullet);
                }
            }
        }
    }


    /**
     * 检查碰撞
     */
    public String checkCollision(int x, int y, int width, int height) {
        if (levelMap == null) return null;

        // 创建坦克矩形区域
        Rectangle tankRect = new Rectangle(x, y, width, height);
        boolean foundWater = false; // 跟踪是否发现水池
        String result = null;

        // 检查与地图元素的碰撞
        for (LevelMap.MapElement element : levelMap.getElements()) {
            String type = element.getType();

            // 跳过草地元素，草地可以通行
            if (type.equals("grass")) continue;

            Rectangle elementRect = new Rectangle(element.getX(), element.getY(), element.getWidth(), element.getHeight());

            if (tankRect.intersects(elementRect.getBoundsInLocal())) {
                if (type.equals("water")) {
                    foundWater = true;

                    // 如果是玩家坦克并且处于无敌状态，则不触发水池伤害
                    if (playerTank != null &&
                            x == playerTank.getX() &&
                            y == playerTank.getY() &&
                            (playerTank.isRespawnInvincible() ||
                                    playerTank.isInvincible() ||
                                    playerTank.isShielded())) {

                        continue;
                    }

                    // 获取当前时间
                    long currentTime = System.currentTimeMillis();

                    // 检查是否已经过了冷却时间
                    if (currentTime - lastWaterDamageTime > WATER_DAMAGE_COOLDOWN) {
                        // 冷却时间已过，可以扣血并更新上次扣血时间
                        result = type;
                        lastWaterDamageTime = currentTime;
                    }
                } else {
                    return type; // 如果是其他障碍物，直接返回
                }
            }
        }

        inWaterLastFrame = foundWater;

        return result; // 返回碰撞类型
    }

    /**
     * 检查位置是否有效
     */
    private boolean isPositionValid(int x, int y, int width, int height) {
        if (levelMap == null) return false;

        // 创建坦克占用的矩形区域
        Rectangle tankRect = new Rectangle(x, y, width, height);

        // 检查与地图元素的碰撞
        for (LevelMap.MapElement element : levelMap.getElements()) {
            String type = element.getType();

            // 跳过可以通过的元素
            if (type.equals("grass")) continue;

            Rectangle elementRect = new Rectangle(element.getX(), element.getY(),
                    element.getWidth(), element.getHeight());

            if (tankRect.intersects(elementRect.getBoundsInLocal())) {
                return false; // 位置无效，与障碍物重叠
            }
        }


        return true; // 位置有效
    }

    /**
     * 寻找有效的出生位置
     */
    private LevelMap.MapPosition findValidPosition() {
        if (levelMap == null) return null;

        int mapWidth = levelMap.getWidth();
        int mapHeight = levelMap.getHeight();
        int tankSize = 40;

        // 创建可能的出生点列表
        List<int[]> possiblePositions = new ArrayList<>();

        // 添加地图上半部分的更多可能位置
        for (int x = 40; x < mapWidth - tankSize; x += 80) {
            possiblePositions.add(new int[]{x, 40}); // 顶部
            possiblePositions.add(new int[]{x, 120}); // 次顶部
        }

        // 添加地图中部的一些位置
        for (int x = 80; x < mapWidth - tankSize; x += 160) {
            possiblePositions.add(new int[]{x, mapHeight / 2 - tankSize});
        }

        // 添加原来的候选位置
        possiblePositions.add(new int[]{mapWidth / 2 - tankSize / 2, mapHeight - tankSize - 40}); // 底部中间
        possiblePositions.add(new int[]{40, 40}); // 左上角
        possiblePositions.add(new int[]{mapWidth - tankSize - 40, 40}); // 右上角
        possiblePositions.add(new int[]{40, mapHeight - tankSize - 40}); // 左下角
        possiblePositions.add(new int[]{mapWidth - tankSize - 40, mapHeight - tankSize - 40}); // 右下角

        // 打乱位置顺序，增加随机性
        Collections.shuffle(possiblePositions);

        // 检查这些随机位置是否可用
        for (int[] pos : possiblePositions) {
            if (isPositionValid(pos[0], pos[1], tankSize, tankSize)) {
                LevelMap.MapPosition newPos = new LevelMap.MapPosition();
                newPos.setX(pos[0]);
                newPos.setY(pos[1]);
                newPos.setWidth(tankSize);
                newPos.setHeight(tankSize);
                return newPos;
            }
        }

        // 如果所有候选位置都不可用，尝试随机位置（先尝试上半部分地图）
        for (int attempt = 0; attempt < 30; attempt++) {
            // 倾向于在地图上半部分生成
            int y = (int) (Math.random() * (mapHeight / 2));
            int x = (int) (Math.random() * (mapWidth - tankSize));

            if (isPositionValid(x, y, tankSize, tankSize)) {
                LevelMap.MapPosition newPos = new LevelMap.MapPosition();
                newPos.setX(x);
                newPos.setY(y);
                newPos.setWidth(tankSize);
                newPos.setHeight(tankSize);
                return newPos;
            }
        }

        // 如果上半部分找不到，再尝试整个地图范围
        for (int attempt = 0; attempt < 20; attempt++) {
            int x = (int) (Math.random() * (mapWidth - tankSize));
            int y = (int) (Math.random() * (mapHeight - tankSize));

            if (isPositionValid(x, y, tankSize, tankSize)) {
                LevelMap.MapPosition newPos = new LevelMap.MapPosition();
                newPos.setX(x);
                newPos.setY(y);
                newPos.setWidth(tankSize);
                newPos.setHeight(tankSize);
                return newPos;
            }
        }

        // 找不到有效位置
        System.err.println("严重错误：无法找到有效的出生位置！");
        return null;
    }

    /**
     * 检查两个坦克是否碰撞
     */
    public boolean checkTankCollision(Tank tank1, Tank tank2) {
        return tank1.getX() < tank2.getX() + tank2.getWidth() &&
                tank1.getX() + tank1.getWidth() > tank2.getX() &&
                tank1.getY() < tank2.getY() + tank2.getHeight() &&
                tank1.getY() + tank1.getHeight() > tank2.getY();
    }

    /**
     * 检查敌方坦克与玩家坦克的碰撞
     *
     * @return 是否发生碰撞
     */
    public boolean checkEnemyPlayerCollisions() {
        if (playerTank == null || playerTank.isDestroyed()) {
            return false;
        }

        boolean collisionDetected = false;

        // 创建一个要删除的坦克列表，避免在迭代中修改集合
        List<Tank> tanksToDestroy = new ArrayList<>();

        // 检查每个敌方坦克
        for (Tank enemyTank : enemyTanks) {
            if (enemyTank.isDestroyed()) {
                continue;
            }

            // 检查玩家坦克和敌方坦克之间是否发生碰撞
            if (checkTankCollision(playerTank, enemyTank)) {
                collisionDetected = true;

                // 检查玩家是否处于无敌状态
                if (playerTank.isInvincible() || playerTank.isRespawnInvincible()) {

                }
                // 检查玩家是否有护盾
                else if (playerTank.isShielded()) {
                    // 移除护盾状态
                    playerTank.removeEffect(Tank.PowerUpType.SHIELD);
                }
                // 正常受到伤害
                else {
                    // 碰撞发生时，减少玩家坦克的血量
                    playerTank.takeDamage(1);

                    // 检查玩家是否死亡
                    if (playerTank.getHealth() <= 0) {
                        playerTank.setDestroyed(true);
                        return true; // 玩家坦克被摧毁
                    }

                    // 更新血量显示
                    if (gameView != null) {
                        gameView.updateHealthDisplay();
                    }
                }

                // 敌方坦克被摧毁
                enemyTank.setHealth(0);
                enemyTank.setDestroyed(true);

                // 记录要删除的坦克
                tanksToDestroy.add(enemyTank);

                // 增加击毁敌方坦克计数
                enemyTanksDestroyed++;
            }
        }

        // 移除被摧毁的坦克
        for (Tank tank : tanksToDestroy) {
            // 如果使用了坦克池，可以考虑将坦克返回到池中而不是直接删除
            enemyTanks.remove(tank);

            // 尝试生成增益效果 - 添加这行代码
            trySpawnPowerUpOnTankDestroyed(tank.getX(), tank.getY());
        }

        return collisionDetected;
    }

    /**
     * 寻找玩家坦克的有效出生位置
     */
    public LevelMap.MapPosition findValidSpawnPosition() {
        // 定义玩家坦克周围的安全区域（100像素）
        final int SAFE_DISTANCE = 100;
        // 网格大小，确保坦克生成位置对齐到网格
        final int GRID_SIZE = 40;

        // 获取地图尺寸
        int mapWidth = levelMap.getWidth();
        int mapHeight = levelMap.getHeight();

        // 最多尝试30次寻找合适的位置
        for (int attempt = 0; attempt < 30; attempt++) {
            // 生成网格对齐的坐标（确保是40的整数倍）
            int gridX = (int) (Math.random() * ((mapWidth - 40) / GRID_SIZE));
            int gridY = (int) (Math.random() * ((mapHeight - 40) / GRID_SIZE));

            // 将网格坐标转换为像素坐标，确保严格对齐到网格
            int x = gridX * GRID_SIZE;
            int y = gridY * GRID_SIZE;

            // 确保不会生成在地图边缘
            if (x < GRID_SIZE || y < GRID_SIZE || x > mapWidth - 80 || y > mapHeight - 80) {
                continue;
            }

            // 检查位置是否有效（不与地图障碍物重叠）
            if (isPositionValid(x, y, 40, 40)) {
                // 检查是否在玩家坦克安全区域之外
                if (playerTank != null) {
                    int playerX = playerTank.getX();
                    int playerY = playerTank.getY();

                    // 计算与玩家的距离
                    double distance = Math.sqrt(
                            Math.pow(x + 20 - (playerX + 20), 2) +
                                    Math.pow(y + 20 - (playerY + 20), 2)
                    );

                    // 如果在安全区域内，继续尝试
                    if (distance < SAFE_DISTANCE) {
                        continue;
                    }
                }

                // 检查与其他敌方坦克的碰撞
                boolean collidesWithOtherTank = false;
                for (Tank enemyTank : enemyTanks) {
                    if (x < enemyTank.getX() + enemyTank.getWidth() &&
                            x + 40 > enemyTank.getX() &&
                            y < enemyTank.getY() + enemyTank.getHeight() &&
                            y + 40 > enemyTank.getY()) {
                        collidesWithOtherTank = true;
                        break;
                    }
                }

                if (!collidesWithOtherTank) {
                    // 创建并返回有效位置
                    LevelMap.MapPosition position = new LevelMap.MapPosition();
                    position.setX(x);
                    position.setY(y);
                    position.setWidth(40);
                    position.setHeight(40);
                    return position;
                }
            }
        }

        
        return null; // 无法找到有效位置
    }

    /**
     * 重生玩家坦克
     */
    public void respawnPlayerTank(String tankType, int x, int y) {
        // 确保位置对齐到网格
        int alignedX = (x / 40) * 40;
        int alignedY = (y / 40) * 40;

        // 创建新的玩家坦克
        Tank.TankType type = Tank.TankType.fromString(tankType);
        playerTank = new Tank(type, alignedX, alignedY);

        // 设置初始方向为向上
        playerTank.setDirection(Tank.Direction.UP);

        // 设置复活无敌状态
        playerTank.setRespawnInvincible(true);

        
    }

    /**
     * 获取被击败的敌人数量
     */
    public int getDefeatedEnemiesCount() {
        return enemyTanksDestroyed;
    }

    /**
     * 重置玩家水池状态 - 同时重置冷却时间
     */
    public void resetWaterState() {
        inWaterLastFrame = false;
        lastWaterDamageTime = 0; // 重置冷却时间
    }

    /**
     * 更新并检测子弹碰撞
     *
     * @return 是否有玩家生命减少
     */
    public boolean updateBulletsAndCheckCollisions() {
        boolean playerLostLife = false;
        Iterator<Bullet> iterator = bullets.iterator();

        while (iterator.hasNext()) {
            Bullet bullet = iterator.next();
            bullet.move();

            // 检查是否出界
            if (bullet.isOutOfBounds(levelMap.getWidth(), levelMap.getHeight())) {
                iterator.remove();
                continue;
            }

            // 子弹碰撞检测区域 (15x15)
            Rectangle bulletRect = new Rectangle(
                    bullet.getX() - 2.5, // 子弹中心点调整（10x10 -> 15x15）
                    bullet.getY() - 2.5,
                    15, 15);

            boolean bulletHit = false;

            // 检查子弹与坦克的碰撞
            if (bullet.isFromPlayer()) {
                // 玩家子弹与敌方坦克碰撞
                for (Iterator<Tank> tankIt = enemyTanks.iterator(); tankIt.hasNext(); ) {
                    Tank enemyTank = tankIt.next();
                    Rectangle tankRect = new Rectangle(
                            enemyTank.getX(), enemyTank.getY(),
                            enemyTank.getWidth(), enemyTank.getHeight());

                    if (bulletRect.intersects(tankRect.getBoundsInLocal())) {
                        // 敌方坦克受到伤害
                        boolean destroyed = enemyTank.takeDamage(bullet.getDamage());
                        bulletHit = true;

                        if (destroyed) {
                            // 敌方坦克被摧毁
                            tankIt.remove();
                            enemyTanksDestroyed++; // 增加已摧毁计数

                            // 安排新坦克生成（3秒后）
                            if (enemyTanksGenerated < totalEnemyTanksToGenerate) {
                                tankRespawnTimes.add(System.currentTimeMillis() + 3000);
                                System.out.println("敌方坦克被摧毁！已安排3秒后生成新坦克，当前已摧毁: " +
                                        enemyTanksDestroyed + "/" + totalEnemyTanksToGenerate);
                            }
                        }
                        break;
                    }
                }
            } else {
                // 敌方子弹与玩家坦克碰撞
                if (playerTank != null) {
                    Rectangle tankRect = new Rectangle(
                            playerTank.getX(), playerTank.getY(),
                            playerTank.getWidth(), playerTank.getHeight());

                    if (bulletRect.intersects(tankRect.getBoundsInLocal())) {
                        // 检查玩家是否处于无敌状态
                        if (playerTank.isInvincible() || playerTank.isRespawnInvincible()) {
                            bulletHit = true;
                        }
                        // 检查玩家是否有护盾
                        else if (playerTank.isShielded()) {
                            // 移除护盾状态
                            playerTank.removeEffect(Tank.PowerUpType.SHIELD);
                            bulletHit = true;
                        }
                        // 正常受到伤害
                        else {
                            // 玩家坦克受到伤害
                            if (playerTank.getHealth() > bullet.getDamage()) {
                                // 扣血但不致死
                                playerTank.setHealth(playerTank.getHealth() - bullet.getDamage());
                                // 更新血量显示
                                if (gameView != null) {
                                    gameView.updateHealthDisplay();
                                }
                            } else {
                                // 玩家血量不足，生命值减一
                                playerTank.setHealth(0);
                                playerLostLife = true;
                            }
                            bulletHit = true;
                        }
                    }
                }
            }

            // 如果子弹击中了目标
            if (bulletHit) {
                // 移除子弹
                iterator.remove();

                // 如果是玩家的子弹，增加子弹数量（在View层处理）
            }
        }

        return playerLostLife;
    }

    /**
     * 检查子弹与地图元素的碰撞
     * 返回子弹碰撞的元素类型，如果没有碰撞则返回null
     */
    public String checkBulletCollisions(Bullet bullet) {
        // 遍历所有地图元素检查碰撞
        for (int i = 0; i < levelMap.getElements().size(); i++) {
            LevelMap.MapElement element = levelMap.getElements().get(i);

            // 跳过草地，子弹可以穿过
            if (element.getType().equals("grass")) {
                continue;
            }

            // 检查碰撞
            if (bullet.getX() < element.getX() + element.getWidth() &&
                    bullet.getX() + bullet.getWidth() > element.getX() &&
                    bullet.getY() < element.getY() + element.getHeight() &&
                    bullet.getY() + bullet.getHeight() > element.getY()) {

                // 如果是砖块，移除它
                if (element.getType().equals("brick")) {
                    levelMap.getElements().remove(i);
                }

                // 返回碰撞的元素类型
                return element.getType();
            }
        }

        return null; // 没有碰撞
    }

    /**
     * 更新游戏中的子弹状态
     */
    public void updateBullets(double deltaTime) {
        // 创建一个新列表来存储需要保留的子弹
        List<Bullet> survivingBullets = new ArrayList<>();

        for (Bullet bullet : bullets) {
            // 移动子弹
            bullet.move();

            // 检查子弹是否超出地图边界
            if (bullet.isOutOfBounds(levelMap.getWidth() * 40, levelMap.getHeight() * 40)) {
                continue; // 跳过这颗子弹，不添加到新列表
            }

            // 检查子弹与地图元素的碰撞
            String collisionType = checkBulletCollisions(bullet);

            // 如果子弹击中了钢铁或砖块
            if (collisionType != null && (collisionType.equals("steel") || collisionType.equals("brick"))) {
                // 子弹被销毁
                continue; // 跳过这颗子弹，不添加到新列表
            }

            // 检查子弹与坦克的碰撞
            boolean hitTank = checkBulletTankCollisions(bullet);

            // 如果子弹击中了坦克
            if (hitTank) {
                continue; // 跳过这颗子弹，不添加到新列表
            }

            // 如果子弹没有被销毁，添加到存活子弹列表
            if (!bullet.isDestroyed()) {
                survivingBullets.add(bullet);
            }
        }

        // 更新子弹列表
        bullets = survivingBullets;
    }

    /**
     * 检查子弹与坦克的碰撞
     *
     * @param bullet 需要检查的子弹
     * @return 是否发生了碰撞
     */
    public boolean checkBulletTankCollisions(Bullet bullet) {
        // 检查所有坦克(包括玩家坦克)
        List<Tank> allTanks = new ArrayList<>(enemyTanks);
        if (playerTank != null) {
            allTanks.add(playerTank);
        }

        for (Tank tank : allTanks) {
            // 如果坦克已被摧毁，跳过
            if (tank.isDestroyed()) {
                continue;
            }

            // 检查子弹是否击中坦克
            if (bullet.getX() < tank.getX() + tank.getWidth() &&
                    bullet.getX() + bullet.getWidth() > tank.getX() &&
                    bullet.getY() < tank.getY() + tank.getHeight() &&
                    bullet.getY() + bullet.getHeight() > tank.getY()) {

                // 如果子弹和坦克来自同一阵营，忽略碰撞
                if (bullet.isFromPlayer() == tank.isFriendly()) {
                    continue;
                }

                // 子弹击中坦克，处理伤害
                boolean isDestroyed = tank.takeDamage(bullet.getDamage());

                // 如果击中的是敌方坦克，并且该坦克被摧毁
                if (!tank.isFriendly() && isDestroyed) {
                    // 增加已击败敌方坦克计数
                    enemyTanksDestroyed++;
                    System.out.println("敌方坦克被摧毁，类型：" + tank.getTypeString() +
                            "，已摧毁： " + enemyTanksDestroyed +
                            "，目标： " + totalEnemyTanksToGenerate);

                    // 修改这部分逻辑：只要当前生成的敌方坦克数小于总目标数量，就安排新坦克生成
                    if (enemyTanksGenerated < totalEnemyTanksToGenerate) {
                        tankRespawnTimes.add(System.currentTimeMillis() + 2000); // 改为2秒后生成
                        
                    }

                    // 尝试生成增益效果
                    trySpawnPowerUpOnTankDestroyed(tank.getX(), tank.getY());
                }

                return true; // 子弹命中，返回true以移除子弹
            }
        }

        return false; // 子弹未命中任何坦克
    }

    /**
     * 获取当前地图
     *
     * @return 当前关卡地图
     */
    public LevelMap getMap() {
        return levelMap;
    }

    /**
     * 获取指定类型的地图元素图片
     *
     * @param elementType 元素类型
     * @return 元素对应的图片
     */
    public Image getElementImage(String elementType) {
        return elementImages.get(elementType);
    }

    /**
     * 配置每个关卡的敌方坦克参数
     */
    private void configureEnemyTanksForLevel(int level) {
        // 清空现有的敌方坦克配置
        enemyTypesToGenerate.clear();

        // 根据关卡设置最大同时存在的敌方坦克数量
        if (level <= 3) {
            maxConcurrentEnemies = 5; // 前三关最多5个敌方坦克
        } else {
            maxConcurrentEnemies = 6; // 第4、5关最多6个敌方坦克
        }

        // 清空延迟生成队列
        tankRespawnTimes.clear();

        // 根据关卡设置敌方坦克目标数量和类型分布
        switch (level) {
            case 1:
                // 第一关：15个坦克，全部是基础坦克
                totalEnemyTanksToGenerate = 15;
                
                break;

            case 2:
                // 第二关：20个坦克，90%基础坦克，10%精英坦克
                totalEnemyTanksToGenerate = 20;
                
                break;

            case 3:
                // 第三关：30个坦克，57%基础坦克，40%精英坦克，3%Boss坦克
                totalEnemyTanksToGenerate = 30;
                
                break;

            case 4:
                // 第四关：45个坦克，20%基础坦克，60%精英坦克，20%Boss坦克
                totalEnemyTanksToGenerate = 45;
                
                break;

            case 5:
                // 第五关：60个坦克，15%基础坦克，50%精英坦克，35%Boss坦克
                totalEnemyTanksToGenerate = 60;
                
                break;

            default:
                // 默认配置，避免出错
                totalEnemyTanksToGenerate = 15;
                
                break;
        }

        // 重置敌方坦克统计信息
        enemyTanksGenerated = 0;
        enemyTanksDestroyed = 0;

        System.out.println("关卡 " + level + " 配置完成：目标敌方坦克数量=" +
                totalEnemyTanksToGenerate + ", 最大同时存在数量=" +
                maxConcurrentEnemies);
    }

    // 修改生成新敌方坦克
    private void spawnNewEnemyTank() {
        // 如果已经生成了足够的坦克，不再生成
        if (enemyTanksGenerated >= totalEnemyTanksToGenerate) {
            
            return;
        }

        // 寻找有效的生成位置
        LevelMap.MapPosition spawnPos = findValidSpawnPosition();
        if (spawnPos != null) {
            // 决定要生成哪种类型的敌方坦克
            Tank.TankType enemyType;
            double random = Math.random();

            // 根据当前关卡决定敌方坦克类型
            switch (currentLevel) {
                case 1:
                    // 第一关：100% 基础坦克
                    enemyType = Tank.TankType.BASIC;
                    break;

                case 2:
                    // 第二关：90% 基础坦克，10% 精英坦克
                    if (random < 0.9) {
                        enemyType = Tank.TankType.BASIC;
                    } else {
                        enemyType = Tank.TankType.ELITE;
                    }
                    break;

                case 3:
                    // 第三关：57% 基础坦克，40% 精英坦克，3% Boss坦克
                    if (random < 0.57) {
                        enemyType = Tank.TankType.BASIC;
                    } else if (random < 0.97) { // 0.57 + 0.40 = 0.97
                        enemyType = Tank.TankType.ELITE;
                    } else {
                        enemyType = Tank.TankType.BOSS;
                    }
                    break;

                case 4:
                    // 第四关：20% 基础坦克，60% 精英坦克，20% Boss坦克
                    if (random < 0.2) {
                        enemyType = Tank.TankType.BASIC;
                    } else if (random < 0.8) { // 0.2 + 0.6 = 0.8
                        enemyType = Tank.TankType.ELITE;
                    } else {
                        enemyType = Tank.TankType.BOSS;
                    }
                    break;

                case 5:
                    // 第五关：15% 基础坦克，50% 精英坦克，35% Boss坦克
                    if (random < 0.15) {
                        enemyType = Tank.TankType.BASIC;
                    } else if (random < 0.65) { // 0.15 + 0.5 = 0.65
                        enemyType = Tank.TankType.ELITE;
                    } else {
                        enemyType = Tank.TankType.BOSS;
                    }
                    break;

                default:
                    // 默认是基础坦克
                    enemyType = Tank.TankType.BASIC;
                    break;
            }

            // 创建新坦克并添加到列表中，确保位置是40的整数倍
            int alignedX = (spawnPos.getX() / 40) * 40;
            int alignedY = (spawnPos.getY() / 40) * 40;

            Tank enemyTank = new Tank(enemyType, alignedX, alignedY);

            // 随机设置初始方向
            int randomDir = (int) (Math.random() * 4);
            enemyTank.setDirection(Tank.Direction.fromValue(randomDir));

            // 给坦克设置一个"出生保护"标记，防止立即移动
            enemyTank.setInitialSpawnDelay(true);

            // 添加到敌方坦克列表
            enemyTanks.add(enemyTank);

            // 增加已生成的坦克计数
            enemyTanksGenerated++;
        } else {
            // 稍后再次尝试生成
            tankRespawnTimes.add(System.currentTimeMillis() + 500);
        }
    }

    // 获取关卡目标（需要消灭的坦克总数）
    public int getTotalEnemyTarget() {
        return totalEnemyTanksToGenerate;
    }

    // 获取当前剩余需要生成的敌人数量
    public int getRemainingEnemiesCount() {
        return totalEnemyTanksToGenerate - enemyTanksGenerated;
    }

    // 检查是否完成关卡目标
    public boolean isLevelCompleted() {
        // 关卡完成条件：已摧毁的坦克数量等于总目标且当前场上没有敌方坦克
        return enemyTanksDestroyed >= totalEnemyTanksToGenerate && enemyTanks.isEmpty();
    }

    /**
     * 处理玩家坦克受到伤害
     */
    public void handlePlayerTankDamage(int damage) {
        if (playerTank == null) return;

        // 调用takeDamage并检查返回值
        boolean tankDestroyed = playerTank.takeDamage(damage);

        // 如果坦克被摧毁，立即通知GameView
        if (tankDestroyed || playerTank.isDead()) {

            // 确保健康值设为0
            playerTank.setHealth(0);

            // 通知GameView处理玩家死亡
            if (eventListener != null) {
                eventListener.onPlayerDestroyed();
            }
        }
    }

    // 优化增益效果图片加载
    private void loadPowerUpImages() {
        String[] powerUpTypes = {"attack", "bomb", "health", "invincibility", "shield", "speed"};

        for (String type : powerUpTypes) {
            try {
                String path = "/images/powerups/" + type + ".png";

                InputStream is = getClass().getResourceAsStream(path);
                if (is != null) {
                    Image image = new Image(is);
                    powerUpImages.put(type, image);
                } else {
                    // 尝试加载默认图片
                    Image defaultImage = new Image(getClass().getResourceAsStream("/images/default_icon.png"));
                    if (defaultImage != null) {
                        powerUpImages.put(type, defaultImage);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // 额外加载放置的炸弹图片
        try {
            String bombPlacedPath = "/images/powerups/bomb_placed.png";
            InputStream is = getClass().getResourceAsStream(bombPlacedPath);
            if (is != null) {
                Image image = new Image(is);
                powerUpImages.put("bomb_placed", image);
                
            }
        } catch (Exception e) {
            System.err.println("加载放置炸弹图片失败: " + e.getMessage());
        }

        
    }

    // 更新并渲染增益效果
    public void updatePowerUps(double deltaTime) {
        // 定期生成增益效果
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastPowerUpSpawnTime > 8000) { // 改为8秒间隔
            spawnRandomPowerUp();
            lastPowerUpSpawnTime = currentTime;
        }

        // 更新增益效果状态
        Iterator<PowerUp> iterator = powerUps.iterator();
        while (iterator.hasNext()) {
            PowerUp powerUp = iterator.next();

            // 检查是否应该移除
            if (powerUp.shouldRemove()) {
                iterator.remove();
                continue;
            }

            // 增强碰撞检测逻辑，确保正确检测到碰撞
            if (playerTank != null && !playerTank.isDead()) {
                boolean collides = checkPlayerPowerUpCollision(playerTank, powerUp);
                if (collides) {
                    collectPowerUp(powerUp);
                    iterator.remove();
                    continue;
                }
            }
        }

        // 更新炸弹状态
        if (activeBomb != null) {
            updateBomb(currentTime);
        }
    }

    // 使用更精确的碰撞检测
    private boolean checkPlayerPowerUpCollision(Tank tank, PowerUp powerUp) {
        // 扩大一点碰撞范围，确保更容易拾取
        int tankX = tank.getX();
        int tankY = tank.getY();
        int tankWidth = tank.getWidth();
        int tankHeight = tank.getHeight();

        int powerUpX = powerUp.getX();
        int powerUpY = powerUp.getY();
        int powerUpWidth = powerUp.getWidth();
        int powerUpHeight = powerUp.getHeight();

        // 使用扩展的碰撞检测
        return tankX < powerUpX + powerUpWidth + 5 &&
                tankX + tankWidth + 5 > powerUpX &&
                tankY < powerUpY + powerUpHeight + 5 &&
                tankY + tankHeight + 5 > powerUpY;
    }

    // 修改现有的collectPowerUp方法，而不是创建新方法
    private void collectPowerUp(PowerUp powerUp) {
        powerUp.collect();

        // 应用增益效果
        Tank.PowerUpType type = powerUp.getType();

        // 确保所有效果都调用applyPowerUp
        playerTank.applyPowerUp(type);

        // 对于每种特定效果，可以额外处理
        switch (type) {
            case HEALTH:
                // 如果已调用applyPowerUp，这里可以不再重复设置健康值
                System.out.println("玩家恢复1点生命值，当前: " + playerTank.getHealth() + "/" +
                        playerTank.getMaxHealth());
                gameView.updateHealthDisplay();
                break;
            case BOMB:
                
                break;
            case ATTACK:
                
                break;
            case INVINCIBILITY:
                
                break;
            case SHIELD:
                
                break;
            case SPEED:
                
                break;
        }

    }

    // 优化随机生成逻辑，减少卡顿
    private void spawnRandomPowerUp() {
        if (levelMap == null) return;

        if (Math.random() > 0.7) {
            return;
        }

        // 使用预计算的随机位置
        LevelMap.MapPosition pos = findValidPowerUpPosition();
        if (pos != null) {
            // 随机选择增益效果类型
            Tank.PowerUpType[] types = Tank.PowerUpType.values();
            Tank.PowerUpType randomType = types[(int) (Math.random() * types.length)];

            // 创建增益效果并添加到列表
            PowerUp powerUp = new PowerUp(pos.getX(), pos.getY(), Tank.PowerUpType.INVINCIBILITY);
            powerUps.add(powerUp);
        }
    }

    // 查找有效的增益效果生成位置
    private LevelMap.MapPosition findValidPowerUpPosition() {
        if (levelMap == null) return null;

        int mapWidth = levelMap.getWidth();
        int mapHeight = levelMap.getHeight();
        int size = 30; // 增益效果大小

        // 最多尝试20次
        for (int attempt = 0; attempt < 20; attempt++) {
            // 生成随机位置，确保对齐到网格（40的倍数）
            int x = ((int) (Math.random() * (mapWidth - size) / 40)) * 40;
            int y = ((int) (Math.random() * (mapHeight - size) / 40)) * 40;

            // 检查位置是否有效
            if (isPositionValid(x, y, size, size)) {
                // 检查与其他增益效果的距离
                boolean tooClose = false;
                for (PowerUp existing : powerUps) {
                    double distance = Math.sqrt(
                            Math.pow(x - existing.getX(), 2) +
                                    Math.pow(y - existing.getY(), 2)
                    );
                    if (distance < 80) { // 至少80像素距离
                        tooClose = true;
                        break;
                    }
                }

                if (!tooClose) {
                    LevelMap.MapPosition position = new LevelMap.MapPosition();
                    position.setX(x);
                    position.setY(y);
                    position.setWidth(size);
                    position.setHeight(size);
                    return position;
                }
            }
        }

        return null; // 找不到有效位置
    }

    // 当坦克被摧毁时尝试生成增益效果
    public void trySpawnPowerUpOnTankDestroyed(int x, int y) {
        if (Math.random() < 0.6) {
            // 随机选择增益效果类型
            Tank.PowerUpType[] types = Tank.PowerUpType.values();
            Tank.PowerUpType randomType = types[(int) (Math.random() * types.length)];

            // 调整位置，确保不会被障碍物遮挡
            int alignedX = (x / 40) * 40 + 5; // 对齐到网格并轻微偏移
            int alignedY = (y / 40) * 40 + 5;

            // 创建增益效果并添加到列表
            PowerUp powerUp = new PowerUp(alignedX, alignedY, randomType);
            powerUps.add(powerUp);
        }
    }

    // 玩家放置炸弹
    public void placeBomb() {
        // 检查玩家是否有炸弹道具
        if (playerTank != null && playerTank.isEffectActive(Tank.PowerUpType.BOMB) && activeBomb == null) {
            // 放置炸弹在玩家坦克位置
            int bombX = playerTank.getX();
            int bombY = playerTank.getY();

            activeBomb = new Bomb(bombX, bombY, System.currentTimeMillis());
            

            // 移除炸弹效果（已使用）
            playerTank.removeEffect(Tank.PowerUpType.BOMB);
        } else if (playerTank != null && !playerTank.isEffectActive(Tank.PowerUpType.BOMB)) {
            
        } else if (activeBomb != null) {
            
        }
    }

    // 更新炸弹状态
    private void updateBomb(long currentTime) {
        if (activeBomb != null) {
            // 检查炸弹是否应该爆炸
            if (activeBomb.shouldExplode()) {
                detonateBomb();
            }
        }
    }

    // 引爆炸弹
    private void detonateBomb() {
        if (activeBomb == null) return;

        // 爆炸范围
        final int EXPLOSION_RANGE = 80;
        int bombX = activeBomb.getX();
        int bombY = activeBomb.getY();

        

        // 检查范围内的敌方坦克
        List<Tank> tanksInRange = new ArrayList<>();
        for (Tank enemy : enemyTanks) {
            if (enemy.isDestroyed()) continue;

            // 计算坦克中心点与炸弹中心点的距离
            int enemyCenterX = enemy.getX() + enemy.getWidth() / 2;
            int enemyCenterY = enemy.getY() + enemy.getHeight() / 2;
            int bombCenterX = bombX + 15; // 炸弹大小为30x30，中心点偏移15
            int bombCenterY = bombY + 15;

            double distance = Math.sqrt(
                    Math.pow(enemyCenterX - bombCenterX, 2) +
                            Math.pow(enemyCenterY - bombCenterY, 2)
            );

            // 如果在爆炸范围内，添加到受影响坦克列表
            if (distance <= EXPLOSION_RANGE) {
                tanksInRange.add(enemy);
            }
        }

        // 对范围内的坦克造成伤害
        for (Tank enemy : tanksInRange) {
            enemy.takeDamage(2); // 造成2点伤害

            // 如果坦克被摧毁
            if (enemy.getHealth() <= 0) {
                enemy.setDestroyed(true);
                enemyTanksDestroyed++;

                // 尝试生成增益效果
                trySpawnPowerUpOnTankDestroyed(enemy.getX(), enemy.getY());
            }
        }

        // 清除炸弹
        activeBomb = null;

        // 这里可以添加爆炸动画或音效
    }

    // 获取当前增益效果列表
    public List<PowerUp> getPowerUps() {
        return powerUps;
    }

    // 获取增益效果图片
    public Image getPowerUpImage(String name) {
        return powerUpImages.get(name);
    }

    public void updatePlayerTank() {
        // 如果坦克已死亡，但未通知视图
        if (playerTank != null && playerTank.isDead() && eventListener != null) {
            eventListener.onPlayerDestroyed();
        }
    }

    // 在GameController类中添加一个setter方法来设置gameView引用
    public void setGameView(GameView gameView) {
        this.gameView = gameView;
    }

    /**
     * 保存游戏状态
     *
     * @param saveName 存档名称，如果为null，将使用时间戳生成名称
     * @return 保存是否成功
     */
    public boolean saveGame(String saveName) {
        try {
            // 创建存档目录
            File saveDir = new File("saves");
            if (!saveDir.exists()) {
                saveDir.mkdir();
            }

            // 生成存档文件名
            if (saveName == null || saveName.isEmpty()) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
                saveName = "save_" + sdf.format(new Date());
            }

            File saveFile = new File(saveDir, saveName + ".json");

            // 创建存档数据对象
            GameSaveData saveData = new GameSaveData();

            // 填充基本信息
            saveData.setCurrentLevel(currentLevel);
            saveData.setPlayerTankType(playerTank.getTypeString());
            saveData.setGameTime(gameView.getTotalGameTime());
            saveData.setScore(gameView.getScore());
            saveData.setPlayerLives(gameView.getPlayerLives());
            saveData.setBulletCount(gameView.getBulletCount());

            // 保存玩家坦克信息
            saveData.setPlayerTank(new GameSaveData.TankData(playerTank));

            // 保存敌方坦克信息
            for (Tank enemyTank : enemyTanks) {
                saveData.getEnemyTanks().add(new GameSaveData.TankData(enemyTank));
            }

            // 保存子弹信息
            for (Bullet bullet : bullets) {
                saveData.getBullets().add(new GameSaveData.BulletData(bullet));
            }

            // 保存增益效果信息
            for (PowerUp powerUp : powerUps) {
                saveData.getPowerUps().add(new GameSaveData.PowerUpData(powerUp));
            }

            // 保存战斗统计
            saveData.setEnemyTanksDestroyed(enemyTanksDestroyed);
            saveData.setTotalEnemyTanksToGenerate(totalEnemyTanksToGenerate);
            saveData.setEnemyTanksGenerated(enemyTanksGenerated);

            // 使用 Gson 将数据转换为 JSON 并写入文件
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            try (FileWriter writer = new FileWriter(saveFile)) {
                gson.toJson(saveData, writer);
            }

            
            return true;

        } catch (Exception e) {
            System.err.println("保存游戏失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 加载游戏存档
     *
     * @param saveFile 存档文件
     * @return 加载是否成功
     */
    public boolean loadGame(File saveFile) {
        try {
            // 读取存档文件
            Gson gson = new Gson();
            GameSaveData saveData;

            try (FileReader reader = new FileReader(saveFile)) {
                saveData = gson.fromJson(reader, GameSaveData.class);
            }

            // 清理当前游戏状态
            enemyTanks.clear();
            bullets.clear();
            powerUps.clear();

            // 加载地图（确保当前关卡地图已经加载）
            currentLevel = saveData.getCurrentLevel();
            this.levelMap = MapLoader.loadLevel(currentLevel);

            // 重新配置敌方坦克参数
            configureEnemyTanksForLevel(currentLevel);

            // 加载玩家坦克
            GameSaveData.TankData playerTankData = saveData.getPlayerTank();
            Tank.TankType playerType = Tank.TankType.fromString(playerTankData.getType());
            playerTank = new Tank(playerType, playerTankData.getX(), playerTankData.getY());
            playerTank.setDirection(Tank.Direction.fromValue(playerTankData.getDirection()));
            playerTank.setHealth(playerTankData.getHealth());

            // 加载玩家坦克的激活效果
            for (Map.Entry<String, Double> effect : playerTankData.getActiveEffects().entrySet()) {
                for (Tank.PowerUpType powerUpType : Tank.PowerUpType.values()) {
                    if (powerUpType.getName().equals(effect.getKey())) {
                        // 以剩余时间设置效果
                        playerTank.applyPowerUp(powerUpType, effect.getValue());
                    }
                }
            }

            // 加载敌方坦克
            for (GameSaveData.TankData tankData : saveData.getEnemyTanks()) {
                Tank.TankType enemyType = Tank.TankType.fromString(tankData.getType());
                Tank enemyTank = new Tank(enemyType, tankData.getX(), tankData.getY());
                enemyTank.setDirection(Tank.Direction.fromValue(tankData.getDirection()));
                enemyTank.setHealth(tankData.getHealth());
                enemyTanks.add(enemyTank);
            }

            // 加载子弹
            for (GameSaveData.BulletData bulletData : saveData.getBullets()) {
                Bullet bullet = new Bullet(
                        bulletData.getX(),
                        bulletData.getY(),
                        Tank.Direction.fromValue(bulletData.getDirection()),
                        bulletData.getBulletType(),
                        bulletData.getSpeed(),
                        bulletData.getDamage(),
                        bulletData.isFromPlayer()
                );
                bullets.add(bullet);
            }

            // 加载增益效果
            for (GameSaveData.PowerUpData powerUpData : saveData.getPowerUps()) {
                for (Tank.PowerUpType type : Tank.PowerUpType.values()) {
                    if (type.getName().equals(powerUpData.getType())) {
                        PowerUp powerUp = new PowerUp(
                                powerUpData.getX(),
                                powerUpData.getY(),
                                type
                        );
                        // 设置创建时间以保持剩余生命周期一致
                        powerUp.setCreationTime(powerUpData.getCreationTime());
                        powerUps.add(powerUp);
                        break;
                    }
                }
            }

            // 加载战斗统计
            enemyTanksDestroyed = saveData.getEnemyTanksDestroyed();
            totalEnemyTanksToGenerate = saveData.getTotalEnemyTanksToGenerate();
            enemyTanksGenerated = saveData.getEnemyTanksGenerated();

            // 更新GameView中的数据
            gameView.setTotalGameTime(saveData.getGameTime());
            gameView.setScore(saveData.getScore());
            gameView.setPlayerLives(saveData.getPlayerLives());
            gameView.setBulletCount(saveData.getBulletCount());

            // 重置游戏时间
            gameView.resetGameStartTime();

            // 重新初始化网格数据 - 确保网格数据正确
            initializeGrid();

            // 重置所有敌方坦克的AI状态
            for (Tank enemyTank : enemyTanks) {
                if (!enemyTank.isFriendly()) {
                    // 重置AI状态
                    enemyTank.resetAIState();

                    // 确保坦克不在障碍物中
                    String collision = checkCollision(enemyTank.getX(), enemyTank.getY(),
                            enemyTank.getWidth(), enemyTank.getHeight());
                    if (collision != null && !collision.equals("water")) {
                        // 如果坦克在障碍物中，移动到有效位置
                        LevelMap.MapPosition validPos = findValidSpawnPosition();
                        if (validPos != null) {
                            enemyTank.setX(validPos.getX());
                            enemyTank.setY(validPos.getY());
                        }
                    }
                }
            }

            // 强制更新一次敌方坦克，使其立即开始移动
            for (Tank enemyTank : enemyTanks) {
                if (!enemyTank.isFriendly() && !enemyTank.isDestroyed()) {
                    // 确保状态正确
                    enemyTank.setAccelerating(true);
                    enemyTank.move(this); // 强制移动一次
                }
            }

            
            return true;

        } catch (Exception e) {
            System.err.println("加载游戏失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}