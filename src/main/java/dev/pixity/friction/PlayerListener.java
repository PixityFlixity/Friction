package dev.pixity.friction;

import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.proxy.Player;
import org.slf4j.Logger;

public class PlayerListener {

    private final QueueManager queueManager;
    private final Logger logger;

    public PlayerListener(QueueManager queueManager, Logger logger) {
        this.queueManager = queueManager;
        this.logger = logger;
    }

    @Subscribe
    public void onPlayerDisconnect(DisconnectEvent event) {
        Player player = event.getPlayer();
        queueManager.removePlayer(player);
        logger.info("Player {} disconnected and was removed from the queue.", player.getUsername());
    }
}
