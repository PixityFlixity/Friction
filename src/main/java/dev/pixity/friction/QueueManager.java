package dev.pixity.friction;

import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.scheduler.ScheduledTask;
import net.kyori.adventure.text.Component;
import org.slf4j.Logger;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

public class QueueManager {

    private final ProxyServer server;
    private final Logger logger;
    private final Object plugin; // plugin instance for scheduler tasks
    private final Queue<Player> playerQueue = new LinkedList<>();

    // Default settings (can be overridden by configuration)
    private int waveIntervalSeconds = 30; // seconds between waves
    private int playersPerWave = 5;         // players allowed per wave
    private String targetServerName = "lobby"; // target server name

    private boolean processing = true;
    private ScheduledTask queueTask;
    private ScheduledTask actionBarTask;

    public QueueManager(ProxyServer server, Logger logger, Object plugin) {
        this.server = server;
        this.logger = logger;
        this.plugin = plugin;
    }

    public synchronized void addPlayer(Player player) {
        if (!playerQueue.contains(player)) {
            playerQueue.offer(player);
            logger.info("Added player {} to the queue.", player.getUsername());
            updateActionBar(player);
        }
    }

    public synchronized void removePlayer(Player player) {
        if (playerQueue.remove(player)) {
            logger.info("Removed player {} from the queue.", player.getUsername());
        }
    }

    public synchronized int getPosition(Player player) {
        int position = 1;
        for (Player p : playerQueue) {
            if (p.equals(player)) {
                return position;
            }
            position++;
        }
        return -1; // player not found
    }

    // Sends an action bar update to a player with their queue position.
    public void updateActionBar(Player player) {
        int pos = getPosition(player);
        Component message = Component.text("Queue Position: " + pos + " / " + playerQueue.size());
        player.sendActionBar(message);
    }

    // Updates action bars for all players.
    public void updateAllActionBars() {
        for (Player player : playerQueue) {
            updateActionBar(player);
        }
    }

    // Starts (or restarts) the queue processing task.
    public synchronized void startQueueProcessing() {
        if (queueTask != null) {
            queueTask.cancel();
        }
        queueTask = server.getScheduler().buildTask(plugin, this::processQueue)
                .repeat(waveIntervalSeconds, TimeUnit.SECONDS)
                .schedule();
        logger.info("Queue processing started with an interval of {} seconds and {} players per wave.",
                waveIntervalSeconds, playersPerWave);
    }

    // Called to reschedule the queue task (e.g., after changing the wave interval).
    public synchronized void rescheduleQueueTask() {
        startQueueProcessing();
    }

    // Starts a repeating task to update all players' action bars every 5 seconds.
    public void startActionBarUpdates() {
        if (actionBarTask != null) {
            actionBarTask.cancel();
        }
        actionBarTask = server.getScheduler().buildTask(plugin, this::updateAllActionBars)
                .repeat(5, TimeUnit.SECONDS)
                .schedule();
        logger.info("Action bar updates scheduled every 5 seconds.");
    }

    // Processes one wave: moves up to playersPerWave players to the target server.
    public synchronized void processQueue() {
        if (!processing) return;
        logger.info("Processing queue...");
        for (int i = 0; i < playersPerWave; i++) {
            Player player = playerQueue.poll();
            if (player == null) break;

            // Get the target server.
            RegisteredServer target = server.getServer(targetServerName).orElse(null);
            if (target != null) {
                // Use connect() and check the result using isSuccessful()
                player.createConnectionRequest(target).connect().thenAccept(result -> {
                    if (result.isSuccessful()) {
                        logger.info("Player {} connected to server {}.", player.getUsername(), targetServerName);
                    } else {
                        logger.warn("Player {} failed to connect to server {}. Reason: {}",
                                player.getUsername(), targetServerName,
                                result.getReasonComponent().orElse(Component.text("Unknown")));
                    }
                });
            } else {
                logger.warn("Target server '{}' not found!", targetServerName);
            }
        }
        updateAllActionBars();
    }

    // Setters for configuration – dynamic changes trigger rescheduling where applicable.
    public void setWaveInterval(int seconds) {
        this.waveIntervalSeconds = seconds;
        logger.info("Wave interval set to {} seconds", seconds);
        rescheduleQueueTask();
    }

    public void setPlayersPerWave(int count) {
        this.playersPerWave = count;
        logger.info("Players per wave set to {}", count);
    }

    public void setTargetServer(String serverName) {
        this.targetServerName = serverName;
        logger.info("Target server set to {}", serverName);
    }

    // Getters for command/status display.
    public int getWaveInterval() {
        return waveIntervalSeconds;
    }

    public int getPlayersPerWave() {
        return playersPerWave;
    }

    public String getTargetServer() {
        return targetServerName;
    }

    public synchronized int getQueueLength() {
        return playerQueue.size();
    }

    public boolean isProcessing() {
        return processing;
    }

    public void setProcessing(boolean processing) {
        this.processing = processing;
        logger.info("Queue processing is now {}", processing ? "enabled" : "disabled");
    }
}
