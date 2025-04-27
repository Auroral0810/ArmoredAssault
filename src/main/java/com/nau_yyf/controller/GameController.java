package com.nau_yyf.controller;

import com.nau_yyf.model.Bullet;
import com.nau_yyf.model.LevelMap;
import com.nau_yyf.model.Tank;
import com.nau_yyf.util.MapLoader;
import javafx.scene.image.Image;
import javafx.scene.canvas.GraphicsContext;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import java.util.Collections;

public class GameController {
    private int currentLevel = 1;
    private LevelMap levelMap;
    private Tank playerTank;
    private List<Tank> enemyTanks = new ArrayList<>();
    private final Map<String, Image> elementImages = new HashMap<>();
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
    
    public GameController() {
        // 预加载地图元素图片
        loadElementImages();
        
        // 预加载坦克图片
        loadTankImages();
        
        // 预加载子弹图片
        loadBulletImages();
    }
    
    private void loadElementImages() {
        try {
            elementImages.put("brick", new Image(getClass().getResourceAsStream("/images/map/brick.png")));
            elementImages.put("steel", new Image(getClass().getResourceAsStream("/images/map/steel.png")));
            elementImages.put("grass", new Image(getClass().getResourceAsStream("/images/map/grass.png")));
            elementImages.put("water", new Image(getClass().getResourceAsStream("/images/map/water.png")));
            elementImages.put("base", new Image(getClass().getResourceAsStream("/images/map/base.png")));
        } catch (Exception e) {
            System.err.println("加载地图元素图片失败: " + e.getMessage());
        }
    }
    
