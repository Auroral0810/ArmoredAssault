package com.nau_yyf.service.serviceImpl;

import com.nau_yyf.controller.GameController;
import com.nau_yyf.controller.MultiGameController;
import com.nau_yyf.service.KeyboardService;
import com.nau_yyf.service.PlayerService;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;

/**
 * @author auroral
 * @date 2025/4/28 03:31
 */
public class MultiKeyboardServiceImpl implements KeyboardService {

    // 玩家1输入状态
    private final PlayerService.InputState player1InputState = new PlayerService.InputState();
    
    // 玩家2输入状态
    private final PlayerService.InputState player2InputState = new PlayerService.InputState();
    
    // 游戏控制器引用
    private MultiGameController multiGameController;
    
    // 回调函数
    private Runnable pauseCallback;
    private Runnable resumeCallback;
    
    /**
     * 设置键盘控制 (通用方法)
     */
    @Override
    public void setupKeyboardControls(GameController controller,
                                      Canvas gameCanvas,
                                      Runnable pauseCallback,
                                      Runnable resumeCallback) {
        if (!(controller instanceof MultiGameController)) {
            throw new IllegalArgumentException("MultiKeyboardServiceImpl需要MultiGameController类型的控制器");
        }
        
        MultiGameController multiController = (MultiGameController) controller;
        
        // 保存引用
        this.multiGameController = multiController;
        this.pauseCallback = pauseCallback;
        this.resumeCallback = resumeCallback;
        
        // 重置输入状态
        resetInputStates();
        
        // 清除之前的监听器
        clearKeyboardControls(gameCanvas);
        
        // 获取场景和根布局
        Scene scene = gameCanvas.getScene();
        StackPane root = (StackPane) gameCanvas.getParent();
        
        // 清除场景上存在的监听器
        if (scene != null) {
            scene.setOnKeyPressed(null);
            scene.setOnKeyReleased(null);
        }
        
        // 清除根布局上存在的监听器
        if (root != null) {
            root.setOnKeyPressed(null);
            root.setOnKeyReleased(null);
        }
        
        // 在画布级别添加新的监听器
        gameCanvas.setOnKeyPressed(e -> {
            handleKeyPressedInternal(e);
            e.consume(); // 阻止事件继续传播
        });
        
        gameCanvas.setOnKeyReleased(e -> {
            handleKeyReleasedInternal(e);
            e.consume(); // 阻止事件继续传播
        });
        
        // 确保画布可以获取焦点
        gameCanvas.setFocusTraversable(true);
    }

    /**
     * 处理按键按下事件
     */
    @Override
    public PlayerService.InputState handleKeyPressed(KeyEvent event, 
                                                 boolean isPauseMenuOpen, 
                                                 boolean isGamePaused) {
        String code = event.getCode().toString();
        
        // 如果暂停菜单已打开
        if (isPauseMenuOpen) {
            if (code.equals("ESCAPE") || code.equals("P")) {
                if (resumeCallback != null) {
                    resumeCallback.run();
                }
            }
            return player1InputState; // 返回玩家1状态，但此时暂停菜单打开，不会处理游戏输入
        }
        
        // 处理游戏按键
        if (!isGamePaused) {
            // 玩家1控制 (WASD + 空格 + E)
            if (code.equals("W")) {
                player1InputState.setUp(true);
            }
            if (code.equals("S")) {
                player1InputState.setDown(true);
            }
            if (code.equals("A")) {
                player1InputState.setLeft(true);
            }
            if (code.equals("D")) {
                player1InputState.setRight(true);
            }
            if (code.equals("SPACE")) {
                player1InputState.setFire(true);
            }
            if (code.equals("E")) {
                if (multiGameController != null) {
                    // 临时注释掉，直到MultiGameController实现此方法
                    // multiGameController.placeP1Bomb();
                    System.out.println("玩家1尝试放置炸弹");
                }
            }
            
            // 玩家2控制 (方向键 + 回车 + J)
            if (code.equals("UP")) {
                player2InputState.setUp(true);
            }
            if (code.equals("DOWN")) {
                player2InputState.setDown(true);
            }
            if (code.equals("LEFT")) {
                player2InputState.setLeft(true);
            }
            if (code.equals("RIGHT")) {
                player2InputState.setRight(true);
            }
            if (code.equals("ENTER")) {
                player2InputState.setFire(true);
            }
            if (code.equals("J")) {
                if (multiGameController != null) {
                    // 临时注释掉，直到MultiGameController实现此方法
                    // multiGameController.placeP2Bomb();
                    System.out.println("玩家2尝试放置炸弹");
                }
            }
            
            // 暂停游戏 (ESC 或 P)
            if (code.equals("ESCAPE") || code.equals("P")) {
                if (pauseCallback != null) {
                    pauseCallback.run();
                }
            }
        }
        
        return player1InputState; // 接口要求返回一个InputState，我们返回玩家1的状态
    }
    
