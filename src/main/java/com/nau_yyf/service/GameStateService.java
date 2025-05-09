package com.nau_yyf.service;

import com.nau_yyf.controller.GameController;
import java.io.File;

/**
 * 游戏状态服务接口 - 负责游戏存档、加载和分数计算
 */
public interface GameStateService {
    
    /**
     * 保存游戏状态
     * 
     * @param controller 游戏控制器
     * @param saveName 存档名称
     * @return 保存是否成功
     */
    boolean saveGame(GameController controller, String saveName);
    
    /**
     * 加载游戏状态
     * 
     * @param controller 游戏控制器
     * @param saveFile 存档文件
     * @return 加载是否成功
     */
    boolean loadGame(GameController controller, File saveFile);
    
    /**
     * 重新开始当前关卡
     * 
     * @param controller 游戏控制器
     * @param tankType 坦克类型
     * @param level 关卡
     */
    void restartGame(GameController controller, String tankType, int level);
    
    /**
     * 计算游戏得分
     * 
     * @param level 当前关卡
     * @param defeatedEnemies 已击败敌人数量
     * @param totalGameTime 总游戏时间
     * @param playerLives 剩余玩家生命
     * @return 计算出的得分
     */
    int calculateScore(int level, int defeatedEnemies, long totalGameTime, int playerLives);
    
    /**
     * 检查关卡是否完成
     * 
     * @param controller 游戏控制器
     * @return 关卡是否完成
     */
    boolean isLevelCompleted(GameController controller);
    
    /**
     * 清理游戏资源
     * 
     * @param controller 游戏控制器
     */
    void cleanupGameResources(GameController controller);
    
    /**
     * 获取游戏得分
     * @param controller 游戏控制器
     * @return 游戏得分
     */
    int getScore(GameController controller);
}
