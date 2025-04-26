package com.nau_yyf.model;

import com.nau_yyf.util.AStarPathfinder;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

public class Tank {
    // 坦克类型枚举
    public enum TankType {
        LIGHT("light"), 
        STANDARD("standard"), 
        HEAVY("heavy"),
        BASIC("basic"),
        ELITE("elite"),
        BOSS("boss");
        
        private final String name;
        
        TankType(String name) {
            this.name = name;
        }
        
        public String getName() {
            return name;
        }
        
        public boolean isFriendly() {
            return this == LIGHT || this == STANDARD || this == HEAVY;
        }
        
        public static TankType fromString(String type) {
            for (TankType tankType : values()) {
                if (tankType.getName().equalsIgnoreCase(type)) {
                    return tankType;
                }
            }
            return STANDARD; // 默认返回标准类型
        }
    }
    
    // 增益效果枚举
    public enum PowerUpType {
        ATTACK("attack", "攻击增强", 10.0),
        BOMB("bomb", "炸弹", 0.0),
        HEALTH("health", "生命恢复", 15.0),
        INVINCIBILITY("invincibility", "无敌", 5.0),
        SHIELD("shield", "护盾", 20.0),
        SPEED("speed", "速度提升", 8.0);
        
        private final String name;
        private final String displayName;
        private final double duration; // 效果持续时间（秒）
        
        PowerUpType(String name, String displayName, double duration) {
            this.name = name;
            this.displayName = displayName;
            this.duration = duration;
        }
        
        public String getName() {
            return name;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public double getDuration() {
            return duration;
        }
    }
    
    // 方向枚举
    public enum Direction {
        UP(0), RIGHT(1), DOWN(2), LEFT(3);
        
        private final int value;
        
        Direction(int value) {
            this.value = value;
        }
        
        public int getValue() {
            return value;
        }
        
        public int getDx() {
            switch(this) {
                case LEFT: return -1;
                case RIGHT: return 1;
                default: return 0;
            }
        }
        
        public int getDy() {
            switch(this) {
                case UP: return -1;
                case DOWN: return 1;
                default: return 0;
            }
        }
        
        public static Direction fromValue(int value) {
            for (Direction dir : values()) {
                if (dir.getValue() == value) {
                    return dir;
                }
            }
            return UP; // 默认返回向上
        }
    }
    
    // 坦克基本属性
    private TankType type;
    private int x;
    private int y;
    private Direction direction;
    private int width = 40;
    private int height = 40;
    
    // 坦克性能参数
    private int health;
    private int maxHealth;
    private int speed;
    private int attackPower;
    private double fireRate; // 每秒攻击次数
    private double fireDelay; // 攻击冷却时间（毫秒）
    private long lastFireTime; // 上次攻击时间
    
    // 子弹参数
    private String bulletType;
    private int bulletSpeed;
    
    // 状态效果
    private Map<PowerUpType, Double> activeEffects = new HashMap<>(); // 效果->剩余时间（秒）
    private boolean isDestroyed = false;
    private boolean isShielded = false;
    private boolean isInvincible = false;
    
    // 坦克类型的默认参数（类型 -> [生命值, 速度, 攻击力, 攻速, 子弹速度]）
    private static final Map<TankType, int[]> DEFAULT_STATS = new HashMap<>();
    
    // 添加用于敌方坦克AI的相关属性和方法
    private List<AStarPathfinder.Node> pathToTarget;
    private int pathIndex;
    private long lastPathfindingTime;
    private static final long PATHFINDING_INTERVAL = 2000; // 每2秒重新计算路径
    
    // 新增字段
    private static final int DETECTION_RANGE = 350; // 坦克探测范围
    private long lastDirectionChangeTime = 0; // 上次改变随机方向的时间
    private long randomMoveDuration = 2000; // 随机移动持续时间，2-4秒
    private boolean isMoving = false; // 是否正在移动
    private long stopDuration = 0; // 停止移动的持续时间
    private long lastStopTime = 0; // 上次停止移动的时间
    
    static {
        // 友方坦克参数: 生命值, 速度, 攻击力, 攻速(每秒), 子弹
        DEFAULT_STATS.put(TankType.LIGHT, new int[]{3, 3, 1, 3, 3});    
        DEFAULT_STATS.put(TankType.STANDARD, new int[]{4, 2, 2, 2, 2});  
        DEFAULT_STATS.put(TankType.HEAVY, new int[]{5, 2, 3, 1, 2});     
        
        // 敌方坦克参数也相应降低
        DEFAULT_STATS.put(TankType.BASIC, new int[]{1, 1, 1, 1, 2});
        DEFAULT_STATS.put(TankType.ELITE, new int[]{2, 2, 2, 2, 3});
        DEFAULT_STATS.put(TankType.BOSS, new int[]{5, 2, 3, 2, 3});
    }
    