    private void loadTankImages() {
        try {
            // 加载玩家坦克图片 - 每个方向一张图片
            for (String type : new String[]{"light", "standard", "heavy"}) {
                Image[] dirImages = new Image[4];
                for (int i = 0; i < 4; i++) {
                    String path = "/images/tanks/friendly/" + type + "/" + i + ".png";
                    System.out.println("加载坦克图片: " + path);
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
                
                System.out.println("加载敌方坦克图片: " + basicPath);
                basicTank[i] = new Image(getClass().getResourceAsStream(basicPath));
                
                System.out.println("加载敌方坦克图片: " + elitePath);
                eliteTank[i] = new Image(getClass().getResourceAsStream(elitePath));
                
                System.out.println("加载敌方坦克图片: " + bossPath);
                bossTank[i] = new Image(getClass().getResourceAsStream(bossPath));
            }
            
            tankImages.put("enemy_basic", basicTank);
            tankImages.put("enemy_elite", eliteTank);
            tankImages.put("enemy_boss", bossTank);
            
            // 验证所有图像是否成功加载
            System.out.println("坦克图像加载完成: " + tankImages.size() + " 种类型");
            for (Map.Entry<String, Image[]> entry : tankImages.entrySet()) {
                System.out.println("  类型: " + entry.getKey() + ", 方向数: " + entry.getValue().length);
            }
            
        } catch (Exception e) {
            System.err.println("加载坦克图片失败: " + e.getMessage());
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
            
            System.out.println("子弹图像加载完成: " + bulletImages.size() + " 种类型");
        } catch (Exception e) {
            System.err.println("加载子弹图片失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void loadLevel(int level) {
        this.currentLevel = level;
        this.levelMap = MapLoader.loadLevel(level);
        System.out.println("加载关卡 " + level + ": " + (levelMap != null ? levelMap.getName() : "加载失败"));
        
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
                System.out.println("警告：玩家出生点(" + playerPos.getX() + "," + playerPos.getY() + ")与障碍物重叠！");
                
                // 寻找有效的替代位置
                LevelMap.MapPosition validPos = findValidPosition();
                if (validPos != null) {
                    System.out.println("找到有效替代位置: (" + validPos.getX() + "," + validPos.getY() + ")");
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
        
        // 确保初始化阶段完成后，至少会生成设定数量的敌方坦克
        System.out.println("关卡初始化完成，当前敌方坦克: " + enemyTanks.size() + 
                         "，已生成: " + enemyTanksGenerated + 
                         "，队列中待生成: " + enemyTypesToGenerate.size() + 
                         "，总目标: " + totalEnemyTanksToGenerate);
        
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
                        System.out.println("找不到子弹图像 " + bulletType + "，使用默认图像");
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
    
    // 修改updateEnemyTanks方法，确保敌方坦克可以正确追踪玩家
    public void updateEnemyTanks() {
        if (playerTank == null) return;
        
        // 确保网格信息是最新的
        if (grid == null) {
            initializeGrid();
        }
        
        // 检查是否需要生成新坦克
        long currentTime = System.currentTimeMillis();
        List<Long> timesToRemove = new ArrayList<>();
        
        // 检查是否有坦克需要重生
        for (Long respawnTime : tankRespawnTimes) {
            if (currentTime >= respawnTime) {
                // 时间到，可以重生一个坦克
                if (enemyTanks.size() < maxConcurrentEnemies && enemyTanksGenerated < totalEnemyTanksToGenerate) {
                    spawnNewEnemyTank();
                    timesToRemove.add(respawnTime);
                }
            }
        }
        
        // 移除已处理的重生时间
        tankRespawnTimes.removeAll(timesToRemove);
        
        // 更新现有坦克
        for (Iterator<Tank> iterator = enemyTanks.iterator(); iterator.hasNext(); ) {
            Tank enemy = iterator.next();
            
            // 检查敌方坦克是否被摧毁
            if (enemy.isDestroyed()) {
                iterator.remove();
                enemyTanksDestroyed++; // 增加已摧毁计数
                
                // 安排新坦克生成（3秒后）
                if (enemyTanksGenerated < totalEnemyTanksToGenerate) {
                    tankRespawnTimes.add(System.currentTimeMillis() + 3000);
                }
                
                System.out.println("敌方坦克被摧毁！剩余: " + enemyTanks.size() + 
                                  "，已摧毁: " + enemyTanksDestroyed + 
                                  "，总目标: " + totalEnemyTanksToGenerate);
                continue;
            }
            
            // 执行AI更新，传入网格和玩家坦克信息
            Bullet enemyBullet = enemy.updateAI(grid, playerTank, this);
            
            // 如果敌方坦克发射了子弹，则将其添加到子弹列表中
            if (enemyBullet != null) {
                bullets.add(enemyBullet);
            }
        }
    }
    
    /**
     * 初始化炮弹对象池
     */
    private void initBulletPool() {
        for (int i = 0; i < INITIAL_POOL_SIZE; i++) {
            bulletPool.add(new Bullet(0, 0, Tank.Direction.UP, "player_bullet", 0, 0, true));
        }
    }
    
    /**
     * 从对象池获取炮弹对象
     */
    private Bullet getBulletFromPool(int x, int y, Tank.Direction direction, 
                                  String bulletType, int speed, int damage, boolean fromPlayer) {
        Bullet bullet;
        if (bulletPool.isEmpty()) {
            bullet = new Bullet(x, y, direction, bulletType, speed, damage, fromPlayer);
        } else {
            bullet = bulletPool.remove(bulletPool.size() - 1);
            bullet.setX(x);
            bullet.setY(y);
            bullet.setDirection(direction);
            bullet.setBulletType(bulletType);
            bullet.setSpeed(speed);
            bullet.setDamage(damage);
            bullet.setFromPlayer(fromPlayer);
            bullet.setDestroyed(false);
        }
        return bullet;
    }
    
    /**
     * 回收炮弹对象到对象池
     */
    private void returnBulletToPool(Bullet bullet) {
        bulletPool.add(bullet);
    }
    
    /**
     * 检查碰撞
     * @return 返回碰撞的地形类型，如果没有碰撞则返回null
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
                    
                    // 获取当前时间
                    long currentTime = System.currentTimeMillis();
                    
                    // 检查是否已经过了冷却时间
                    if (currentTime - lastWaterDamageTime > WATER_DAMAGE_COOLDOWN) {
                        // 冷却时间已过，可以扣血并更新上次扣血时间
                        result = type;
                        lastWaterDamageTime = currentTime;
                        System.out.println("玩家进入水池并受到伤害！冷却时间开始计时"); // 调试输出
                    } else {
                        // 在冷却时间内，不扣血
                        System.out.println("玩家在水池中，但处于冷却时间内，不受伤害"); // 调试输出
                    }
                } else {
                    return type; // 如果是其他障碍物，直接返回
                }
            }
        }
        
        // 更新水池状态
        if (!foundWater && inWaterLastFrame) {
            System.out.println("玩家离开水池！"); // 调试输出
        }
        inWaterLastFrame = foundWater;
        
        return result; // 返回碰撞类型（可能是水池或null）
    }
    
    /**
     * 检查位置是否有效（不与障碍物重叠）
     */
    private boolean isPositionValid(int x, int y, int width, int height) {
        if (levelMap == null) return false;
        
        // 创建坦克占用的矩形区域
        Rectangle tankRect = new Rectangle(x, y, width, height);
        
        // 检查与地图元素的碰撞
        for (LevelMap.MapElement element : levelMap.getElements()) {
            String type = element.getType();
            
            // 跳过可以通过的元素（如草地）
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
     * 寻找有效的出生位置（修改后增加随机性）
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
     * @return 是否发生碰撞
     */
    public boolean checkEnemyPlayerCollisions() {
        if (playerTank == null) return false;
        
        boolean collision = false;
        Tank player = playerTank;
        
        // 创建玩家坦克的碰撞矩形
        Rectangle playerRect = new Rectangle(
            player.getX(), player.getY(), 
            player.getWidth(), player.getHeight()
        );
        
        // 使用迭代器以便安全删除
        Iterator<Tank> iterator = enemyTanks.iterator();
        while (iterator.hasNext()) {
            Tank enemy = iterator.next();
            
            // 创建敌方坦克的碰撞矩形
            Rectangle enemyRect = new Rectangle(
                enemy.getX(), enemy.getY(),
                enemy.getWidth(), enemy.getHeight()
            );
            
            // 检查碰撞
            if (playerRect.intersects(enemyRect.getBoundsInLocal())) {
                collision = true;
                
                // 摧毁敌方坦克
                iterator.remove();
                
                // 重要修复：增加击败敌人的计数
                enemyTanksDestroyed++;
                System.out.println("玩家坦克撞毁敌方坦克！击败敌人计数: " + enemyTanksDestroyed);
                
                // 添加到重生队列（如果需要）
                if (enemyTanksGenerated < totalEnemyTanksToGenerate) {
                    tankRespawnTimes.add(System.currentTimeMillis() + 3000);
                }
                
                // 对玩家坦克造成伤害
                player.takeDamage(1);
                
                // 检查玩家坦克是否被摧毁
                if (player.getHealth() <= 0) {
                    // 玩家坦克被摧毁的处理逻辑
                    return true;
                }
            }
        }
        
        return collision;
    }
    
    /**
     * 将一个坦克从另一个坦克推开一定距离
     */
    private void pushTankAway(Tank tankToPush, Tank referencePoint) {
        // 计算两个坦克中心点
        int tank1CenterX = tankToPush.getX() + tankToPush.getWidth() / 2;
        int tank1CenterY = tankToPush.getY() + tankToPush.getHeight() / 2;
        int tank2CenterX = referencePoint.getX() + referencePoint.getWidth() / 2;
        int tank2CenterY = referencePoint.getY() + referencePoint.getHeight() / 2;
        
        // 计算推力方向
        int pushDirX = tank1CenterX - tank2CenterX;
        int pushDirY = tank1CenterY - tank2CenterY;
        
        // 归一化方向
        double length = Math.sqrt(pushDirX * pushDirX + pushDirY * pushDirY);
        if (length > 0) {
            pushDirX = (int)(pushDirX / length * 20); // 推开20像素
            pushDirY = (int)(pushDirY / length * 20);
        } else {
            // 如果坦克中心完全重合，默认向右上推
            pushDirX = 20;
            pushDirY = -20;
        }
        
        // 应用推力
        int newX = tankToPush.getX() + pushDirX;
        int newY = tankToPush.getY() + pushDirY;
        
        // 确保坦克不会被推出地图边界
        newX = Math.max(0, Math.min(newX, levelMap.getWidth() - tankToPush.getWidth()));
        newY = Math.max(0, Math.min(newY, levelMap.getHeight() - tankToPush.getHeight()));
        
        tankToPush.setX(newX);
        tankToPush.setY(newY);
    }
    
    /**
     * 寻找玩家坦克的有效出生位置
     */
    public LevelMap.MapPosition findValidSpawnPosition() {
        return findValidPosition();
    }
    
    /**
     * 重新创建玩家坦克
     */
    public void respawnPlayerTank(String tankType, int x, int y) {
        // 将字符串转换为TankType枚举
        Tank.TankType type = Tank.TankType.valueOf(tankType.toUpperCase());
        
        // 创建全新的坦克对象
        playerTank = new Tank(type, x, y);
        
        // 确保坦克拥有完整血量
        playerTank.setHealth(playerTank.getMaxHealth());
        
        // 默认朝上
        playerTank.setDirection(Tank.Direction.UP);
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
                for (Iterator<Tank> tankIt = enemyTanks.iterator(); tankIt.hasNext();) {
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
                        // 玩家坦克受到伤害
                        if (playerTank.getHealth() > bullet.getDamage()) {
                            // 扣血但不致死
                            playerTank.setHealth(playerTank.getHealth() - bullet.getDamage());
                            System.out.println("玩家受到伤害! 当前生命: " + playerTank.getHealth());
                        } else {
                            // 玩家血量不足，生命值减一
                            playerTank.setHealth(0);
                            playerLostLife = true;
                            System.out.println("玩家坦克被摧毁!");
                        }
                        bulletHit = true;
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
                    System.out.println("砖块被子弹摧毁");
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
     * @param bullet 需要检查的子弹
     * @return 是否发生了碰撞
     */
    public boolean checkBulletTankCollisions(Bullet bullet) {
        // 子弹碰撞检测区域
        Rectangle bulletRect = new Rectangle(
            bullet.getX() - 2.5, // 子弹中心点调整
            bullet.getY() - 2.5,
            15, 15 // 略大的碰撞检测范围以提高游戏体验
        );
        
        boolean collisionDetected = false;
        
        if (bullet.isFromPlayer()) {
            // 玩家子弹与敌方坦克碰撞
            for (Iterator<Tank> tankIt = enemyTanks.iterator(); tankIt.hasNext();) {
                Tank enemyTank = tankIt.next();
                Rectangle tankRect = new Rectangle(
                    enemyTank.getX(), enemyTank.getY(),
                    enemyTank.getWidth(), enemyTank.getHeight()
                );
                
                if (bulletRect.intersects(tankRect.getBoundsInLocal())) {
                    // 敌方坦克受到伤害
                    boolean destroyed = enemyTank.takeDamage(bullet.getDamage());
                    
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
                    
                    bullet.destroy(); // 子弹被销毁
                    collisionDetected = true;
                    break;
                }
            }
        } else {
            // 敌方子弹与玩家坦克碰撞
            if (playerTank != null && !playerTank.isDestroyed()) {
                Rectangle tankRect = new Rectangle(
                    playerTank.getX(), playerTank.getY(),
                    playerTank.getWidth(), playerTank.getHeight()
                );
                
                if (bulletRect.intersects(tankRect.getBoundsInLocal())) {
                    // 玩家坦克受到伤害
                    boolean destroyed = playerTank.takeDamage(bullet.getDamage());
                    
                    if (destroyed) {
                        System.out.println("玩家坦克被摧毁！");
                    } else {
                        System.out.println("玩家受到伤害！当前生命值: " + playerTank.getHealth());
                    }
                    
                    bullet.destroy(); // 子弹被销毁
                    collisionDetected = true;
                }
            }
        }
        
        return collisionDetected;
    }

    /**
     * 获取当前地图
     * @return 当前关卡地图
     */
    public LevelMap getMap() {
        return levelMap;
    }

    /**
     * 获取指定类型的地图元素图片
     * @param elementType 元素类型
     * @return 元素对应的图片
     */
    public Image getElementImage(String elementType) {
        return elementImages.get(elementType);
    }

    // 配置不同关卡的敌方坦克参数
    private void configureEnemyTanksForLevel(int level) {
        switch (level) {
            case 1:
                totalEnemyTanksToGenerate = 15;
                maxConcurrentEnemies = 5;
                break;
            case 2:
                totalEnemyTanksToGenerate = 20;
                maxConcurrentEnemies = 5;
                break;
            case 3:
                totalEnemyTanksToGenerate = 25;
                maxConcurrentEnemies = 6;
                break;
            case 4:
                totalEnemyTanksToGenerate = 30;
                maxConcurrentEnemies = 6;
                break;
            case 5:
                totalEnemyTanksToGenerate = 35;
                maxConcurrentEnemies = 7;
                break;
            default:
                totalEnemyTanksToGenerate = 15;
                maxConcurrentEnemies = 5;
                break;
        }
    }

    // 修改生成新敌方坦克
    private void spawnNewEnemyTank() {
        // 找到一个有效的出生位置
        LevelMap.MapPosition spawnPos = findValidPosition();
        if (spawnPos != null) {
            // 确定坦克类型 - 只使用确保有图像的类型
            Tank.TankType tankType = Tank.TankType.BASIC; // 默认使用BASIC类型
            
            // 只从池中选择确定支持的类型
            if (!enemyTypesToGenerate.isEmpty()) {
                tankType = enemyTypesToGenerate.remove(0);
                
                // 确保类型是有效的
                if (tankType != Tank.TankType.BASIC && 
                    tankType != Tank.TankType.ELITE && 
                    tankType != Tank.TankType.BOSS) {
                    System.out.println("警告：无效的坦克类型 " + tankType + "，使用BASIC类型替代");
                    tankType = Tank.TankType.BASIC;
                }
            }
            
            // 验证图像是否加载
            String imageKey = "enemy_" + tankType.name().toLowerCase();
            if (!tankImages.containsKey(imageKey)) {
                System.out.println("警告：找不到坦克图像键 " + imageKey + "，使用BASIC类型替代");
                tankType = Tank.TankType.BASIC;
                imageKey = "enemy_basic";
            }
            
            // 再次检查图像是否存在
            Image[] images = tankImages.get(imageKey);
            if (images == null || images.length < 4 || images[0] == null) {
                System.out.println("严重错误：即使对基本类型也找不到有效图像！使用红色方块替代");
                // 在这种情况下，仍然创建坦克，但会在渲染时使用红色方块
                tankType = Tank.TankType.BASIC;
            }
            
            // 创建并添加新坦克
            Tank newTank = new Tank(tankType, spawnPos.getX(), spawnPos.getY());
            newTank.setDirection(Tank.Direction.UP); // 确保方向为UP以避免数组越界
            
            // 最终安全检查
            if (isPositionValid(spawnPos.getX(), spawnPos.getY(), 40, 40)) {
                enemyTanks.add(newTank);
                enemyTanksGenerated++;
                System.out.println("成功生成坦克: " + tankType + 
                                  " 在位置(" + spawnPos.getX() + "," + spawnPos.getY() + ")" +
                                  " 图像键: " + imageKey);
            } else {
                tankRespawnTimes.add(System.currentTimeMillis() + 1000);
            }
        } else {
            tankRespawnTimes.add(System.currentTimeMillis() + 1000);
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
}