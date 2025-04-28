package com.nau_yyf.updater;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import org.update4j.Configuration;
import org.update4j.FileMetadata;
import org.update4j.service.DefaultUpdateHandler;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UpdateManager {
    private static final String GITHUB_API_URL = "https://api.github.com/repos/Auroral0810/ArmoredAssault/releases/latest";
    private static final String CONFIG_URL = "https://raw.githubusercontent.com/Auroral0810/ArmoredAssault/main/update-config.xml";
    private static final String CURRENT_VERSION = "1.0.0"; // 当前应用版本
    
    public static void checkForUpdates() {
        try {
            String latestVersion = getLatestVersion();
            
            if (isNewerVersion(latestVersion, CURRENT_VERSION)) {
                boolean shouldUpdate = showUpdateDialog(latestVersion);
                
                if (shouldUpdate) {
                    performUpdate();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private static String getLatestVersion() throws Exception {
        URL url = new URL(GITHUB_API_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        
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
    
    private static boolean showUpdateDialog(String newVersion) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("发现新版本");
        alert.setHeaderText("发现新版本 v" + newVersion);
        alert.setContentText("要立即更新吗？更新后程序将重启。");
        
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }
    
    private static void performUpdate() {
        try {
            // 从服务器获取更新配置
            URL url = new URL(CONFIG_URL);
            Configuration config = Configuration.read(url.openStream());
            
            // 执行更新
            config.update(DefaultUpdateHandler.class);
            
            // 更新完成后重启应用
            Path launcherPath = Paths.get(System.getProperty("user.dir"), "ArmoredAssault");
            ProcessBuilder pb = new ProcessBuilder(launcherPath.toString());
            pb.start();
            
            // 关闭当前应用
            Platform.exit();
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("更新失败");
            alert.setHeaderText("应用更新失败");
            alert.setContentText("错误信息: " + e.getMessage());
            alert.showAndWait();
        }
    }
} 