    // 构造函数
    public Tank(TankType type, int x, int y) {
        this.type = type;
        this.x = x;
        this.y = y;
        this.direction = Direction.UP;
        
        // 设置默认参数
        int[] stats = DEFAULT_STATS.get(type);
        this.maxHealth = stats[0];
        this.health = maxHealth;
        this.speed = stats[1];
        this.attackPower = stats[2];
        this.fireRate = stats[3];
        this.fireDelay = 1000.0 / fireRate;
        this.bulletSpeed = stats[4];
        
        // 设置子弹类型
        setBulletTypeBasedOnTankType();
    }
    
    // 根据坦克类型设置子弹类型
    private void setBulletTypeBasedOnTankType() {
        switch (type) {
            case LIGHT:
            case STANDARD:
            case HEAVY:
                this.bulletType = "player_bullet";
                break;
            case BASIC:
                this.bulletType = "enemy_basic_bullet";
                break;
            case ELITE:
                this.bulletType = "enemy_elite_bullet";
                break;
            case BOSS:
                this.bulletType = "enemy_boss_bullet";
                break;
        }
    }
    
    // 移动方法
    public void move() {
        int actualSpeed = isEffectActive(PowerUpType.SPEED) ? (int)(speed * 1.5) : speed;
        x += direction.getDx() * actualSpeed;
        y += direction.getDy() * actualSpeed;
    }
    
    // 转向方法
    public void setDirection(Direction direction) {
        this.direction = direction;
    }
    
    // 检查是否可以开火
    public boolean canFire() {
        long currentTime = System.currentTimeMillis();
        double actualFireDelay = isEffectActive(PowerUpType.ATTACK) ? fireDelay * 0.7 : fireDelay;
        return currentTime - lastFireTime >= actualFireDelay;
    }
    
    // 开火方法，返回创建的子弹
    public Bullet fire() {
        if (canFire()) {
            lastFireTime = System.currentTimeMillis();
            
            // 计算子弹生成位置（坦克中心）
            // 坦克宽高为40x40，子弹宽高为10x10，因此需要从坦克中心减去子弹尺寸的一半
            int bulletX = x + (width / 2) - 5; // 子弹宽度为10，所以一半是5
            int bulletY = y + (height / 2) - 5; // 子弹高度为10，所以一半是5
            
            // 根据方向调整初始位置，让子弹出现在坦克边缘而不是中心
            // 这样看起来更自然，好像子弹是从炮管射出的
            switch (direction) {
                case UP:
                    bulletY = y - 10; // 从坦克顶部发射
                    break;
                case DOWN:
                    bulletY = y + height; // 从坦克底部发射
                    break;
                case LEFT:
                    bulletX = x - 10; // 从坦克左侧发射
                    break;
                case RIGHT:
                    bulletX = x + width; // 从坦克右侧发射
                    break;
            }
            
            // 子弹攻击力根据增益效果可能增加
            int bulletDamage = isEffectActive(PowerUpType.ATTACK) ? 
                    (int)(attackPower * 1.5) : attackPower;
            
            return new Bullet(bulletX, bulletY, direction, bulletType, 
                    bulletSpeed, bulletDamage, type.isFriendly());
        }
        return null;
    }
    
    // 受到伤害
    public boolean takeDamage(int damage) {
        // 无敌或有护盾时不受伤害
        if (isInvincible || isShielded) {
            if (isShielded) {
                // 护盾被击中后移除
                removeEffect(PowerUpType.SHIELD);
                isShielded = false;
            }
            return false;
        }
        
        health -= damage;
        if (health <= 0) {
            health = 0;
            isDestroyed = true;
            return true; // 坦克被摧毁
        }
        return false;
    }
    
    // 使用增益道具
    public void applyPowerUp(PowerUpType powerUpType) {
        // 添加效果和持续时间
        activeEffects.put(powerUpType, powerUpType.getDuration());
        
        // 应用效果
        switch (powerUpType) {
            case HEALTH:
                // 生命恢复，但不超过最大生命值
                health = Math.min(maxHealth, health + 1);
                // 生命恢复是即时效果，不需要保持状态
                activeEffects.remove(PowerUpType.HEALTH);
                break;
            case SHIELD:
                isShielded = true;
                break;
            case INVINCIBILITY:
                isInvincible = true;
                break;
            case BOMB:
                // 炸弹是即时效果，不需要保持状态
                activeEffects.remove(PowerUpType.BOMB);
                // 炸弹效果在游戏控制器中处理
                break;
            // ATTACK和SPEED效果通过isEffectActive方法在相应功能中处理
        }
    }
    
