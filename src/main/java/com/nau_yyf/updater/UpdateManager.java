package com.nau_yyf.updater;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 游戏更新管理器
 * 简化版实现，支持手动更新和离线模式
 */
public class UpdateManager {
    private static final String GITHUB_API_URL = "https://api.github.com/repos/Auroral0810/ArmoredAssault/releases/latest";
    private static final String DOWNLOAD_URL_BASE = "https://github.com/Auroral0810/ArmoredAssault/releases/download/";
    private static final String CURRENT_VERSION = "1.0.0"; // 当前应用版本
    private static final boolean OFFLINE_MODE = true; // 如果无法访问GitHub，设为true

    /**
     * 检查更新
     * @return 如果更新进行中返回true，否则返回false
     */
    public static boolean checkForUpdates() {
        // 离线模式下跳过更新检查
        if (OFFLINE_MODE) {
            System.out.println("应用运行在离线模式，跳过更新检查");
            return false;
        }
        
        try {
            System.out.println("正在检查更新...");
            String latestVersion = getLatestVersion();
            
            if (isNewerVersion(latestVersion, CURRENT_VERSION)) {
                boolean shouldUpdate = showUpdateDialog(latestVersion);
                
                if (shouldUpdate) {
                    performUpdate(latestVersion);
                    return true; // 更新进行中
                }
            } else {
                System.out.println("已是最新版本");
            }
        } catch (Exception e) {
            System.out.println("更新检查失败: " + e.getMessage());
            // 只在开发模式下显示错误对话框，正式环境静默失败
            if (isDevMode()) {
                showErrorDialog("检查更新失败", 
                        "无法连接到GitHub API，可能因为:\n" +
                        "1. 网络连接问题\n" +
                        "2. 仓库尚未创建发布版本\n" +
                        "错误详情: " + e.getMessage());
            }
        }
        return false; // 无更新或用户取消
    }
    
    /**
     * 判断是否为开发模式
     */
    private static boolean isDevMode() {
        // 通过系统属性或环境变量判断
        return System.getProperty("dev.mode", "false").equals("true");
    }
    
    /**
     * 获取GitHub上的最新版本号
     */
    private static String getLatestVersion() throws Exception {
        // 添加User-Agent以符合GitHub API要求
        URL url = new URL(GITHUB_API_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("User-Agent", "ArmoredAssault-Updater");
        conn.setConnectTimeout(5000); // 5秒连接超时
        conn.setReadTimeout(5000);    // 5秒读取超时
        
        int responseCode = conn.getResponseCode();
        
        if (responseCode != 200) {
            throw new Exception("GitHub API返回错误状态码: " + responseCode);
        }
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            
            // 从GitHub API响应中提取版本号
            Pattern pattern = Pattern.compile("\"tag_name\":\"v(\\d+\\.\\d+\\.\\d+)\"");
            Matcher matcher = pattern.matcher(response.toString());
            if (matcher.find()) {
                return matcher.group(1);
            }
        }
        
        return CURRENT_VERSION; // 如果无法获取，返回当前版本
    }
    
    /**
     * 比较版本号，确定是否需要更新
     */
    private static boolean isNewerVersion(String latest, String current) {
        String[] latestParts = latest.split("\\.");
        String[] currentParts = current.split("\\.");
        
        for (int i = 0; i < Math.min(latestParts.length, currentParts.length); i++) {
            int latestPart = Integer.parseInt(latestParts[i]);
            int currentPart = Integer.parseInt(currentParts[i]);
            
            if (latestPart > currentPart) {
                return true;
            } else if (latestPart < currentPart) {
                return false;
            }
        }
        
        return latestParts.length > currentParts.length;
    }
    
    /**
     * 显示更新确认对话框
     */
    private static boolean showUpdateDialog(String newVersion) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("发现新版本");
        alert.setHeaderText("发现新版本 v" + newVersion);
        alert.setContentText("要立即更新吗？更新后程序将重启。");
        
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }
    
    /**
     * 显示错误对话框
     */
    private static void showErrorDialog(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("更新错误");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    /**
     * 执行更新操作
     * 简化版：打开浏览器到发布页面，让用户手动下载
     */
    private static void performUpdate(String version) {
        try {
            // 构建下载URL（指向GitHub发布页面）
            String downloadUrl = "https://github.com/Auroral0810/ArmoredAssault/releases/tag/v" + version;
            
            // 打开默认浏览器到下载页面
            java.awt.Desktop.getDesktop().browse(java.net.URI.create(downloadUrl));
            
            // 显示提示
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("手动更新");
            alert.setHeaderText("已打开下载页面");
            alert.setContentText("请从网页下载并安装新版本，然后关闭当前应用。");
            alert.showAndWait();
            
            // 关闭当前应用
            Platform.exit();
        } catch (Exception e) {
            e.printStackTrace();
            showErrorDialog("更新失败", e.getMessage());
        }
    }
}