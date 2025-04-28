package com.nau_yyf.view;

import com.nau_yyf.controller.GameController;
import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;

/**
 * 游戏屏幕通用接口
 * 所有游戏模式的屏幕都应实现此接口
 */
public interface GameScreen {
    /**
     * 显示游戏屏幕
     * @param controller 游戏控制器
     */
    void show(GameController controller);
    
    /**
     * 获取游戏画布
     */
    Canvas getGameCanvas();
    
    /**
     * 获取游戏数据面板
     */
    HBox getGameDataPanel();
    
    /**
     * 设置游戏数据面板
     */
    void setGameDataPanel(HBox panel);
    
    /**
     * 获取游戏循环
     */
    AnimationTimer getGameLoop();

    /**
     * 设置游戏循环
     */
    void setGameLoop(AnimationTimer gameLoop);
    
    /**
     * 获取时间信息文本组件
     * @return 时间信息文本组件
     */
    Text getTimeInfo();
    
    /**
     * 设置时间信息文本组件
     * @param timeInfo 时间信息文本组件
     */
    void setTimeInfo(Text timeInfo);
    
    /**
     * 获取游戏总时间
     */
    long getTotalGameTime();
    
    /**
     * 设置游戏总时间
     */
    void setTotalGameTime(long time);
    
    /**
     * 获取上次更新时间
     */
    long getLastUpdateTime();
    
    /**
     * 设置上次更新时间
     */
    void setLastUpdateTime(long time);
    
    /**
     * 获取游戏是否暂停
     */
    boolean isGamePaused();
    
    /**
     * 设置游戏暂停状态
     */
    void setGamePaused(boolean paused);
    
    /**
     * 获取暂停菜单是否打开
     */
    boolean isPauseMenuOpen();
    
    /**
     * 设置暂停菜单打开状态
     */
    void setIsPauseMenuOpen(boolean open);
    
    /**
     * 清理游戏资源
     */
    void cleanupGameResources();
    
    /**
     * 获取玩家生命值
     * @return 玩家生命值
     */
    int getPlayerLives();
    
    /**
     * 设置玩家生命值
     * @param lives 生命值
     */
    void setPlayerLives(int lives);
    
    /**
     * 获取子弹数量
     * @return 子弹数量
     */
    int getBulletCount();
    
    /**
     * 设置子弹数量
     * @param count 子弹数量
     */
    void setBulletCount(int count);
    
    /**
     * 获取上次子弹补充时间
     * @return 上次子弹补充时间(毫秒)
     */
    long getLastBulletRefillTime();
    
    /**
     * 设置上次子弹补充时间
     * @param time 时间戳
     */
    void setLastBulletRefillTime(long time);
} 