    // 更新效果持续时间
    public void updateEffects(double deltaTime) {
        // 复制Map键集以避免并发修改异常
        for (PowerUpType effect : new HashMap<>(activeEffects).keySet()) {
            double remainingTime = activeEffects.get(effect) - deltaTime;
            if (remainingTime <= 0) {
                // 效果结束
                removeEffect(effect);
            } else {
                // 更新剩余时间
                activeEffects.put(effect, remainingTime);
            }
        }
    }
    
    // 移除效果
    private void removeEffect(PowerUpType effect) {
        activeEffects.remove(effect);
        
        // 重置相关状态
        switch (effect) {
            case SHIELD:
                isShielded = false;
                break;
            case INVINCIBILITY:
                isInvincible = false;
                break;
        }
    }
    
    // 检查效果是否激活
    public boolean isEffectActive(PowerUpType effect) {
        return activeEffects.containsKey(effect);
    }
    
    // 判断是否与其他对象碰撞
    public boolean collidesWith(GameObject other) {
        return x < other.getX() + other.getWidth() &&
               x + width > other.getX() &&
               y < other.getY() + other.getHeight() &&
               y + height > other.getY();
    }
    
    // Getters和Setters
    public TankType getType() {
        return type;
    }
    
    public String getTypeString() {
        return type.getName();
    }
    
