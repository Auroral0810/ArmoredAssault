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
    
    public GameController() {
        // 预加载地图元素图片
        loadElementImages();
        
        // 预加载坦克图片
        loadTankImages();
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
            // 加载玩家坦克图片 - 每个方向一张图片,方向顺序: 上、右、下、左
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
                basicTank[i] = new Image(getClass().getResourceAsStream("/images/tanks/enemy/basic/" + i + ".png"));
                eliteTank[i] = new Image(getClass().getResourceAsStream("/images/tanks/enemy/elite/" + i + ".png"));
                bossTank[i] = new Image(getClass().getResourceAsStream("/images/tanks/enemy/boss/" + i + ".png"));
            }
            
            tankImages.put("enemy_basic", basicTank);
            tankImages.put("enemy_elite", eliteTank);
            tankImages.put("enemy_boss", bossTank);
            
        } catch (Exception e) {
            System.err.println("加载坦克图片失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void loadLevel(int level) {
        this.currentLevel = level;
        this.levelMap = MapLoader.loadLevel(level);
        System.out.println("加载关卡 " + level + ": " + (levelMap != null ? levelMap.getName() : "加载失败"));
        
        // 清空敌方坦克
        enemyTanks.clear();
        
        // 如果地图加载成功，创建敌方坦克
        if (levelMap != null && levelMap.getEnemies() != null) {
            for (LevelMap.EnemySpawn enemySpawn : levelMap.getEnemies()) {
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
                        }
                    } else {
                        Tank enemyTank = new Tank(type, pos.getX(), pos.getY());
                        enemyTanks.add(enemyTank);
                    }
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
        
        // 标记基地为不可行走
        if (levelMap.getPlayerBase() != null) {
            int baseX = levelMap.getPlayerBase().getX() / 40;
            int baseY = levelMap.getPlayerBase().getY() / 40;
            
            if (baseX >= 0 && baseX < gridWidth && baseY >= 0 && baseY < gridHeight) {
                grid[baseY][baseX] = true;
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
        
        // 渲染玩家基地
        LevelMap.MapPosition base = levelMap.getPlayerBase();
        if (base != null) {
            Image baseImg = elementImages.get("base");
            if (baseImg != null) {
                gc.drawImage(baseImg, base.getX(), base.getY(), base.getWidth(), base.getHeight());
            }

        }
        
        // 渲染敌方坦克
        for (Tank enemy : enemyTanks) {
            String imageKey = "enemy_" + enemy.getType().name().toLowerCase();
            Image[] tankImgs = tankImages.get(imageKey);
            if (tankImgs != null && enemy.getDirection().ordinal() < tankImgs.length) {
                gc.drawImage(tankImgs[enemy.getDirection().ordinal()], enemy.getX(), enemy.getY(), 40, 40);
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
            }
        }
        
        // 渲染子弹
        for (Bullet bullet : bullets) {
            // 加载子弹图像
            try {
                String bulletImagePath = bullet.getImagePath();
                Image bulletImage = new Image(getClass().getResourceAsStream(bulletImagePath));
                gc.drawImage(bulletImage, bullet.getX(), bullet.getY(), bullet.getWidth(), bullet.getHeight());
            } catch (Exception e) {
                // 如果加载失败，绘制一个简单的圆形
                gc.setFill(Color.WHITE);
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
        
        for (Iterator<Tank> iterator = enemyTanks.iterator(); iterator.hasNext(); ) {
            Tank enemy = iterator.next();
            
            // 检查敌方坦克是否被摧毁
            if (enemy.isDestroyed()) {
                iterator.remove();
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
        
        // 检查与基地的碰撞
        if (levelMap.getPlayerBase() != null) {
            LevelMap.MapPosition base = levelMap.getPlayerBase();
            Rectangle baseRect = new Rectangle(base.getX(), base.getY(), base.getWidth(), base.getHeight());
            
            if (tankRect.intersects(baseRect.getBoundsInLocal())) {
                return "base"; // 返回基地碰撞
            }
        }
        
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
        
        // 检查与基地的碰撞
        if (levelMap.getPlayerBase() != null) {
            LevelMap.MapPosition base = levelMap.getPlayerBase();
            Rectangle baseRect = new Rectangle(base.getX(), base.getY(), base.getWidth(), base.getHeight());
            
            if (tankRect.intersects(baseRect.getBoundsInLocal())) {
                return false; // 位置无效，与基地重叠
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
        
        // 尝试的位置按优先级：地图底部中间 -> 地图四个角 -> 随机位置
        int[][] candidatePositions = {
            {mapWidth / 2 - tankSize / 2, mapHeight - tankSize - 40},  // 底部中间
            {40, 40},                                                  // 左上角
            {mapWidth - tankSize - 40, 40},                            // 右上角
            {40, mapHeight - tankSize - 40},                           // 左下角
            {mapWidth - tankSize - 40, mapHeight - tankSize - 40}      // 右下角
        };
        
        // 先检查候选位置
        for (int[] pos : candidatePositions) {
            if (isPositionValid(pos[0], pos[1], tankSize, tankSize)) {
                LevelMap.MapPosition newPos = new LevelMap.MapPosition();
                newPos.setX(pos[0]);
                newPos.setY(pos[1]);
                newPos.setWidth(tankSize);
                newPos.setHeight(tankSize);
                return newPos;
            }
        }
        
        // 如果候选位置都不可用，尝试网格式搜索
        for (int y = 40; y < mapHeight - tankSize; y += 40) {
            for (int x = 40; x < mapWidth - tankSize; x += 40) {
                if (isPositionValid(x, y, tankSize, tankSize)) {
                    LevelMap.MapPosition newPos = new LevelMap.MapPosition();
                    newPos.setX(x);
                    newPos.setY(y);
                    newPos.setWidth(tankSize);
                    newPos.setHeight(tankSize);
                    return newPos;
                }
            }
        }
        
        // 如果还是找不到，最后尝试随机位置
        for (int attempt = 0; attempt < 50; attempt++) {
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
        
        // 实在找不到有效位置，打印错误日志
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
     * 检查并处理敌方坦克与玩家坦克碰撞
     * @return 如果发生碰撞返回true
     */
    public boolean checkEnemyPlayerCollisions() {
        if (playerTank == null) return false;
        
        boolean collisionDetected = false;
        List<Tank> tanksToRemove = new ArrayList<>();
        
        for (Tank enemyTank : enemyTanks) {
            if (checkTankCollision(playerTank, enemyTank)) {
                // 敌方坦克与玩家相撞，减少玩家生命值
                if (playerTank.getHealth() > 1) {
                    playerTank.setHealth(playerTank.getHealth() - 1);
                    System.out.println("玩家受到碰撞伤害! 当前生命: " + playerTank.getHealth());
                } else {
                    // 玩家血量降为0，需要扣除一条命
                    playerTank.setHealth(0);
                    System.out.println("玩家坦克被敌方坦克摧毁!");
                }
                
                // 标记敌方坦克为已摧毁
                tanksToRemove.add(enemyTank);
                collisionDetected = true;
                
                // 输出调试信息
                System.out.println("敌方坦克因碰撞被销毁!");
            }
        }
        
        // 移除被摧毁的敌方坦克
        enemyTanks.removeAll(tanksToRemove);
        
        return collisionDetected;
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
        // 这里可以实现一个计数器来记录被击败的敌人
        // 简化实现，可以用初始敌人数量减去当前剩余敌人数量
        return levelMap.getEnemies().size() - enemyTanks.size();
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
                            System.out.println("敌方坦克被摧毁！剩余: " + enemyTanks.size());
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
}