package dev.pixity.friction;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import org.slf4j.Logger;

import java.nio.file.Path;

@Plugin(
        id = "friction",
        name = "Friction",
        version = "1.0.0-SNAPSHOT",
        description = "GrindMC Friction, Transfer your velocity players the right way.",
        url = "pixity.dev",
        authors = {"Pixityy"}
)
public class Friction {

    @Inject
    private ProxyServer server;

    @Inject
    private Logger logger;

    @Inject
    @DataDirectory
    private Path dataDirectory;

    private QueueManager queueManager;
    private ConfigurationManager configManager;

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        Path dataFolder = (dataDirectory != null) ? dataDirectory : Path.of("plugins/friction");

        configManager = new ConfigurationManager(dataFolder, logger);
        configManager.loadConfig();

        queueManager = new QueueManager(server, logger, this);
        queueManager.setWaveInterval(configManager.getWaveInterval());
        queueManager.setPlayersPerWave(configManager.getPlayersPerWave());
        queueManager.setTargetServer(configManager.getTargetServer());

        // Use the command manager's metaBuilder instead of CommandMeta.builder()
        CommandMeta meta = server.getCommandManager().metaBuilder("queue")
                .aliases("queuecontrol")
                .build();
        server.getCommandManager().register(meta, new QueueCommand(queueManager, logger, configManager));

        server.getEventManager().register(this, new PlayerListener(queueManager, logger));

        queueManager.startQueueProcessing();
        queueManager.startActionBarUpdates();

        logger.info("Friction plugin initialized!");
    }
}
