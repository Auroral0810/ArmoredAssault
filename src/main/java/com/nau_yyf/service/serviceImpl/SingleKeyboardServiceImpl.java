package com.nau_yyf.service.serviceImpl;

import com.nau_yyf.controller.GameController;
import com.nau_yyf.controller.SingleGameController;
import com.nau_yyf.service.KeyboardService;
import com.nau_yyf.service.PlayerService;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;

/**
 * 单人游戏键盘监听服务实现
 */
public class SingleKeyboardServiceImpl implements KeyboardService {
    
    // 输入状态
    private final PlayerService.InputState inputState = new PlayerService.InputState();
    
    // 游戏控制器引用
    private SingleGameController singleGameController;
    
    // 回调函数
    private Runnable pauseCallback;
    private Runnable resumeCallback;

    /**
     * 通用的设置键盘控制方法
     */
    @Override
    public void setupKeyboardControls(GameController controller, 
                                     Canvas gameCanvas, 
                                     Runnable pauseCallback, 
                                     Runnable resumeCallback) {
        if (!(controller instanceof SingleGameController)) {
            throw new IllegalArgumentException("SingleKeyboardServiceImpl需要SingleGameController类型的控制器");
        }
        
        setupKeyboardControls((SingleGameController)controller, gameCanvas, pauseCallback, resumeCallback);
    }

    /**
     * 设置键盘控制 (为兼容性保留)
     */
    public void setupKeyboardControls(SingleGameController singleGameController,
                                      Canvas gameCanvas,
                                      Runnable pauseCallback,
                                      Runnable resumeCallback) {
        // 保存引用
        this.singleGameController = singleGameController;
        this.pauseCallback = pauseCallback;
        this.resumeCallback = resumeCallback;
        
        // 重置输入状态
        resetInputState();
        
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
            // 使用内部方法处理按键按下事件，参数从调用处传递
            handleKeyPressedInternal(e);
            e.consume(); // 阻止事件继续传播
        });
        
        gameCanvas.setOnKeyReleased(e -> {
            // 使用内部方法处理按键释放事件
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
            if (code.equals("ESCAPE")) {
                if (resumeCallback != null) {
                    resumeCallback.run();
                }
            }
            return inputState;
        }
        
        // 处理游戏按键
        if (!isGamePaused) {
            if (code.equals("UP") || code.equals("W")) {
                inputState.setUp(true);
            }
            if (code.equals("DOWN") || code.equals("S")) {
                inputState.setDown(true);
            }
            if (code.equals("LEFT") || code.equals("A")) {
                inputState.setLeft(true);
            }
            if (code.equals("RIGHT") || code.equals("D")) {
                inputState.setRight(true);
            }
            if (code.equals("SPACE")) {
                inputState.setFire(true);
            }
            if (code.equals("ESCAPE")) {
                if (pauseCallback != null) {
                    pauseCallback.run();
                }
            }
            // 添加E键放置炸弹
            if (code.equals("E")) {
                if (singleGameController != null) {
                    singleGameController.placeBomb();
                }
            }
        }
        
        return inputState;
    }
    
    /**
     * 处理按键释放事件
     */
    @Override
    public PlayerService.InputState handleKeyReleased(KeyEvent event) {
        String code = event.getCode().toString();
        
        if (code.equals("UP") || code.equals("W")) {
            inputState.setUp(false);
        }
        if (code.equals("DOWN") || code.equals("S")) {
            inputState.setDown(false);
        }
        if (code.equals("LEFT") || code.equals("A")) {
            inputState.setLeft(false);
        }
        if (code.equals("RIGHT") || code.equals("D")) {
            inputState.setRight(false);
        }
        if (code.equals("SPACE")) {
            inputState.setFire(false);
        }
        
        return inputState;
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
    private void resetInputState() {
        inputState.setUp(false);
        inputState.setDown(false);
        inputState.setLeft(false);
        inputState.setRight(false);
        inputState.setFire(false);
    }
    
    /**
     * 获取当前输入状态
     */
    public PlayerService.InputState getInputState() {
        return inputState;
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
