package com.nau_yyf.service;

import com.nau_yyf.controller.SingleGameController;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.KeyEvent;

/**
 * 键盘输入服务接口 - 负责键盘输入处理
 */
public interface KeyboardService {

    void setupKeyboardControls(Object gameController,
                               Canvas gameCanvas,
                               Runnable pauseCallback,
                               Runnable resumeCallback);

    /**
     * 设置键盘控制
     * 
     * @param singleGameController 游戏控制器
     * @param gameCanvas 游戏画布
     * @param pauseCallback 暂停回调
     * @param resumeCallback 恢复回调
     */
    void setupKeyboardControls(SingleGameController singleGameController,
                               Canvas gameCanvas,
                               Runnable pauseCallback,
                               Runnable resumeCallback);
    
    /**
     * 处理按键按下事件
     * 
     * @param event 键盘事件
     * @param isPauseMenuOpen 暂停菜单是否打开
     * @param isGamePaused 游戏是否暂停
     * @return 更新后的输入状态
     */
    PlayerService.InputState handleKeyPressed(KeyEvent event, 
                                             boolean isPauseMenuOpen, 
                                             boolean isGamePaused);
    
    /**
     * 处理按键释放事件
     * 
     * @param event 键盘事件
     * @return 更新后的输入状态
     */
    PlayerService.InputState handleKeyReleased(KeyEvent event);
    
    /**
     * 清除键盘控制
     * 
     * @param gameCanvas 游戏画布
     */
    void clearKeyboardControls(Canvas gameCanvas);
}