    /**
     * 处理按键释放事件
     */
    @Override
    public PlayerService.InputState handleKeyReleased(KeyEvent event) {
        String code = event.getCode().toString();
        
        // 玩家1控制 (WASD + 空格)
        if (code.equals("W")) {
            player1InputState.setUp(false);
        }
        if (code.equals("S")) {
            player1InputState.setDown(false);
        }
        if (code.equals("A")) {
            player1InputState.setLeft(false);
        }
        if (code.equals("D")) {
            player1InputState.setRight(false);
        }
        if (code.equals("SPACE")) {
            player1InputState.setFire(false);
        }
        
        // 玩家2控制 (方向键 + 回车)
        if (code.equals("UP")) {
            player2InputState.setUp(false);
        }
        if (code.equals("DOWN")) {
            player2InputState.setDown(false);
        }
        if (code.equals("LEFT")) {
            player2InputState.setLeft(false);
        }
        if (code.equals("RIGHT")) {
            player2InputState.setRight(false);
        }
        if (code.equals("ENTER")) {
            player2InputState.setFire(false);
        }
        
        return player1InputState; // 接口要求返回一个InputState，我们返回玩家1的状态
    }
    
    /**
     * 清除键盘控制
     */
    @Override
    public void clearKeyboardControls(Canvas gameCanvas) {
        if (gameCanvas != null) {
            gameCanvas.setOnKeyPressed(null);
            gameCanvas.setOnKeyReleased(null);
            
            // 移除所有键盘事件处理器
            gameCanvas.removeEventHandler(KeyEvent.KEY_PRESSED, event -> {});
            gameCanvas.removeEventHandler(KeyEvent.KEY_RELEASED, event -> {});
        }
    }
    
    /**
     * 重置输入状态
     */
    private void resetInputStates() {
        // 重置玩家1输入状态
        player1InputState.setUp(false);
        player1InputState.setDown(false);
        player1InputState.setLeft(false);
        player1InputState.setRight(false);
        player1InputState.setFire(false);
        
        // 重置玩家2输入状态
        player2InputState.setUp(false);
        player2InputState.setDown(false);
        player2InputState.setLeft(false);
        player2InputState.setRight(false);
        player2InputState.setFire(false);
    }
    
    /**
     * 获取玩家1输入状态
     */
    public PlayerService.InputState getPlayer1InputState() {
        return player1InputState;
    }
    
    /**
     * 获取玩家2输入状态
     */
    public PlayerService.InputState getPlayer2InputState() {
        return player2InputState;
    }
    
    /**
     * 内部方法：处理按键按下事件
     */
    private void handleKeyPressedInternal(KeyEvent event) {
        // 这里让此方法委托给接口方法，便于子类重写
        handleKeyPressed(event, false, false);
    }
    
    /**
     * 内部方法：处理按键释放事件
     */
    private void handleKeyReleasedInternal(KeyEvent event) {
        // 这里让此方法委托给接口方法，便于子类重写
        handleKeyReleased(event);
    }

}