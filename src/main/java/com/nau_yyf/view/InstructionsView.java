package com.nau_yyf.view;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class InstructionsView {

    private final Color PRIMARY_COLOR = Color.rgb(37, 160, 218);    // 蓝色
    private final Color SECONDARY_COLOR = Color.rgb(76, 175, 80);   // 绿色
    private final Color TEXT_COLOR = Color.WHITE;
    private final Color BACKGROUND_COLOR = Color.rgb(27, 40, 56);   // 深蓝灰色
    private final Color CARD_COLOR = Color.rgb(40, 58, 78);         // 卡片背景色

    private StackPane root;
    private Dialog<Void> dialog;
    private Map<String, Image> imageCache = new HashMap<>();

    public InstructionsView(StackPane root) {
        this.root = root;
    }

    public void show() {
        Platform.runLater(() -> {
            dialog = new Dialog<>();
            dialog.setTitle("坦克2025 - 游戏说明");

            TabPane tabPane = new TabPane();
            tabPane.setTabMinWidth(100);
            tabPane.setTabMaxWidth(100);
            tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
            tabPane.setMinHeight(550);
            tabPane.setPrefWidth(800);

            tabPane.setStyle("-fx-background-color: " + toHexString(BACKGROUND_COLOR) + ";");

            tabPane.getTabs().add(createBasicControlsTab());
            tabPane.getTabs().add(createTanksTab());
            tabPane.getTabs().add(createPowerUpsTab());
            tabPane.getTabs().add(createLevelsTab());
            tabPane.getTabs().add(createStrategyTab());

            DialogPane dialogPane = dialog.getDialogPane();
            dialogPane.setContent(tabPane);
            dialogPane.getStylesheets().add(getClass().getResource("/css/instructions.css").toExternalForm());
            dialogPane.getButtonTypes().add(ButtonType.CLOSE);
            dialogPane.setPrefWidth(820);
            dialogPane.setPrefHeight(600);

            dialog.showAndWait();
        });
    }

    private Tab createBasicControlsTab() {
        Tab tab = new Tab("操作控制");

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: " + toHexString(BACKGROUND_COLOR) + ";");

        VBox content = new VBox(30);
        content.setPadding(new Insets(25));
        content.setAlignment(Pos.TOP_CENTER);
        content.setStyle("-fx-background-color: " + toHexString(BACKGROUND_COLOR) + ";");

        // 单人游戏控制
        VBox singlePlayerCard = createCard("单人游戏(1P)控制");

        HBox controlsLayout = new HBox(50);
        controlsLayout.setAlignment(Pos.CENTER);

        // 左边：WASD控制
        VBox leftControls = new VBox(15);
        leftControls.setAlignment(Pos.CENTER);

        HBox wasdRow1 = new HBox(5);
        wasdRow1.setAlignment(Pos.CENTER);
        wasdRow1.getChildren().add(createKeyButton("W", true));

        HBox wasdRow2 = new HBox(5);
        wasdRow2.setAlignment(Pos.CENTER);
        wasdRow2.getChildren().addAll(
                createKeyButton("A", true),
                createKeyButton("S", true),
                createKeyButton("D", true)
        );

        Text wasdLabel = createText("向上移动    向下移动    向左移动    向右移动");

        leftControls.getChildren().addAll(wasdRow1, wasdRow2, wasdLabel);

        // 中间：箭头控制
        VBox middleControls = new VBox(15);
        middleControls.setAlignment(Pos.CENTER);

        HBox arrowRow1 = new HBox(5);
        arrowRow1.setAlignment(Pos.CENTER);
        arrowRow1.getChildren().add(createKeyButton("↑", false));

        HBox arrowRow2 = new HBox(5);
        arrowRow2.setAlignment(Pos.CENTER);
        arrowRow2.getChildren().addAll(
                createKeyButton("←", false),
                createKeyButton("↓", false),
                createKeyButton("→", false)
        );

        Text arrowLabel = createText("或");

        middleControls.getChildren().addAll(arrowRow1, arrowRow2, arrowLabel);

        // 右边：功能键
        VBox rightControls = new VBox(10);
        rightControls.setAlignment(Pos.CENTER);

        HBox spaceRow = new HBox(5);
        spaceRow.setAlignment(Pos.CENTER);
        spaceRow.getChildren().add(createKeyButton("空格", true, 100, 40));
        Text spaceLabel = createText("发射子弹");

        HBox eRow = new HBox(5);
        eRow.setAlignment(Pos.CENTER);
        eRow.getChildren().add(createKeyButton("E", true));
        Text eLabel = createText("使用炸弹");

        HBox escRow = new HBox(5);
        escRow.setAlignment(Pos.CENTER);
        escRow.getChildren().add(createKeyButton("ESC", true, 60, 40));
        Text escLabel = createText("暂停游戏");

        rightControls.getChildren().addAll(spaceRow, spaceLabel, eRow, eLabel, escRow, escLabel);

        controlsLayout.getChildren().addAll(leftControls, middleControls, rightControls);
        singlePlayerCard.getChildren().add(controlsLayout);

        // 双人游戏控制
        VBox multiPlayerCard = createCard("双人游戏(2P)控制");

        GridPane multiPlayerGrid = new GridPane();
        multiPlayerGrid.setHgap(20);
        multiPlayerGrid.setVgap(15);
        multiPlayerGrid.setAlignment(Pos.CENTER);

        // 表头
        Text playerHeader = createBoldText("玩家");
        Text upHeader = createBoldText("向上");
        Text downHeader = createBoldText("向下");
        Text leftHeader = createBoldText("向左");
        Text rightHeader = createBoldText("向右");
        Text shootHeader = createBoldText("射击");
        Text bombHeader = createBoldText("炸弹");
        Text pauseHeader = createBoldText("暂停");

        multiPlayerGrid.add(playerHeader, 0, 0);
        multiPlayerGrid.add(upHeader, 1, 0);
        multiPlayerGrid.add(downHeader, 2, 0);
        multiPlayerGrid.add(leftHeader, 3, 0);
        multiPlayerGrid.add(rightHeader, 4, 0);
        multiPlayerGrid.add(shootHeader, 5, 0);
        multiPlayerGrid.add(bombHeader, 6, 0);
        multiPlayerGrid.add(pauseHeader, 7, 0);

        // 玩家1
        Text player1Text = createText("玩家1");
        multiPlayerGrid.add(player1Text, 0, 1);
        multiPlayerGrid.add(createKeyButton("W", true), 1, 1);
        multiPlayerGrid.add(createKeyButton("S", true), 2, 1);
        multiPlayerGrid.add(createKeyButton("A", true), 3, 1);
        multiPlayerGrid.add(createKeyButton("D", true), 4, 1);
        multiPlayerGrid.add(createKeyButton("空格", true, 80, 40), 5, 1);
        multiPlayerGrid.add(createKeyButton("E", true), 6, 1);
        multiPlayerGrid.add(createKeyButton("ESC", true, 60, 40), 7, 1);

        // 玩家2
        Text player2Text = createText("玩家2");
        multiPlayerGrid.add(player2Text, 0, 2);
        multiPlayerGrid.add(createKeyButton("↑", false), 1, 2);
        multiPlayerGrid.add(createKeyButton("↓", false), 2, 2);
        multiPlayerGrid.add(createKeyButton("←", false), 3, 2);
        multiPlayerGrid.add(createKeyButton("→", false), 4, 2);
        multiPlayerGrid.add(createKeyButton("回车", false, 80, 40), 5, 2);
        multiPlayerGrid.add(createKeyButton("J", false), 6, 2);
        multiPlayerGrid.add(createKeyButton("P", false, 60, 40), 7, 2);

        multiPlayerCard.getChildren().add(multiPlayerGrid);

        // 游戏目标卡片
        VBox objectivesCard = createCard("游戏目标");

        VBox objectivesContent = new VBox(15);
        objectivesContent.setAlignment(Pos.CENTER_LEFT);

        objectivesContent.getChildren().addAll(
                createNumberedItem("1", "消灭所有敌方坦克"),
                createNumberedItem("2", "保护己方基地不被摧毁"),
                createNumberedItem("3", "收集道具提升能力")
        );

        objectivesCard.getChildren().add(objectivesContent);

        content.getChildren().addAll(singlePlayerCard, multiPlayerCard, objectivesCard);
        scrollPane.setContent(content);

        tab.setContent(scrollPane);
        return tab;
    }

    private Tab createTanksTab() {
        Tab tab = new Tab("坦克类型");

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: " + toHexString(BACKGROUND_COLOR) + ";");

        VBox content = new VBox(30);
        content.setPadding(new Insets(25));
        content.setAlignment(Pos.TOP_CENTER);
        content.setStyle("-fx-background-color: " + toHexString(BACKGROUND_COLOR) + ";");

        // 友方坦克
        VBox friendlyTanksCard = createCard("友方坦克");

        VBox friendlyTanksContent = new VBox(20);
        friendlyTanksContent.setAlignment(Pos.CENTER);

        // 轻型坦克卡片
        HBox lightTank = createTankInfoCard(
                "轻型坦克",
                "/images/tanks/friendly/light/0.png",
                createStatBar(3, 5),
                createStatBar(4, 5),
                createStatBar(1, 5),
                "高速度，快速攻击，但装甲薄弱"
        );

        // 标准坦克卡片
        HBox standardTank = createTankInfoCard(
                "标准坦克",
                "/images/tanks/friendly/standard/0.png",
                createStatBar(4, 5),
                createStatBar(3, 5),
                createStatBar(2, 5),
                "均衡的速度和火力，适合新手"
        );

        // 重型坦克卡片
        HBox heavyTank = createTankInfoCard(
                "重型坦克",
                "/images/tanks/friendly/heavy/0.png",
                createStatBar(5, 5),
                createStatBar(2, 5),
                createStatBar(3, 5),
                "强大火力和装甲，但移动缓慢"
        );

        friendlyTanksContent.getChildren().addAll(lightTank, standardTank, heavyTank);
        friendlyTanksCard.getChildren().add(friendlyTanksContent);

        // 敌方坦克
        VBox enemyTanksCard = createCard("敌方坦克");

        VBox enemyTanksContent = new VBox(20);
        enemyTanksContent.setAlignment(Pos.CENTER);

        // 基础坦克卡片
        HBox basicTank = createTankInfoCard(
                "基础坦克",
                "/images/tanks/enemy/basic/0.png",
                createStatBar(2, 5),
                createStatBar(2, 5),
                createStatBar(1, 5),
                "最常见的敌人，攻击和防御均较弱"
        );

        // 精英坦克卡片
        HBox eliteTank = createTankInfoCard(
                "精英坦克",
                "/images/tanks/enemy/elite/0.png",
                createStatBar(4, 5),
                createStatBar(3, 5),
                createStatBar(2, 5),
                "危险度提升，攻击更频繁"
        );

        // 终结者坦克卡片
        HBox bossTank = createTankInfoCard(
                "终结者坦克",
                "/images/tanks/enemy/boss/0.png",
                createStatBar(5, 5),
                createStatBar(4, 5),
                createStatBar(3, 5),
                "最强大的敌人，装甲厚重且火力强大"
        );

        enemyTanksContent.getChildren().addAll(basicTank, eliteTank, bossTank);
        enemyTanksCard.getChildren().add(enemyTanksContent);

        content.getChildren().addAll(friendlyTanksCard, enemyTanksCard);
        scrollPane.setContent(content);

        tab.setContent(scrollPane);
        return tab;
    }

    private Tab createPowerUpsTab() {
        Tab tab = new Tab("增益道具");

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: " + toHexString(BACKGROUND_COLOR) + ";");

        VBox content = new VBox(30);
        content.setPadding(new Insets(25));
        content.setAlignment(Pos.TOP_CENTER);
        content.setStyle("-fx-background-color: " + toHexString(BACKGROUND_COLOR) + ";");

        // 增益道具展示
        VBox powerUpsCard = createCard("增益道具");

        // 使用卡片布局展示道具
        FlowPane powerUpsFlow = new FlowPane();
        powerUpsFlow.setHgap(20);
        powerUpsFlow.setVgap(20);
        powerUpsFlow.setAlignment(Pos.CENTER);

        powerUpsFlow.getChildren().addAll(
                createPowerUpCard("攻击增强", "/images/powerups/attack.png", "伤害提升50%", "10秒"),
                createPowerUpCard("炸弹", "/images/powerups/bomb.png", "放置后5秒爆炸，范围伤害", "一次性"),
                createPowerUpCard("生命恢复", "/images/powerups/health.png", "恢复1点生命值", "立即"),
                createPowerUpCard("无敌状态", "/images/powerups/invincibility.png", "免疫所有伤害", "5秒"),
                createPowerUpCard("护盾保护", "/images/powerups/shield.png", "可抵挡一次伤害", "20秒或直到使用"),
                createPowerUpCard("速度提升", "/images/powerups/speed.png", "移动速度提升50%", "8秒")
        );

        powerUpsCard.getChildren().add(powerUpsFlow);

        // 道具掉落机制卡片
        VBox dropMechanismCard = createCard("道具掉落机制");

        VBox dropContent = new VBox(15);
        dropContent.setAlignment(Pos.CENTER_LEFT);

        HBox dropPoint1 = createBulletPoint("敌方坦克被摧毁时有60%几率掉落道具");
        HBox dropPoint2 = createBulletPoint("每8秒随机在地图上生成道具");
        HBox dropPoint3 = createBulletPoint("道具会在出现后30秒闪烁，提示即将消失");

        dropContent.getChildren().addAll(dropPoint1, dropPoint2, dropPoint3);
        dropMechanismCard.getChildren().add(dropContent);

        // 炸弹使用说明卡片
        VBox bombUsageCard = createCard("炸弹使用说明");

        HBox bombUsageContent = new HBox(20);
        bombUsageContent.setAlignment(Pos.CENTER_LEFT);

        // 炸弹图标
        ImageView bombImage = new ImageView(getImage("/images/powerups/bomb.png"));
        bombImage.setFitHeight(50);
        bombImage.setFitWidth(50);
        bombImage.setPreserveRatio(true);

        VBox bombTextContent = new VBox(10);
        bombTextContent.setAlignment(Pos.CENTER_LEFT);
        bombTextContent.getChildren().addAll(
                createBulletPoint("拾取炸弹道具后，按【E】键放置炸弹，5秒后自动爆炸"),
                createBulletPoint("炸弹爆炸会对范围内的敌方坦克造成2点伤害"),
                createBulletPoint("是对付成群敌人的有效手段")
        );

        bombUsageContent.getChildren().addAll(bombImage, bombTextContent);
        bombUsageCard.getChildren().add(bombUsageContent);

        content.getChildren().addAll(powerUpsCard, dropMechanismCard, bombUsageCard);
        scrollPane.setContent(content);

        tab.setContent(scrollPane);
        return tab;
    }

    private Tab createLevelsTab() {
        Tab tab = new Tab("关卡");

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: " + toHexString(BACKGROUND_COLOR) + ";");

        VBox content = new VBox(30);
        content.setPadding(new Insets(25));
        content.setAlignment(Pos.TOP_CENTER);
        content.setStyle("-fx-background-color: " + toHexString(BACKGROUND_COLOR) + ";");

        // 关卡说明卡片
        VBox levelsCard = createCard("关卡说明");

        // 创建表格式布局
        GridPane levelTable = new GridPane();
        levelTable.setGridLinesVisible(true); // 显示网格线
        levelTable.setHgap(0);
        levelTable.setVgap(0);
        levelTable.setAlignment(Pos.CENTER);

        // 表头
        createTableCell(levelTable, "关卡", 0, 0, true);
        createTableCell(levelTable, "敌方坦克数量", 0, 1, true);
        createTableCell(levelTable, "敌人组成", 0, 2, true);
        createTableCell(levelTable, "难度", 0, 3, true);

        // 数据行
        createTableCell(levelTable, "第1关", 1, 0, false);
        createTableCell(levelTable, "15", 1, 1, false);
        createTableCell(levelTable, "全部基础坦克", 1, 2, false);
        createTableCell(levelTable, "★☆☆☆☆", 1, 3, false);

        createTableCell(levelTable, "第2关", 2, 0, false);
        createTableCell(levelTable, "20", 2, 1, false);
        createTableCell(levelTable, "90%基础坦克，10%精英坦克", 2, 2, false);
        createTableCell(levelTable, "★★☆☆☆", 2, 3, false);

        createTableCell(levelTable, "第3关", 3, 0, false);
        createTableCell(levelTable, "30", 3, 1, false);
        createTableCell(levelTable, "57%基础坦克，40%精英坦克，3%终结者", 3, 2, false);
        createTableCell(levelTable, "★★★☆☆", 3, 3, false);

        createTableCell(levelTable, "第4关", 4, 0, false);
        createTableCell(levelTable, "45", 4, 1, false);
        createTableCell(levelTable, "20%基础坦克，60%精英坦克，20%终结者", 4, 2, false);
        createTableCell(levelTable, "★★★★☆", 4, 3, false);

        createTableCell(levelTable, "第5关", 5, 0, false);
        createTableCell(levelTable, "60", 5, 1, false);
        createTableCell(levelTable, "15%基础坦克，50%精英坦克，35%终结者", 5, 2, false);
        createTableCell(levelTable, "★★★★★", 5, 3, false);

        // 设置列宽
        ColumnConstraints col1 = new ColumnConstraints(120);
        ColumnConstraints col2 = new ColumnConstraints(120);
        ColumnConstraints col3 = new ColumnConstraints(350);
        ColumnConstraints col4 = new ColumnConstraints(120);
        levelTable.getColumnConstraints().addAll(col1, col2, col3, col4);

        levelsCard.getChildren().add(levelTable);

        // 添加关卡预览图（五个关卡）
        VBox levelPreviewCard = createCard("关卡预览");

        GridPane previewGrid = new GridPane();
        previewGrid.setHgap(20);
        previewGrid.setVgap(20);
        previewGrid.setAlignment(Pos.CENTER);

        // 第一行预览图
        for (int i = 1; i <= 3; i++) {
            VBox levelPreview = new VBox(10);
            levelPreview.setAlignment(Pos.CENTER);

            Rectangle previewImage = new Rectangle(140, 140);
            previewImage.setFill(Color.rgb(20, 30, 45));
            previewImage.setStroke(Color.WHITE);
            previewImage.setStrokeWidth(1);
            previewImage.setArcWidth(0);
            previewImage.setArcHeight(0);

            Text levelText = createText("第" + i + "关");

            levelPreview.getChildren().addAll(previewImage, levelText);
            previewGrid.add(levelPreview, i - 1, 0);
        }

        // 第二行预览图
        for (int i = 4; i <= 5; i++) {
            VBox levelPreview = new VBox(10);
            levelPreview.setAlignment(Pos.CENTER);

            Rectangle previewImage = new Rectangle(140, 140);
            previewImage.setFill(Color.rgb(20, 30, 45));
            previewImage.setStroke(Color.WHITE);
            previewImage.setStrokeWidth(1);
            previewImage.setArcWidth(0);
            previewImage.setArcHeight(0);

            Text levelText = createText("第" + i + "关");

            levelPreview.getChildren().addAll(previewImage, levelText);
            previewGrid.add(levelPreview, i - 4, 1);
        }

        levelPreviewCard.getChildren().add(previewGrid);

        content.getChildren().addAll(levelsCard, levelPreviewCard);
        scrollPane.setContent(content);

        tab.setContent(scrollPane);
        return tab;
    }

    private Tab createStrategyTab() {
        Tab tab = new Tab("游戏攻略");

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: " + toHexString(BACKGROUND_COLOR) + ";");

        VBox content = new VBox(30);
        content.setPadding(new Insets(25));
        content.setAlignment(Pos.TOP_CENTER);
        content.setStyle("-fx-background-color: " + toHexString(BACKGROUND_COLOR) + ";");

        // 坦克选择策略卡片
        VBox tankStrategyCard = createCard("坦克选择策略");

        VBox tankStrategyContent = new VBox(15);
        tankStrategyContent.setAlignment(Pos.CENTER_LEFT);

        tankStrategyContent.getChildren().addAll(
                createBulletPoint("新手推荐使用标准坦克，均衡的性能适合熟悉游戏"),
                createBulletPoint("熟练后可尝试轻型坦克快速突袭或重型坦克防守基地")
        );

        tankStrategyCard.getChildren().add(tankStrategyContent);

        // 道具优先级卡片
        VBox powerUpStrategyCard = createCard("道具优先级");

        VBox powerUpStrategyContent = new VBox(15);
        powerUpStrategyContent.setAlignment(Pos.CENTER_LEFT);

        powerUpStrategyContent.getChildren().addAll(
                createBulletPoint("低血量时优先获取生命恢复和护盾"),
                createBulletPoint("面对大量敌人时利用炸弹清场"),
                createBulletPoint("突破困境时使用无敌道具")
        );

        powerUpStrategyCard.getChildren().add(powerUpStrategyContent);

        // 关卡策略卡片
        VBox levelStrategyCard = createCard("关卡策略");

        VBox levelStrategyContent = new VBox(15);
        levelStrategyContent.setAlignment(Pos.CENTER_LEFT);

        levelStrategyContent.getChildren().addAll(
                createBulletPoint("第1-2关：积极进攻，快速消灭敌人"),
                createBulletPoint("第3关：平衡进攻与防守，注意精英坦克"),
                createBulletPoint("第4-5关：保持防守姿态，优先击败终结者坦克")
        );

        levelStrategyCard.getChildren().add(levelStrategyContent);

        // 添加游戏技巧卡片
        VBox tipsCard = createCard("游戏技巧");

        VBox tipsContent = new VBox(15);
        tipsContent.setAlignment(Pos.CENTER_LEFT);

        tipsContent.getChildren().addAll(
                createBulletPoint("在水域附近引诱敌方坦克，让他们掉入水中受到伤害"),
                createBulletPoint("利用墙壁做掩护，采取伏击战术"),
                createBulletPoint("合理规划移动路线，避免被敌方坦克包围"),
                createBulletPoint("高级关卡中，先清理小兵再集中火力对付终结者坦克")
        );

        tipsCard.getChildren().add(tipsContent);

        content.getChildren().addAll(tankStrategyCard, powerUpStrategyCard, levelStrategyCard, tipsCard);
        scrollPane.setContent(content);

        tab.setContent(scrollPane);
        return tab;
    }

    // 创建卡片容器
    private VBox createCard(String title) {
        VBox card = new VBox(15);
        card.setAlignment(Pos.TOP_CENTER);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: " + toHexString(CARD_COLOR) + "; " +
                "-fx-background-radius: 10;");

        // 添加卡片阴影效果
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.rgb(0, 0, 0, 0.5));
        shadow.setOffsetX(0);
        shadow.setOffsetY(3);
        shadow.setRadius(5);
        card.setEffect(shadow);

        Text titleText = new Text(title);
        titleText.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        titleText.setFill(TEXT_COLOR);

        card.getChildren().add(titleText);
        return card;
    }

    // 创建文本
    private Text createText(String content) {
        Text text = new Text(content);
        text.setFont(Font.font("Arial", 14));
        text.setFill(TEXT_COLOR);
        return text;
    }

    // 创建粗体文本
    private Text createBoldText(String content) {
        Text text = new Text(content);
        text.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        text.setFill(TEXT_COLOR);
        return text;
    }

    // 创建按键按钮
    private StackPane createKeyButton(String text, boolean isPrimary) {
        return createKeyButton(text, isPrimary, 40, 40);
    }

    private StackPane createKeyButton(String text, boolean isPrimary, double width, double height) {
        StackPane keyButton = new StackPane();
        keyButton.setPrefSize(width, height);

        Rectangle bg = new Rectangle(width, height);
        bg.setArcWidth(10);
        bg.setArcHeight(10);

        if (isPrimary) {
            bg.setFill(Color.rgb(40, 50, 60));
        } else {
            bg.setFill(Color.rgb(60, 70, 80));
        }

        bg.setStroke(Color.WHITE);
        bg.setStrokeWidth(1);

        Text keyText = new Text(text);
        keyText.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        keyText.setFill(TEXT_COLOR);

        keyButton.getChildren().addAll(bg, keyText);
        return keyButton;
    }

    // 创建带有标签的按键
    private HBox createKeyWithLabel(StackPane keyButton, StackPane alternateKey, String label) {
        HBox container = new HBox(10);
        container.setAlignment(Pos.CENTER_LEFT);

        if (alternateKey != null) {
            Text orText = new Text("或");
            orText.setFont(Font.font("Arial", 14));
            orText.setFill(TEXT_COLOR);

            container.getChildren().addAll(keyButton, orText, alternateKey);
        } else {
            container.getChildren().add(keyButton);
        }

        Text labelText = new Text(label);
        labelText.setFont(Font.font("Arial", 14));
        labelText.setFill(TEXT_COLOR);

        container.getChildren().add(labelText);
        return container;
    }

    // 创建目标编号
    private StackPane createObjectiveNumber(String number) {
        StackPane numberContainer = new StackPane();

        Circle circle = new Circle(15);
        circle.setFill(PRIMARY_COLOR);

        Text numberText = new Text(number);
        numberText.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        numberText.setFill(TEXT_COLOR);

        numberContainer.getChildren().addAll(circle, numberText);
        return numberContainer;
    }

    // 创建坦克信息卡片
    private HBox createTankInfoCard(String type, String imagePath, Node healthBar, Node speedBar, Node attackBar, String description) {
        HBox card = new HBox(15);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(15));
        card.setPrefWidth(750);
        card.setMinHeight(100);
        card.setStyle("-fx-background-color: rgba(0, 0, 0, 0.3); " +
                "-fx-background-radius: 0;");

        // 坦克图标容器
        StackPane imageContainer = new StackPane();
        imageContainer.setMinWidth(70);

        // 坦克图标
        ImageView tankImage = new ImageView(getImage(imagePath));
        tankImage.setFitHeight(60);
        tankImage.setFitWidth(60);
        tankImage.setPreserveRatio(true);
        imageContainer.getChildren().add(tankImage);

        // 坦克信息
        VBox infoBox = new VBox(5);
        infoBox.setAlignment(Pos.CENTER_LEFT);
        infoBox.setMinWidth(300);

        Text nameText = new Text(type);
        nameText.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        nameText.setFill(TEXT_COLOR);

        // 属性栏容器
        VBox statsBox = new VBox(6);
        statsBox.setMinWidth(300);

        // 生命值
        HBox healthBox = new HBox(5);
        healthBox.setAlignment(Pos.CENTER_LEFT);
        Text healthLabel = createText("生命值:");
        HBox healthLabelBox = new HBox(healthLabel);
        healthLabelBox.setMinWidth(50);
        healthBox.getChildren().addAll(healthLabelBox, healthBar);

        // 速度
        HBox speedBox = new HBox(5);
        speedBox.setAlignment(Pos.CENTER_LEFT);
        Text speedLabel = createText("速度:");
        HBox speedLabelBox = new HBox(speedLabel);
        speedLabelBox.setMinWidth(50);
        speedBox.getChildren().addAll(speedLabelBox, speedBar);

        // 攻击力
        HBox attackBox = new HBox(5);
        attackBox.setAlignment(Pos.CENTER_LEFT);
        Text attackLabel = createText("攻击力:");
        HBox attackLabelBox = new HBox(attackLabel);
        attackLabelBox.setMinWidth(50);
        attackBox.getChildren().addAll(attackLabelBox, attackBar);

        statsBox.getChildren().addAll(healthBox, speedBox, attackBox);

        // 特点描述
        Text descText = createText(description);
        descText.setWrappingWidth(350);

        infoBox.getChildren().addAll(nameText, statsBox, descText);
        card.getChildren().addAll(imageContainer, infoBox);

        return card;
    }

    // 创建属性条
    private HBox createStatBar(int value, int maxValue) {
        HBox statBar = new HBox(2);

        for (int i = 1; i <= maxValue; i++) {
            Rectangle bar = new Rectangle(20, 10);

            if (i <= value) {
                if (i <= 2) {
                    bar.setFill(Color.rgb(76, 175, 80)); // 绿色
                } else if (i <= 3) {
                    bar.setFill(Color.rgb(255, 193, 7)); // 黄色
                } else {
                    bar.setFill(Color.rgb(244, 67, 54)); // 红色
                }
            } else {
                bar.setFill(Color.rgb(150, 150, 150, 0.3)); // 灰色
            }

            statBar.getChildren().add(bar);
        }

        return statBar;
    }

    // 创建道具卡片
    private VBox createPowerUpCard(String name, String imagePath, String effect, String duration) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(15));
        card.setPrefSize(180, 200);
        card.setStyle("-fx-background-color: rgba(0, 0, 0, 0.3); " +
                "-fx-background-radius: 10;");

        // 道具图标
        ImageView powerUpImage = new ImageView(getImage(imagePath));
        powerUpImage.setFitHeight(50);
        powerUpImage.setFitWidth(50);
        powerUpImage.setPreserveRatio(true);

        // 道具名称
        Text nameText = new Text(name);
        nameText.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        nameText.setFill(TEXT_COLOR);

        // 道具效果
        Text effectText = new Text(effect);
        effectText.setFont(Font.font("Arial", 14));
        effectText.setFill(TEXT_COLOR);
        effectText.setTextAlignment(TextAlignment.CENTER);
        effectText.setWrappingWidth(150);

        // 持续时间
        HBox durationBox = new HBox(5);
        durationBox.setAlignment(Pos.CENTER);

        Text durationLabel = new Text("持续时间:");
        durationLabel.setFont(Font.font("Arial", 12));
        durationLabel.setFill(TEXT_COLOR);

        Text durationValue = new Text(duration);
        durationValue.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        durationValue.setFill(PRIMARY_COLOR);

        durationBox.getChildren().addAll(durationLabel, durationValue);

        card.getChildren().addAll(powerUpImage, nameText, effectText, durationBox);
        return card;
    }

    // 创建带项目符号的文本
    private HBox createBulletPoint(String text) {
        HBox container = new HBox(10);
        container.setAlignment(Pos.CENTER_LEFT);

        Circle bullet = new Circle(4);
        bullet.setFill(PRIMARY_COLOR);

        Text contentText = createText(text);

        container.getChildren().addAll(bullet, contentText);
        return container;
    }

    // 创建表格单元格
    private void createTableCell(GridPane table, String text, int row, int col, boolean isHeader) {
        StackPane cell = new StackPane();
        cell.setPadding(new Insets(10));

        if (isHeader) {
            cell.setStyle("-fx-background-color: " + toHexString(PRIMARY_COLOR) + ";");
            Text headerText = new Text(text);
            headerText.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            headerText.setFill(TEXT_COLOR);
            cell.getChildren().add(headerText);
        } else {
            if (row % 2 == 0) {
                cell.setStyle("-fx-background-color: rgba(0, 0, 0, 0.3);");
            } else {
                cell.setStyle("-fx-background-color: rgba(0, 0, 0, 0.2);");
            }
            Text cellText = new Text(text);
            cellText.setFont(Font.font("Arial", 14));
            cellText.setFill(TEXT_COLOR);
            cell.getChildren().add(cellText);
        }

        table.add(cell, col, row);
    }

    // 创建编号项目
    private HBox createNumberedItem(String number, String text) {
        HBox item = new HBox(15);
        item.setAlignment(Pos.CENTER_LEFT);

        Text numberText = createText(number + ".");
        numberText.setFont(Font.font("Arial", FontWeight.BOLD, 16));

        Text contentText = createText(text);

        item.getChildren().addAll(numberText, contentText);
        return item;
    }

    private String toHexString(Color color) {
        int r = (int) (color.getRed() * 255);
        int g = (int) (color.getGreen() * 255);
        int b = (int) (color.getBlue() * 255);
        return String.format("#%02X%02X%02X", r, g, b);
    }

    // 图像加载和缓存方法
    private Image getImage(String path) {
        if (imageCache.containsKey(path)) {
            return imageCache.get(path);
        }

        try {
            // 首先尝试使用getResourceAsStream加载
            InputStream is = getClass().getResourceAsStream(path);

            // 如果流为空，尝试备用路径格式
            if (is == null) {
                // 移除前导斜杠再试一次
                if (path.startsWith("/")) {
                    String altPath = path.substring(1);
                    is = getClass().getResourceAsStream(altPath);
                }

                // 如果仍然为空，尝试添加前导斜杠
                if (is == null && !path.startsWith("/")) {
                    String altPath = "/" + path;
                    is = getClass().getResourceAsStream(altPath);
                }

                // 如果还是为空，使用ClassLoader直接加载
                if (is == null) {
                    String classLoaderPath = path.startsWith("/") ? path.substring(1) : path;
                    is = getClass().getClassLoader().getResourceAsStream(classLoaderPath);
                }
            }

            // 如果最终找到了资源流
            if (is != null) {
                Image image = new Image(is);
                imageCache.put(path, image);
                return image;
            } else {
                // 创建一个彩色的占位图
                return createPlaceholderImage(path);
            }
        } catch (Exception e) {
            // 创建占位图
            return createPlaceholderImage(path);
        }
    }

    // 创建占位图，使界面可以正常显示
    private Image createPlaceholderImage(String path) {
        // 从路径名提取类型信息来选择不同颜色
        Color color;
        if (path.contains("tanks/friendly")) {
            color = Color.rgb(0, 100, 200); // 友方坦克蓝色
        } else if (path.contains("tanks/enemy")) {
            color = Color.rgb(200, 50, 50); // 敌方坦克红色
        } else if (path.contains("powerups/attack")) {
            color = Color.rgb(255, 50, 50); // 攻击道具红色
        } else if (path.contains("powerups/bomb")) {
            color = Color.rgb(50, 50, 50); // 炸弹道具黑色
        } else if (path.contains("powerups/health")) {
            color = Color.rgb(50, 200, 50); // 健康道具绿色
        } else if (path.contains("powerups/invincibility")) {
            color = Color.rgb(200, 200, 50); // 无敌道具黄色
        } else if (path.contains("powerups/shield")) {
            color = Color.rgb(50, 100, 250); // 护盾道具蓝色
        } else if (path.contains("powerups/speed")) {
            color = Color.rgb(150, 50, 250); // 速度道具紫色
        } else {
            color = Color.rgb(150, 150, 150); // 默认灰色
        }

        // 创建一个40x40的占位符图像
        int size = 40;
        WritableImage image = new WritableImage(size, size);
        PixelWriter pixelWriter = image.getPixelWriter();

        // 填充颜色
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                // 计算到中心的距离（用于创建圆形）
                double distance = Math.sqrt(Math.pow(x - size / 2, 2) + Math.pow(y - size / 2, 2));

                if (distance < size / 2 - 2) {
                    // 内部填充
                    pixelWriter.setColor(x, y, color);
                } else if (distance < size / 2) {
                    // 边框
                    pixelWriter.setColor(x, y, Color.BLACK);
                } else {
                    // 背景透明
                    pixelWriter.setColor(x, y, Color.TRANSPARENT);
                }
            }
        }

        // 缓存并返回占位图
        imageCache.put(path, image);
        return image;
    }

    // 辅助类：圆形
    private class Circle extends javafx.scene.shape.Circle {
        public Circle(double radius) {
            super(radius);
            setFill(Color.TRANSPARENT);
            setStroke(Color.WHITE);
            setStrokeWidth(1);
        }
    }
}