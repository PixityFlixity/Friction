package dev.pixity.friction;

import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;

public class ConfigurationManager {
    private final Path configFile;
    private final Logger logger;
    private int waveInterval = 30;
    private int playersPerWave = 5;
    private String targetServer = "lobby";

    public ConfigurationManager(Path dataDirectory, Logger logger) {
        this.logger = logger;
        if (!Files.exists(dataDirectory)) {
            try {
                Files.createDirectories(dataDirectory);
            } catch (IOException e) {
                logger.error("Failed to create data directory", e);
            }
        }
        this.configFile = dataDirectory.resolve("config.properties");
    }

    public void loadConfig() {
        if (Files.exists(configFile)) {
            try (InputStream in = Files.newInputStream(configFile)) {
                java.util.Properties props = new java.util.Properties();
                props.load(in);
                waveInterval = Integer.parseInt(props.getProperty("waveInterval", "30"));
                playersPerWave = Integer.parseInt(props.getProperty("playersPerWave", "5"));
                targetServer = props.getProperty("targetServer", "lobby");
                logger.info("Configuration loaded: waveInterval={}, playersPerWave={}, targetServer={}",
                        waveInterval, playersPerWave, targetServer);
            } catch (IOException e) {
                logger.error("Failed to load configuration", e);
            }
        } else {
            saveDefaultConfig();
        }
    }

    public void saveDefaultConfig() {
        try {
            java.util.Properties props = new java.util.Properties();
            props.setProperty("waveInterval", String.valueOf(waveInterval));
            props.setProperty("playersPerWave", String.valueOf(playersPerWave));
            props.setProperty("targetServer", targetServer);
            Files.write(configFile, () -> props.entrySet().stream()
                    .<CharSequence>map(e -> e.getKey() + "=" + e.getValue())
                    .iterator());
            logger.info("Default configuration saved to {}", configFile.toAbsolutePath());
        } catch (IOException e) {
            logger.error("Failed to save default configuration", e);
        }
    }

    public int getWaveInterval() {
        return waveInterval;
    }

    public int getPlayersPerWave() {
        return playersPerWave;
    }

    public String getTargetServer() {
        return targetServer;
    }
}
