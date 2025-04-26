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
                    Tank enemyTank = new Tank(type, pos.getX(), pos.getY());
                    enemyTanks.add(enemyTank);
                }
            }
        }
        
        // 创建玩家坦克（如果地图加载成功）
        if (levelMap != null && levelMap.getPlayerSpawn() != null) {
            if (playerTank == null) {
                // 默认使用STANDARD坦克
                playerTank = new Tank(Tank.TankType.STANDARD, levelMap.getPlayerSpawn().getX(), levelMap.getPlayerSpawn().getY());
            } else {
                // 只更新位置
                playerTank.setX(levelMap.getPlayerSpawn().getX());
                playerTank.setY(levelMap.getPlayerSpawn().getY());
            }
        }
        
        // 初始化网格
        initializeGrid();
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
        if (levelMap != null && levelMap.getEnemies() != null) {
            return levelMap.getEnemies().size();
        }
        return 0;
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
    
    // 修改updateEnemyTanks方法，收集并添加敌方坦克发射的子弹
    public void updateEnemyTanks() {
        if (playerTank == null) return;
        
        for (Tank enemy : enemyTanks) {
            // 执行AI更新，并获取可能发射的子弹
            Bullet enemyBullet = enemy.updateAI(grid, playerTank);
            
            // 如果敌方坦克发射了子弹，则将其添加到子弹列表中
            if (enemyBullet != null) {
                bullets.add(enemyBullet);
                System.out.println("敌方坦克发射了子弹: " + enemyBullet.getBulletType());
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
}