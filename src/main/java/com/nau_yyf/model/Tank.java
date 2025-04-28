package com.nau_yyf.model;

import com.nau_yyf.controller.SingleGameController;
import com.nau_yyf.util.AStarPathfinder;
import javafx.scene.canvas.GraphicsContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
            switch (this) {
                case LEFT:
                    return -1;
                case RIGHT:
                    return 1;
                default:
                    return 0;
            }
        }

        public int getDy() {
            switch (this) {
                case UP:
                    return -1;
                case DOWN:
                    return 1;
                default:
                    return 0;
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
    private double speed;
    private int attackPower;
    private double fireRate; // 每秒攻击次数
    private double fireDelay; // 攻击冷却时间（毫秒）
    private long lastFireTime; // 上次攻击时间

    // 子弹参数
    private String bulletType;
    private double bulletSpeed;

    // 状态效果
    private Map<PowerUpType, Double> activeEffects = new HashMap<>(); // 效果->剩余时间（秒）
    private boolean isDestroyed = false;
    private boolean isShielded = false;
    private boolean isInvincible = false;

    // 坦克类型的默认参数（类型 -> [生命值, 速度, 攻击力, 攻速, 子弹速度]）
    private static final Map<TankType, double[]> DEFAULT_STATS = new HashMap<>();

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


    // 添加新的成员变量来标记坦克是否刚刚生成
    private boolean initialSpawnDelay = false;
    private long spawnTime = 0;
    private static final long SPAWN_PROTECTION_TIME = 500; // 500毫秒的出生保护时间

    // 在Tank类的成员变量区域添加以下内容
    private boolean isRespawnInvincible = false;
    private long respawnInvincibleStartTime = 0;
    private static final long RESPAWN_INVINCIBLE_DURATION = 5000; // 5秒无敌时间
    private boolean respawnBlinkVisible = true; // 控制闪烁显示状态
    private long lastBlinkTime = 0; // 上次闪烁时间

    // 在Tank类中添加一个标记，记录上一帧是否在水中
    private boolean inWaterLastFrame = false;

    // 在类的成员变量区域添加以下变量
    private double acceleration; // 加速度
    private double deceleration; // 减速度
    private double currentSpeed; // 当前速度
    private double maxSpeed; // 最大速度
    private boolean isAccelerating = false; // 是否正在加速
    private double[] lastMotion = new double[]{0, 0}; // 上一帧的运动方向 [x方向, y方向]

    static {
        // 友方坦克参数: 生命值, 基础速度, 攻击力, 攻速(每秒), 子弹速度, 加速度, 最大速度, 减速度
        DEFAULT_STATS.put(TankType.LIGHT, new double[]{3, 2, 1, 3, 3, 0.3, 3.2, 0.5});
        DEFAULT_STATS.put(TankType.STANDARD, new double[]{4, 1.6, 2, 2, 2, 0.2, 2.8, 0.4});
        DEFAULT_STATS.put(TankType.HEAVY, new double[]{5, 1.3, 3, 1, 2, 0.1, 2.4, 0.3});

        // 敌方坦克参数: 减小速度相关参数
        DEFAULT_STATS.put(TankType.BASIC, new double[]{2, 0.7, 1, 1, 2, 0.05, 1.0, 0.2});
        DEFAULT_STATS.put(TankType.ELITE, new double[]{4, 0.9, 2, 2, 3, 0.08, 1.2, 0.3});
        DEFAULT_STATS.put(TankType.BOSS, new double[]{7, 1.1, 3, 2, 3, 0.1, 1.4, 0.25});
    }

    // 构造函数
    public Tank(TankType type, int x, int y) {
        this.type = type;
        this.x = x;
        this.y = y;
        this.direction = Direction.UP;

        // 设置默认参数
        double[] stats = DEFAULT_STATS.get(type);
        this.maxHealth = (int) stats[0];
        this.health = maxHealth;
        this.speed = stats[1];
        this.attackPower = (int) stats[2];
        this.fireRate = stats[3];
        this.fireDelay = 1000.0 / fireRate;
        this.bulletSpeed = stats[4];
        this.acceleration = stats[5];
        this.maxSpeed = stats[6];
        this.deceleration = stats[7];

        // 敌方坦克初始就有速度，友方坦克则需要玩家按键加速
        if (!type.isFriendly()) {
            this.currentSpeed = maxSpeed * 0.3; // 从50%减小到30%
            this.isMoving = true; // 敌方坦克默认移动状态为true
        } else {
            this.currentSpeed = 0; // 玩家坦克初始速度为0
        }

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

    // move方法
    public void move(SingleGameController singleGameController) {
        // 如果坦克已经死亡，不允许移动
        if (isDead()) return;

        // 重置上一帧水池状态
        inWaterLastFrame = false;

        // 计算加速度和最大速度
        double effectiveMaxSpeed = isEffectActive(PowerUpType.SPEED) ? maxSpeed * 1.5 : maxSpeed;
        double effectiveAcceleration = isEffectActive(PowerUpType.SPEED) ? acceleration * 1.2 : acceleration;

        // 更新速度
        if (isAccelerating) {
            // 加速
            currentSpeed = Math.min(effectiveMaxSpeed, currentSpeed + effectiveAcceleration);
        } else {
            // 减速
            currentSpeed = Math.max(0, currentSpeed - deceleration);
        }

        // 如果速度几乎为0，直接设为0
        if (currentSpeed < 0.1) {
            currentSpeed = 0;
        }

        // 如果没有速度，不移动
        if (currentSpeed <= 0) {
            return;
        }

        // 计算下一个位置
        double nextX = x;
        double nextY = y;

        // 保存运动方向
        double dirX = 0;
        double dirY = 0;

        switch (direction) {
            case UP:
                nextY -= currentSpeed;
                dirY = -1;
                break;
            case DOWN:
                nextY += currentSpeed;
                dirY = 1;
                break;
            case LEFT:
                nextX -= currentSpeed;
                dirX = -1;
                break;
            case RIGHT:
                nextX += currentSpeed;
                dirX = 1;
                break;
        }

        // 更新运动方向
        lastMotion[0] = dirX;
        lastMotion[1] = dirY;

        // 边界检查
        int mapWidth = (singleGameController.getMap() != null) ? singleGameController.getMap().getWidth() : 800;
        int mapHeight = (singleGameController.getMap() != null) ? singleGameController.getMap().getHeight() : 600;

        if (nextX < 0) {
            nextX = 0;
            currentSpeed *= 0.5; // 碰到边界减速
        } else if (nextX > mapWidth - width) {
            nextX = mapWidth - width;
            currentSpeed *= 0.5; // 碰到边界减速
        }

        if (nextY < 0) {
            nextY = 0;
            currentSpeed *= 0.5; // 碰到边界减速
        } else if (nextY > mapHeight - height) {
            nextY = mapHeight - height;
            currentSpeed *= 0.5; // 碰到边界减速
        }

        // 检查碰撞
        String collisionType = singleGameController.checkCollision((int) nextX, (int) nextY, width, height);

        if (collisionType == null) {
            // 无碰撞，正常移动
            x = (int) nextX;
            y = (int) nextY;
        } else if (collisionType.equals("water")) {
            // 水池，移动但需要减速和检查状态
            x = (int) nextX;
            y = (int) nextY;
            inWaterLastFrame = true;

            // 水池中移动减速
            currentSpeed *= 0.7;

            // 敌方坦克不会受到水池伤害
            if (!isFriendly()) {
                return;
            }

            // 检查是否处于任何无敌状态
            if (isRespawnInvincible() || isInvincible) {
                return; // 无敌状态不受伤害，直接返回
            }

            // 护盾可以抵挡水池伤害
            if (isShielded) {
                
                removeEffect(PowerUpType.SHIELD);
                isShielded = false;
                return;
            }

            // 不在无敌状态下
            health--;

            // 检查是否死亡
            if (health <= 0) {
                isDestroyed = true;
            }
        } else {
            // 其他障碍物碰撞，减速或停止
            currentSpeed *= 0.2; // 碰到障碍物大幅减速
        }
    }

    public void move() {
        // 如果坦克已经死亡，不允许移动
        if (isDead()) return;

        double actualSpeed = isEffectActive(PowerUpType.SPEED) ? speed * 1.5 : speed;
        // 使用Math.round转换为int以减少精度损失
        setX(getX() + (int) Math.round(direction.getDx() * actualSpeed));
        setY(getY() + (int) Math.round(direction.getDy() * actualSpeed));
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
        // 如果坦克已经死亡，不允许射击
        if (isDead()) return null;

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

            // 子弹攻击力增益 - 增加50%伤害
            int bulletDamage = isEffectActive(PowerUpType.ATTACK) ?
                    attackPower * 2 : attackPower;

            return new Bullet(bulletX, bulletY, direction, bulletType,
                    bulletSpeed, bulletDamage, type.isFriendly());
        }
        return null;
    }

    // 受到伤害（优化逻辑判断，加入出生保护检查）
    public boolean takeDamage(int damage) {
        // 如果坦克已经死亡，不再处理伤害
        if (isDead()) return false;

        // 无敌状态不受伤害（所有类型的无敌状态）
        if (isRespawnInvincible() || isInvincible || isInSpawnProtection()) {
            if (isRespawnInvincible()) {
                
            } else if (isInSpawnProtection()) {
                
            } else {
                
            }
            return false;
        }

        // 护盾可以抵挡一次伤害，然后消失
        if (isShielded) {
            
            removeEffect(PowerUpType.SHIELD);
            isShielded = false;
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
                // 生命恢复1
                health = Math.min(maxHealth, health + 1);
                activeEffects.remove(PowerUpType.HEALTH);
                break;
            case SHIELD:
                isShielded = true;
                break;
            case INVINCIBILITY:
                isInvincible = true;
                break;
            case BOMB:
                break;
            // ATTACK和SPEED效果通过isEffectActive方法在相应功能中处理
        }
    }

    // 添加一个新的 applyPowerUp 方法重载，允许直接设置剩余时间
    public void applyPowerUp(PowerUpType powerUpType, double remainingTime) {
        // 设置指定的剩余时间
        activeEffects.put(powerUpType, remainingTime);

        // 应用效果
        switch (powerUpType) {
            case HEALTH:
                // 生命恢复1
                health = Math.min(maxHealth, health + 1);
                break;
            case SHIELD:
                isShielded = true;
                break;
            case INVINCIBILITY:
                isInvincible = true;
                break;
            case BOMB:
                break;
            // ATTACK和SPEED效果通过isEffectActive方法在相应功能中处理
        }
    }

    // 更新效果持续时间
    public void updateEffects(double deltaTime) {
        // 复制Map键集以避免并发修改异常
        for (PowerUpType effect : new HashMap<>(activeEffects).keySet()) {
            double remainingTime = activeEffects.get(effect);

            // 如果是生命恢复的特殊标记，立即移除
            if (effect == PowerUpType.HEALTH && remainingTime < 0) {
                activeEffects.remove(effect);
                continue;
            }

            remainingTime -= deltaTime;
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
    public void removeEffect(PowerUpType effect) {
        activeEffects.remove(effect);

        // 重置相关状态
        switch (effect) {
            case SHIELD:
                isShielded = false;
                break;
            case INVINCIBILITY:
                isInvincible = false;
                break;
            case BOMB:
                // 特殊处理炸弹效果，无需额外操作
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

    public double getSpeed() {
        return isEffectActive(PowerUpType.SPEED) ? speed * 1.5 : speed;
    }

    public double getAttackPower() {
        return isEffectActive(PowerUpType.ATTACK) ? attackPower * 1.5 : attackPower;
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
    public double getBaseSpeed() {
        return speed;
    }

    // AI行为方法
    public Bullet updateAI(boolean[][] grid, Tank playerTank, SingleGameController singleGameController) {
        // 如果处于出生保护状态，不执行AI逻辑
        if (isInSpawnProtection()) {
            return null;
        }

        // 添加空值检查
        if (grid == null || playerTank == null || singleGameController == null) {
            // 如果关键参数为空，执行简单的随机移动
            setRandomDirection();
            if (singleGameController != null) {
                move(singleGameController);
            }
            return null;
        }

        // 检查是否是首次更新（从存档加载后）
        if (pathToTarget == null && lastPathfindingTime == 0) {
            // 强制立即进行路径计算
            lastPathfindingTime = 0;
            // 确保坦克处于移动状态
            isMoving = true;
            setAccelerating(true);
        }

        Bullet firedBullet = null;

        if (!isFriendly()) {
            long currentTime = System.currentTimeMillis();

            // 敌方坦克始终处于移动状态
            isMoving = true;

            // 在执行移动前，设置加速状态
            setAccelerating(true);

            // 检查玩家坦克是否在草丛中
            boolean playerInGrass = isPlayerInGrass(playerTank, singleGameController);

            // 计算与玩家坦克的距离
            double distance = calculateDistance(playerTank);

            // 如果玩家在探测范围内且不在草丛中，使用A*寻路追踪玩家
            if (distance <= DETECTION_RANGE && !playerInGrass) {
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
                    AStarPathfinder.Node lastNode = pathToTarget.get(pathToTarget.size() - 1);
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


                        // 使用A*算法查找路径
                        List<AStarPathfinder.Node> newPath = AStarPathfinder.findPath(
                                grid, startX, startY, targetX, targetY);

                        if (newPath != null && !newPath.isEmpty()) {
                            pathToTarget = newPath;
                            pathIndex = 0;
                        } else {
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
                    int targetCenterX = nextNode.getX() * cellSize + cellSize / 2;
                    int targetCenterY = nextNode.getY() * cellSize + cellSize / 2;

                    // 计算坦克中心点
                    int tankCenterX = x + width / 2;
                    int tankCenterY = y + height / 2;

                    // 计算坦克中心点到目标中心点的距离
                    double nodeDistance = Math.sqrt(
                            Math.pow(targetCenterX - tankCenterX, 2) +
                                    Math.pow(targetCenterY - tankCenterY, 2));

                    // 判断是否已经达到当前路径点（使用更精确的距离判断）
                    if (nodeDistance < currentSpeed) {
                        // 已经足够接近这个路径点，移动到下一个
                        pathIndex++;

                        // 如果已经是最后一个点，重新计算路径
                        if (pathIndex >= pathToTarget.size()) {
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
                        if (singleGameController != null) {
                            move(singleGameController);
                        } else {
                            move();
                        }

                        // 检查是否有实际移动
                        if (oldX == x && oldY == y) {
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
                    moveDirectlyTowardsPlayer(playerTank, singleGameController);

                    // 也尝试开火
                    if (Math.random() < 0.2 && canFire()) {
                        firedBullet = fire();
                    }
                }
            } else {
                // 玩家不在探测范围内或在草丛中，执行随机移动
                firedBullet = updateRandomMovementWithCollision(currentTime, grid, singleGameController);
            }
        }

        // 添加自我修复机制：如果坦克移动不正常，重置AI状态
        if (!isFriendly() && currentSpeed < 0.1 && isAccelerating) {
            resetAIState();  // 重置AI状态
            currentSpeed = maxSpeed * 0.4;  // 从70%减小到40%
        }

        return firedBullet;
    }

    // 优化moveDirectlyTowardsPlayer方法，增加卡住检测
    private void moveDirectlyTowardsPlayer(Tank playerTank, SingleGameController singleGameController) {
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
        if (singleGameController != null) {
            move(singleGameController);
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

                    if (singleGameController != null) {
                        move(singleGameController);
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
    private Bullet updateRandomMovementWithCollision(long currentTime, boolean[][] grid, SingleGameController singleGameController) {
        // 添加对grid的空值检查
        if (grid == null) {
            // 如果grid为空，执行简单的随机移动而不检查网格碰撞
            setRandomDirection();
            if (singleGameController != null) {
                move(singleGameController);
            } else {
                move();
            }
            return null;
        }

        // 确保敌方坦克保持加速
        setAccelerating(true);

        // 如果当前正在停止状态
        if (!isMoving) {
            // 如果停止时间已结束
            if (currentTime - lastStopTime > stopDuration) {
                isMoving = true; // 开始移动
                setAccelerating(true); // 确保设置加速状态
                // 随机选择一个新方向
                setRandomDirection();
                // 设置新的移动持续时间（2-4秒）
                randomMoveDuration = 2000 + (long) (Math.random() * 2000);
                lastDirectionChangeTime = currentTime;
            } else {
                setAccelerating(false); // 停止状态下不加速
            }
            return null; // 如果仍在停止状态，直接返回null
        }

        // 正在移动中，确保加速状态为true
        setAccelerating(true);

        // 检查是否需要改变方向或停止
        if (currentTime - lastDirectionChangeTime > randomMoveDuration) {
            // 有20%的几率停止移动
            if (Math.random() < 0.2) {
                isMoving = false;
                stopDuration = 500 + (long) (Math.random() * 1500); // 停止0.5-2秒
                lastStopTime = currentTime;
                return null;
            } else {
                // 否则改变方向
                setRandomDirection();
                randomMoveDuration = 2000 + (long) (Math.random() * 2000); // 2-4秒
                lastDirectionChangeTime = currentTime;
            }
        }

        // 移动前检查是否会碰到障碍物
        double nextX = x;
        double nextY = y;

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
        int cellX = (int) Math.floor(nextX / 40);
        int cellY = (int) Math.floor(nextY / 40);

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
        move(singleGameController);

        // 随机尝试开火（10%概率）
        if (Math.random() < 0.1 && canFire()) {
            return fire(); // 返回发射的子弹
        }

        return null; // 如果没有发射子弹就返回null
    }

    // 随机设置方向
    private void setRandomDirection() {
        // 随机选择一个方向: 0=上, 1=右, 2=下, 3=左
        int randomDir = (int) (Math.random() * 4);
        setDirection(Direction.fromValue(randomDir));
    }

    /**
     * 设置坦克是否被摧毁
     */
    public void setDestroyed(boolean destroyed) {
        this.isDestroyed = destroyed;
    }


    /**
     * 设置坦克初始出生延迟
     */
    public void setInitialSpawnDelay(boolean delay) {
        this.initialSpawnDelay = delay;
        if (delay) {
            this.spawnTime = System.currentTimeMillis();
        }
    }

    /**
     * 检查坦克是否在出生保护期
     */
    public boolean isInSpawnProtection() {
        if (!initialSpawnDelay) {
            return false;
        }

        long currentTime = System.currentTimeMillis();
        if (currentTime - spawnTime > SPAWN_PROTECTION_TIME) {
            initialSpawnDelay = false; // 出生保护时间已过
            return false;
        }
        return true;
    }

    /**
     * 检查坦克是否死亡
     */
    public boolean isDead() {
        return health <= 0 || isDestroyed;
    }

    /**
     * 设置复活无敌状态
     */
    public void setRespawnInvincible(boolean invincible) {
        this.isRespawnInvincible = invincible;
        if (invincible) {
            respawnInvincibleStartTime = System.currentTimeMillis();
            respawnBlinkVisible = true;
            lastBlinkTime = respawnInvincibleStartTime;
        }
    }

    /**
     * 检查是否处于复活无敌状态
     */
    public boolean isRespawnInvincible() {
        if (!isRespawnInvincible) return false;

        long currentTime = System.currentTimeMillis();
        if (currentTime - respawnInvincibleStartTime > RESPAWN_INVINCIBLE_DURATION) {
            isRespawnInvincible = false; // 无敌时间结束
            return false;
        }

        // 控制闪烁效果（每200毫秒切换一次可见性）
        if (currentTime - lastBlinkTime > 200) {
            respawnBlinkVisible = !respawnBlinkVisible;
            lastBlinkTime = currentTime;
        }

        return true;
    }

    /**
     * 获取复活无敌状态下的可见性（用于闪烁效果）
     */
    public boolean isVisibleDuringRespawnInvincible() {
        return !isRespawnInvincible || respawnBlinkVisible;
    }

    /**
     * 获取复活无敌剩余时间（毫秒）
     */
    public long getRespawnInvincibleRemainingTime() {
        if (!isRespawnInvincible) return 0;
        return Math.max(0, RESPAWN_INVINCIBLE_DURATION -
                (System.currentTimeMillis() - respawnInvincibleStartTime));
    }

    // 为标记添加getter方法
    public boolean isInWaterLastFrame() {
        return inWaterLastFrame;
    }

    // 添加设置/获取加速状态的方法
    public void setAccelerating(boolean accelerating) {
        this.isAccelerating = accelerating;
    }

    public boolean isAccelerating() {
        return isAccelerating;
    }

    // 获取当前速度
    public double getCurrentSpeed() {
        return currentSpeed;
    }

    // 在Tank类中添加一个重置AI状态的方法
    public void resetAIState() {
        // 重置路径寻找相关数据
        pathToTarget = null;
        pathIndex = 0;

        // 重置时间戳，确保立即重新计算路径
        lastPathfindingTime = 0;
        lastDirectionChangeTime = System.currentTimeMillis();

        // 重置移动状态
        isMoving = true;
        setAccelerating(true);

        // 敌方坦克设置更低的初始速度
        if (!isFriendly()) {
            currentSpeed = maxSpeed * 0.3; // 从70%减小到40%
        }

        // 随机选择一个方向
        setRandomDirection();

        // 重置随机移动的相关参数
        randomMoveDuration = 2000 + (long) (Math.random() * 2000);
        stopDuration = 0;
        lastStopTime = 0;
    }

    // 修改判断玩家是否在草丛中的方法
    private boolean isPlayerInGrass(Tank playerTank, SingleGameController singleGameController) {
        // 获取玩家坦克位置和尺寸
        int playerX = playerTank.getX();
        int playerY = playerTank.getY();
        int playerWidth = playerTank.getWidth();
        int playerHeight = playerTank.getHeight();

        // 创建玩家坦克的矩形区域
        java.awt.Rectangle playerRect = new java.awt.Rectangle(playerX, playerY, playerWidth, playerHeight);

        // 获取地图元素并检查是否与草丛重叠
        if (singleGameController.getMap() != null) {
            for (LevelMap.MapElement element : singleGameController.getMap().getElements()) {
                // 只检查草丛类型的元素
                if (element.getType().equals("grass")) {
                    java.awt.Rectangle grassRect = new java.awt.Rectangle(
                            element.getX(), element.getY(),
                            element.getWidth(), element.getHeight()
                    );

                    // 使用java.awt.Rectangle的intersects方法
                    if (playerRect.intersects(grassRect)) {
                        return true;
                    }
                }
            }
        }

        // 没有与任何草丛重叠，返回false
        return false;
    }

    /**
     * 渲染坦克到画布
     * @param gc 图形上下文
     */
    public void render(GraphicsContext gc) {
        if (!isVisibleDuringRespawnInvincible()) {
            return;  // 如果在无敌状态闪烁期间不可见，则不渲染
        }
        
        // 设置坦克颜色
        if (isFriendly()) {
            gc.setFill(javafx.scene.paint.Color.BLUE);  // 友方坦克为蓝色
        } else {
            gc.setFill(javafx.scene.paint.Color.RED);   // 敌方坦克为红色
        }
        
        // 绘制坦克主体
        gc.fillRect(x, y, width, height);
        
        // 绘制坦克炮塔
        double turretWidth = width * 0.5;
        double turretHeight = height * 0.2;
        
        double turretX = x + (width - turretWidth) / 2;
        double turretY = y;
        
        // 根据方向调整炮塔位置
        switch (direction) {
            case UP:
                turretY = y - turretHeight;
                break;
            case DOWN:
                turretY = y + height;
                break;
            case LEFT:
                turretWidth = width * 0.2;
                turretHeight = height * 0.5;
                turretX = x - turretWidth;
                turretY = y + (height - turretHeight) / 2;
                break;
            case RIGHT:
                turretWidth = width * 0.2;
                turretHeight = height * 0.5;
                turretX = x + width;
                turretY = y + (height - turretHeight) / 2;
                break;
        }
        
        gc.fillRect(turretX, turretY, turretWidth, turretHeight);
        
        // 如果有护盾则绘制
        if (isShielded()) {
            gc.setStroke(javafx.scene.paint.Color.CYAN);
            gc.setLineWidth(2);
            gc.strokeOval(x - 5, y - 5, width + 10, height + 10);
        }
        
        // 如果有无敌效果则绘制
        if (isInvincible()) {
            gc.setStroke(javafx.scene.paint.Color.GOLD);
            gc.setLineWidth(2);
            gc.strokeOval(x - 3, y - 3, width + 6, height + 6);
        }
        
        // 绘制血量条
        double healthPercentage = (double) health / maxHealth;
        double healthBarWidth = width * healthPercentage;
        
        gc.setFill(javafx.scene.paint.Color.RED);
        gc.fillRect(x, y - 8, width, 5);
        
        gc.setFill(javafx.scene.paint.Color.GREEN);
        gc.fillRect(x, y - 8, healthBarWidth, 5);
    }
}
