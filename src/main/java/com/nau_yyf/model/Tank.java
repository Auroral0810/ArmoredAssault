package com.nau_yyf.model;

import com.nau_yyf.controller.GameController;
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
    private static final int DETECTION_RANGE = 600; // 坦克探测范围
    private long lastDirectionChangeTime = 0; // 上次改变随机方向的时间
    private long randomMoveDuration = 2000; // 随机移动持续时间，2-4秒
    private boolean isMoving = false; // 是否正在移动
    private long stopDuration = 0; // 停止移动的持续时间
    private long lastStopTime = 0; // 上次停止移动的时间
    
    // 在Tank类中添加以下变量用于跟踪坦克是否被卡住
    private int lastX = -1;
    private int lastY = -1;
    private long lastMovementTime = 0;
    private static final long STUCK_THRESHOLD = 2000; // 2秒卡住阈值
    
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
    
    // 修改move方法，添加碰撞检测
    public void move(GameController gameController) {
        int actualSpeed = isEffectActive(PowerUpType.SPEED) ? (int)(speed * 1.5) : speed;
        
        // 计算下一个位置
        int nextX = x;
        int nextY = y;
        
        switch (direction) {
            case UP:
                nextY -= actualSpeed;
                break;
            case DOWN:
                nextY += actualSpeed;
                break;
            case LEFT:
                nextX -= actualSpeed;
                break;
            case RIGHT:
                nextX += actualSpeed;
                break;
        }
        
        // 检查碰撞
        String collisionType = gameController.checkCollision(nextX, nextY, width, height);
        
        if (collisionType == null) {
            // 无碰撞，正常移动
            x = nextX;
            y = nextY;
        } else if (collisionType.equals("water")) {
            // 水池，移动但扣血（由于修改了checkCollision方法，这里只会在刚进入水池时触发）
            x = nextX;
            y = nextY;
            
            // 每次进入水池扣1点血，但不致死
            if (health > 1) {
                health--;
                System.out.println("玩家进入水池，失去1点生命值！当前生命值：" + health);
            }
        }
        // 其他情况（砖墙、钢墙）不移动
    }
    
    // 保留原来的无参move方法以兼容AI代码
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
        long currentTime = System.currentTimeMillis();
        
        // 如果坦克类型是敌人且冷却时间不足，则不允许发射
        if (!type.isFriendly() && currentTime - lastFireTime < fireDelay) {
            return null;
        }
        
        // 设置不同的冷却时间
        if (!type.isFriendly()) {
            switch (type) {
                case BASIC:
                    fireDelay = 3000; // 基础敌人3秒1发
                    break;
                case ELITE:
                    fireDelay = 2500; // 精英敌人2.5秒1发
                    break;
                case BOSS:
                    fireDelay = 2000; // Boss敌人2秒1发
                    break;
                default:
                    fireDelay = 3000;
            }
        }
        
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
    public Bullet updateAI(boolean[][] grid, Tank playerTank, GameController gameController) {
        Bullet firedBullet = null;
        
        if (!isFriendly()) {
            long currentTime = System.currentTimeMillis();
            
            // 计算与玩家坦克的距离
            double distance = calculateDistance(playerTank);
            
            // 调试输出
            if (Math.random() < 0.01) {
                System.out.println("敌方坦克(" + getType() + ")到玩家距离: " + distance + 
                                 ", 探测范围: " + DETECTION_RANGE);
            }
            
            // 如果玩家在探测范围内，使用A*寻路追踪玩家
            if (distance <= DETECTION_RANGE) {
                boolean needRecalculatePath = false;
                
                // 检查是否需要重新计算路径
                if (pathToTarget == null || pathToTarget.isEmpty()) {
                    needRecalculatePath = true;
                } else if (currentTime - lastPathfindingTime > PATHFINDING_INTERVAL) {
                    // 如果已经过了重新计算路径的间隔时间
                    needRecalculatePath = true;
                } else if (pathIndex >= pathToTarget.size()) {
                    // 如果已经到达了路径的终点
                    needRecalculatePath = true;
                } else {
                    // 检查玩家是否移动很远，需要更新路径
                    AStarPathfinder.Node lastNode = pathToTarget.get(pathToTarget.size()-1);
                    int targetGridX = playerTank.getX() / 40;
                    int targetGridY = playerTank.getY() / 40;
                    
                    if (Math.abs(lastNode.getX() - targetGridX) > 2 || 
                        Math.abs(lastNode.getY() - targetGridY) > 2) {
                        needRecalculatePath = true;
                    }
                }
                
                // 重新计算路径
                if (needRecalculatePath) {
                    int cellSize = 40; // 网格单元格大小
                    
                    // 将坦克位置转换为网格坐标
                    int startX = x / cellSize;
                    int startY = y / cellSize;
                    int targetX = playerTank.getX() / cellSize;
                    int targetY = playerTank.getY() / cellSize;
                    
                    // 边界检查
                    if (grid != null && grid.length > 0 && grid[0].length > 0) {
                        int gridWidth = grid[0].length;
                        int gridHeight = grid.length;
                        
                        startX = Math.max(0, Math.min(startX, gridWidth - 1));
                        startY = Math.max(0, Math.min(startY, gridHeight - 1));
                        targetX = Math.max(0, Math.min(targetX, gridWidth - 1));
                        targetY = Math.max(0, Math.min(targetY, gridHeight - 1));
                        
                        System.out.println("计算从(" + startX + "," + startY + ")到(" + targetX + "," + targetY + ")的A*路径");
                        
                        // 使用A*算法查找路径
                        List<AStarPathfinder.Node> newPath = AStarPathfinder.findPath(
                            grid, startX, startY, targetX, targetY);
                        
                        if (newPath != null && !newPath.isEmpty()) {
                            pathToTarget = newPath;
                            pathIndex = 0;
                            System.out.println("找到包含" + newPath.size() + "个节点的路径");
                            
                            // 打印路径以便调试
                            for (int i = 0; i < newPath.size(); i++) {
                                AStarPathfinder.Node node = newPath.get(i);
                                System.out.println("路径点 " + i + ": (" + node.getX() + "," + node.getY() + ")");
                            }
                        } else {
                            System.out.println("A*算法未找到路径，切换到直接移动");
                            pathToTarget = null;
                        }
                        lastPathfindingTime = currentTime;
                    }
                }
                
                // 如果有路径，严格按照路径移动
                if (pathToTarget != null && !pathToTarget.isEmpty() && pathIndex < pathToTarget.size()) {
                    AStarPathfinder.Node nextNode = pathToTarget.get(pathIndex);
                    
                    // 计算网格中节点的中心点像素坐标
                    int cellSize = 40;
                    int targetCenterX = nextNode.getX() * cellSize + cellSize/2;
                    int targetCenterY = nextNode.getY() * cellSize + cellSize/2;
                    
                    // 计算坦克中心点
                    int tankCenterX = x + width/2;
                    int tankCenterY = y + height/2;
                    
                    // 计算坦克中心点到目标中心点的距离
                    double nodeDistance = Math.sqrt(
                        Math.pow(targetCenterX - tankCenterX, 2) + 
                        Math.pow(targetCenterY - tankCenterY, 2));
                    
                    // 判断是否已经达到当前路径点（使用更精确的距离判断）
                    if (nodeDistance < speed) {
                        // 已经足够接近这个路径点，移动到下一个
                        pathIndex++;
                        System.out.println("到达路径点" + (pathIndex-1) + "，前进到下一点: " + pathIndex);
                        
                        // 如果已经是最后一个点，重新计算路径
                        if (pathIndex >= pathToTarget.size()) {
                            System.out.println("已达到路径终点，将在下一次更新重新计算路径");
                        }
                    } else {
                        // 还没到达当前路径点，继续移动
                        // 计算移动方向
                        if (Math.abs(targetCenterX - tankCenterX) > Math.abs(targetCenterY - tankCenterY)) {
                            // 水平移动
                            if (targetCenterX > tankCenterX) {
                                setDirection(Direction.RIGHT);
                            } else {
                                setDirection(Direction.LEFT);
                            }
                        } else {
                            // 垂直移动
                            if (targetCenterY > tankCenterY) {
                                setDirection(Direction.DOWN);
                            } else {
                                setDirection(Direction.UP);
                            }
                        }
                        
                        // 移动前记录当前位置
                        int oldX = x;
                        int oldY = y;
                        
                        // 执行移动
                        if (gameController != null) {
                            move(gameController);
                        } else {
                            move();
                        }
                        
                        // 检查是否有实际移动
                        if (oldX == x && oldY == y) {
                            // 坦克被卡住，无法朝当前方向移动
                            System.out.println("坦克被卡住，无法移动到路径点" + pathIndex);
                            
                            // 尝试更改方向或重新计算路径
                            lastPathfindingTime = 0; // 强制下次更新重新计算路径
                        }
                        
                        // 在追踪玩家过程中尝试开火
                        if (Math.random() < 0.3 && canFire()) {
                            firedBullet = fire();
                        }
                    }
                } else {
                    // 没有有效路径，直接向玩家移动
                    moveDirectlyTowardsPlayer(playerTank, gameController);
                    
                    // 也尝试开火
                    if (Math.random() < 0.2 && canFire()) {
                        firedBullet = fire();
                    }
                }
            } else {
                // 玩家不在探测范围内，执行随机移动
                firedBullet = updateRandomMovementWithCollision(currentTime, grid, gameController);
            }
        }
        
        return firedBullet;
    }
    
    // 优化moveDirectlyTowardsPlayer方法，增加卡住检测
    private void moveDirectlyTowardsPlayer(Tank playerTank, GameController gameController) {
        // 记录移动前的位置
        int oldX = x;
        int oldY = y;
        
        // 计算玩家中心点
        int playerCenterX = playerTank.getX() + playerTank.getWidth() / 2;
        int playerCenterY = playerTank.getY() + playerTank.getHeight() / 2;
        
        // 计算敌方坦克中心点
        int tankCenterX = x + width / 2;
        int tankCenterY = y + height / 2;
        
        // 确定移动方向
        if (Math.abs(playerCenterX - tankCenterX) > Math.abs(playerCenterY - tankCenterY)) {
            // 水平移动
            if (playerCenterX > tankCenterX) {
                setDirection(Direction.RIGHT);
            } else {
                setDirection(Direction.LEFT);
            }
        } else {
            // 垂直移动
            if (playerCenterY > tankCenterY) {
                setDirection(Direction.DOWN);
            } else {
                setDirection(Direction.UP);
            }
        }
        
        // 执行移动
        if (gameController != null) {
            move(gameController);
        } else {
            move();
        }
        
        // 检查是否被卡住
        if (oldX == x && oldY == y) {
            // 被卡住了，尝试其他方向
            Direction[] alternateDirections = {
                Direction.UP, Direction.RIGHT, Direction.DOWN, Direction.LEFT
            };
            
            for (Direction dir : alternateDirections) {
                if (dir != direction) { // 尝试不同于当前方向的方向
                    setDirection(dir);
                    
                    if (gameController != null) {
                        move(gameController);
                    } else {
                        move();
                    }
                    
                    // 如果移动成功，跳出循环
                    if (x != oldX || y != oldY) {
                        break;
                    }
                }
            }
        }
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
    
    // 添加带碰撞检测的随机移动方法
    private Bullet updateRandomMovementWithCollision(long currentTime, boolean[][] grid, GameController gameController) {
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
        move(gameController);
        
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
    
    /**
     * 设置坦克是否被摧毁
     */
    public void setDestroyed(boolean destroyed) {
        this.isDestroyed = destroyed;
    }
    
    // 添加一个新方法来强制坦克脱离卡住状态
    private void forceUnstuck(GameController gameController) {
        // 尝试所有4个方向，按优先级：上、右、下、左
        Direction[] directionsToTry = {
            Direction.UP, Direction.RIGHT, Direction.DOWN, Direction.LEFT
        };
        
        // 随机打乱方向顺序，增加多样性
        shuffleDirections(directionsToTry);
        
        // 依次尝试每个方向
        for (Direction dir : directionsToTry) {
            // 计算该方向移动后的位置
            int newX = x;
            int newY = y;
            
            switch (dir) {
                case UP: newY -= speed; break;
                case DOWN: newY += speed; break;
                case LEFT: newX -= speed; break;
                case RIGHT: newX += speed; break;
            }
            
            // 检查这个方向是否可以移动
            if (gameController == null || 
                gameController.checkCollision(newX, newY, width, height) == null ||
                gameController.checkCollision(newX, newY, width, height).equals("water")) {
                
                // 这个方向可以移动，设置方向并移动
                setDirection(dir);
                if (gameController != null) {
                    move(gameController);
                } else {
                    move();
                }
                
                // 如果成功移动，更新最后移动时间和位置
                lastMovementTime = System.currentTimeMillis();
                lastX = x;
                lastY = y;
                System.out.println("坦克成功脱离卡住状态，移动方向: " + dir);
                return;
            }
        }
        
        // 如果所有方向都不能移动，尝试更激进的移动（多走几步）
        for (Direction dir : directionsToTry) {
            // 设置方向
            setDirection(dir);
            
            // 尝试连续移动3次，有可能能走出卡住的区域
            for (int i = 0; i < 3; i++) {
                if (gameController != null) {
                    move(gameController);
                } else {
                    move();
                }
                
                // 如果位置已经改变，说明成功移动
                if (x != lastX || y != lastY) {
                    lastMovementTime = System.currentTimeMillis();
                    lastX = x;
                    lastY = y;
                    System.out.println("坦克通过激进移动脱离卡住状态");
                    return;
                }
            }
        }
    }
    
    // 辅助方法：随机打乱方向数组
    private void shuffleDirections(Direction[] directions) {
        for (int i = directions.length - 1; i > 0; i--) {
            int index = (int) (Math.random() * (i + 1));
            // 交换元素
            Direction temp = directions[index];
            directions[index] = directions[i];
            directions[i] = temp;
        }
    }
}
