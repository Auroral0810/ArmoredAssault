package com.nau_yyf.service.serviceImpl;

import com.nau_yyf.controller.GameController;
import com.nau_yyf.service.GameStateService;
import com.nau_yyf.view.GameView;
import com.nau_yyf.view.singleGame.SinglePlayerGameScreen;
import javafx.application.Platform;
import javafx.scene.canvas.Canvas;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

/**
 * 单人游戏状态服务实现类
 * 负责单人游戏模式下的游戏状态管理
 */
public class SingleGameStateServiceImpl implements GameStateService {

    private GameView gameView;
    private Stage stage;

    public SingleGameStateServiceImpl(GameView gameView, Stage stage) {
        this.gameView = gameView;
        this.stage = stage;
    }

    /**
     * 保存游戏状态
     */
    @Override
    public boolean saveGame(GameController gameController, String saveName) {
        if (gameController == null)
            return false;

        // 调用GameController的保存方法
        return gameController.saveGame(saveName);
    }

    /**
     * 加载游戏状态
     */
    @Override
    public boolean loadGame(GameController gameController, File saveFile) {
        if (gameController == null || saveFile == null)
            return false;

        // 调用GameController的加载方法
        return gameController.loadGame(saveFile);
    }

    /**
     * 显示加载游戏对话框并加载选择的存档
     * 这是一个额外的辅助方法，提供完整的加载流程
     *
     * @return 是否成功加载
     */
    public boolean showLoadGameDialog(GameController gameController) {
        // 如果gameController为null，则创建一个新的
        boolean needInitController = (gameController == null);
        String selectedTankType = "standard"; // 默认坦克类型

        if (needInitController) {
            gameController = new GameController();
            // 设置必要的监听器
            GameController finalGameController = gameController;
            gameController.setGameEventListener(new GameController.GameEventListener() {
                @Override
                public void onPlayerDestroyed() {
                    // 直接获取SinglePlayerGameScreen
                    SinglePlayerGameScreen gameScreen = gameView.getSinglePlayerGameStarter().getGameScreen();
                    gameScreen.handlePlayerDestroyed(finalGameController, selectedTankType, gameView.getPlayerLives());
                }
            });

            // 设置视图引用
            gameController.setGameView(gameView);
        }

        // 创建文件选择器
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("加载游戏存档");

        // 设置默认目录（如果存在）
        File savesDir = new File("saves");
        if (savesDir.exists() && savesDir.isDirectory()) {
            fileChooser.setInitialDirectory(savesDir);
        }

        // 设置文件过滤器
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("游戏存档文件", "*.json"));

        // 显示对话框并获取选择的文件
        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            // 尝试加载游戏
            boolean success = loadGame(gameController, selectedFile);

            if (success && needInitController) {
                // 如果是新创建的控制器并且加载成功，设置到GameView
                gameView.setGameController(gameController);
            }

            return success;
        }

        return false;
    }

    /**
     * 重新开始当前关卡
     */
    @Override
    public void restartGame(GameController gameController, String tankType, int level) {
        if (gameController == null)
            return;

        Platform.runLater(() -> {
            // 彻底清除所有事件监听器
            Canvas gameCanvas = gameView.getGameCanvas();
            if (gameCanvas != null) {
                gameCanvas.setOnKeyPressed(null);
                gameCanvas.setOnKeyReleased(null);
            }

            // 清理游戏资源
            cleanupGameResources(gameController);

            // 使用GameView的方法启动游戏
            gameView.startGameWithLevel(tankType, level);
        });
    }

    /**
     * 计算游戏得分
     */
    @Override
    public int calculateScore(int level, int defeatedEnemies, long totalGameTime, int playerLives) {
        // 基础分数：每个敌人100分
        int baseScore = defeatedEnemies * 100;

        // 关卡加成：关卡数 * 500
        int levelBonus = level * 500;

        // 时间加成：游戏时间越短，得分越高
        int timeBonus = 0;
        long gameTimeSeconds = totalGameTime / 1000;
        if (gameTimeSeconds < 120) { // 少于2分钟
            timeBonus = 1000;
        } else if (gameTimeSeconds < 180) { // 少于3分钟
            timeBonus = 800;
        } else if (gameTimeSeconds < 240) { // 少于4分钟
            timeBonus = 600;
        } else if (gameTimeSeconds < 300) { // 少于5分钟
            timeBonus = 400;
        } else {
            timeBonus = 200;
        }

        // 生命加成：每个剩余生命200分
        int livesBonus = playerLives * 200;

        // 总分
        return baseScore + levelBonus + timeBonus + livesBonus;
    }

    /**
     * 检查关卡是否完成
     */
    @Override
    public boolean isLevelCompleted(GameController gameController) {
        return gameController != null && gameController.isLevelCompleted();
    }

    /**
     * 清理游戏资源
     */
    @Override
    public void cleanupGameResources(GameController gameController) {
        // 清理游戏控制器资源
        if (gameController != null) {
            // 在GameView中设置为null
            gameView.setGameController(null);
        }

        // 其余的清理工作由GameView完成
        // 这是因为很多UI和状态变量直接存在于GameView中
    }

    /**
     * 获取游戏得分
     */
    public int getScore(GameController gameController) {
        if (gameController == null) return 0;
        
        // 获取游戏数据
        int level = gameController.getCurrentLevel();
        int defeatedEnemies = gameController.getDefeatedEnemiesCount();
        long totalGameTime = gameView.getTotalGameTime();
        int playerLives = gameView.getPlayerLives();
        
        // 当玩家失败时，可能会传入错误的生命值
        if (playerLives < 0) {
            playerLives = 0;
        }
        
        // 计算得分
        int score = calculateScore(level, defeatedEnemies, totalGameTime, playerLives);
        
        // 确保不会返回负分
        return Math.max(0, score);
    }
}