    public boolean isFriendly() {
        return type.isFriendly();
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
    
    public Direction getDirection() {
        return direction;
    }
    
    public int getDirectionValue() {
        return direction.getValue();
    }
    
    public int getWidth() {
        return width;
    }
    
    public int getHeight() {
        return height;
    }
    
    public int getHealth() {
        return health;
    }
    
    public int getMaxHealth() {
        return maxHealth;
    }
    
    public void setHealth(int health) {
        this.health = Math.min(health, maxHealth);
    }
    
    public int getSpeed() {
        return isEffectActive(PowerUpType.SPEED) ? (int)(speed * 1.5) : speed;
    }
    
    public int getAttackPower() {
        return isEffectActive(PowerUpType.ATTACK) ? (int)(attackPower * 1.5) : attackPower;
    }
    
    public double getFireRate() {
        return isEffectActive(PowerUpType.ATTACK) ? fireRate * 1.3 : fireRate;
    }
    
    public String getBulletType() {
        return bulletType;
    }
    
    public boolean isDestroyed() {
        return isDestroyed;
    }
    
    public boolean isShielded() {
        return isShielded;
    }
    
    public boolean isInvincible() {
        return isInvincible;
    }
    
    public Map<PowerUpType, Double> getActiveEffects() {
        return new HashMap<>(activeEffects);
    }
    
    // 新增方法，返回不含特效的基础速度
    public int getBaseSpeed() {
        return speed;
    }
    
    // AI行为方法 - 修改后加入探测范围和随机移动
    public Bullet updateAI(boolean[][] grid, Tank playerTank) {
        Bullet firedBullet = null; // 用于返回发射的子弹
        
        if (!isFriendly()) { // 只对敌方坦克执行AI
            long currentTime = System.currentTimeMillis();
            
            // 计算与玩家坦克的距离
            double distance = calculateDistance(playerTank);
            
            // 如果玩家在探测范围内，使用A*寻路追踪玩家
            if (distance <= DETECTION_RANGE) {
                // 每隔一段时间重新计算路径
                if (pathToTarget == null || pathToTarget.isEmpty() || 
                    currentTime - lastPathfindingTime > PATHFINDING_INTERVAL) {
                    
                    // 计算网格单元格大小 (假设地图元素是40x40)
                    int cellSize = 40;
                    
                    // 将坦克位置转换为网格坐标
                    int startX = x / cellSize;
                    int startY = y / cellSize;
                    int targetX = playerTank.getX() / cellSize;
                    int targetY = playerTank.getY() / cellSize;
                    
                    // 确保坐标在网格范围内
                    if (startX >= 0 && startY >= 0 && targetX >= 0 && targetY >= 0 && 
                        startX < grid[0].length && startY < grid.length && 
                        targetX < grid[0].length && targetY < grid.length) {
                        // 查找路径
                        pathToTarget = AStarPathfinder.findPath(grid, startX, startY, targetX, targetY);
                        pathIndex = 0;
                        lastPathfindingTime = currentTime;
                    }
                }
                
                // 如果有路径且还未到达终点，沿着路径移动
                if (pathToTarget != null && !pathToTarget.isEmpty() && pathIndex < pathToTarget.size()) {
                    AStarPathfinder.Node nextNode = pathToTarget.get(pathIndex);
                    
                    // 计算网格单元格大小
                    int cellSize = 40;
                    
                    // 使用getter方法访问节点坐标
                    int targetX = nextNode.getX() * cellSize + cellSize/2 - width/2;
                    int targetY = nextNode.getY() * cellSize + cellSize/2 - height/2;
                    
                    // 确定移动方向
                    if (Math.abs(targetX - x) > Math.abs(targetY - y)) {
                        // 水平移动
                        if (targetX > x) {
                            setDirection(Direction.RIGHT);
                        } else {
                            setDirection(Direction.LEFT);
                        }
                    } else {
                        // 垂直移动
                        if (targetY > y) {
                            setDirection(Direction.DOWN);
                        } else {
                            setDirection(Direction.UP);
                        }
                    }
                    
                    // 执行移动
                    move();
                    
                    // 检查是否已经到达当前节点
                    if (Math.abs(x - targetX) < speed && Math.abs(y - targetY) < speed) {
                        pathIndex++;
                    }
                    
                    // 在追踪玩家过程中，更频繁地尝试开火（30%概率）
                    if (Math.random() < 0.3 && canFire()) {
                        firedBullet = fire(); // 保存发射的子弹，而不只是调用fire()
                    }
                }
            } else {
                // 玩家不在探测范围内，执行随机移动，可能返回子弹
                firedBullet = updateRandomMovement(currentTime, grid);
            }
        }
        
        return firedBullet; // 返回发射的子弹，如果没有发射则返回null
    }
    
    // 计算与另一个坦克的距离
    private double calculateDistance(Tank otherTank) {
        // 计算两个坦克中心点的距离
        int centerX1 = x + width / 2;
        int centerY1 = y + height / 2;
        int centerX2 = otherTank.getX() + otherTank.getWidth() / 2;
        int centerY2 = otherTank.getY() + otherTank.getHeight() / 2;
        
        // 欧几里得距离公式
        return Math.sqrt(Math.pow(centerX2 - centerX1, 2) + Math.pow(centerY2 - centerY1, 2));
    }
    
    // 修改随机移动方法，返回可能发射的子弹
    private Bullet updateRandomMovement(long currentTime, boolean[][] grid) {
        // 如果当前正在停止状态
        if (!isMoving) {
            // 如果停止时间已结束
            if (currentTime - lastStopTime > stopDuration) {
                isMoving = true; // 开始移动
                // 随机选择一个新方向
                setRandomDirection();
                // 设置新的移动持续时间（2-4秒）
                randomMoveDuration = 2000 + (long)(Math.random() * 2000);
                lastDirectionChangeTime = currentTime;
            }
            return null; // 如果仍在停止状态，直接返回null
        }
        
        // 正在移动中
        // 检查是否需要改变方向或停止
        if (currentTime - lastDirectionChangeTime > randomMoveDuration) {
            // 有20%的几率停止移动
            if (Math.random() < 0.2) {
                isMoving = false;
                stopDuration = 500 + (long)(Math.random() * 1500); // 停止0.5-2秒
                lastStopTime = currentTime;
                return null;
            } else {
                // 否则改变方向
                setRandomDirection();
                randomMoveDuration = 2000 + (long)(Math.random() * 2000); // 2-4秒
                lastDirectionChangeTime = currentTime;
            }
        }
        
        // 移动前检查是否会碰到障碍物
        int nextX = x;
        int nextY = y;
        
        // 根据当前方向计算下一个位置
        switch (direction) {
            case UP:
                nextY -= speed;
                break;
            case DOWN:
                nextY += speed;
                break;
            case LEFT:
                nextX -= speed;
                break;
            case RIGHT:
                nextX += speed;
                break;
        }
        
        // 检查是否会超出边界
        int gridWidth = grid[0].length * 40;
        int gridHeight = grid.length * 40;
        
        if (nextX < 0 || nextX + width > gridWidth || nextY < 0 || nextY + height > gridHeight) {
            // 如果会超出边界，改变方向
            setRandomDirection();
            lastDirectionChangeTime = currentTime;
            return null;
        }
        
        // 检查是否会碰到障碍物
        int cellX = nextX / 40;
        int cellY = nextY / 40;
        
        // 考虑坦克所占据的多个格子
        boolean willCollide = false;
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++) {
                int checkX = cellX + i;
                int checkY = cellY + j;
                
                if (checkX < grid[0].length && checkY < grid.length && 
                    checkX >= 0 && checkY >= 0 && grid[checkY][checkX]) {
                    willCollide = true;
                    break;
                }
            }
            if (willCollide) break;
        }
        
        if (willCollide) {
            // 如果会碰到障碍物，改变方向
            setRandomDirection();
            lastDirectionChangeTime = currentTime;
            return null;
        }
        
        // 执行移动
        move();
        
        // 随机尝试开火（10%概率）
        if (Math.random() < 0.1 && canFire()) {
            return fire(); // 返回发射的子弹
        }
        
        return null; // 如果没有发射子弹就返回null
    }
    
    // 随机设置方向
    private void setRandomDirection() {
        // 随机选择一个方向: 0=上, 1=右, 2=下, 3=左
        int randomDir = (int)(Math.random() * 4);
        setDirection(Direction.fromValue(randomDir));
    }
}
