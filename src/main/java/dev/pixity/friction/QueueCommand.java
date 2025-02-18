package dev.pixity.friction;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;
import org.slf4j.Logger;

public class QueueCommand implements SimpleCommand {

    private final QueueManager queueManager;
    private final Logger logger;
    private final ConfigurationManager configManager;

    public QueueCommand(QueueManager queueManager, Logger logger, ConfigurationManager configManager) {
        this.queueManager = queueManager;
        this.logger = logger;
        this.configManager = configManager;
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource sender = invocation.source();
        String[] args = invocation.arguments();

        // Check permission for players (console is allowed).
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (!player.hasPermission("grindvelocity.queue")) {
                player.sendMessage(Component.text("You do not have permission to execute this command."));
                return;
            }
        }

        if (args.length == 0) {
            sender.sendMessage(Component.text("Queue commands: add, remove, start, stop, setwaveinterval <seconds>, setplayersperwave <number>, settarget <server>, status, reload"));
            return;
        }

        String subCommand = args[0].toLowerCase();
        switch (subCommand) {
            case "add":
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    queueManager.addPlayer(player);
                    sender.sendMessage(Component.text("You have been added to the queue."));
                } else {
                    sender.sendMessage(Component.text("Only players can add themselves to the queue."));
                }
                break;
            case "remove":
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    queueManager.removePlayer(player);
                    sender.sendMessage(Component.text("You have been removed from the queue."));
                } else {
                    sender.sendMessage(Component.text("Only players can remove themselves from the queue."));
                }
                break;
            case "start":
                queueManager.setProcessing(true);
                sender.sendMessage(Component.text("Queue processing started."));
                break;
            case "stop":
                queueManager.setProcessing(false);
                sender.sendMessage(Component.text("Queue processing stopped."));
                break;
            case "setwaveinterval":
                if (args.length < 2) {
                    sender.sendMessage(Component.text("Usage: /queue setwaveinterval <seconds>"));
                } else {
                    try {
                        int seconds = Integer.parseInt(args[1]);
                        queueManager.setWaveInterval(seconds);
                        sender.sendMessage(Component.text("Wave interval set to " + seconds + " seconds."));
                    } catch (NumberFormatException e) {
                        sender.sendMessage(Component.text("Invalid number for seconds."));
                    }
                }
                break;
            case "setplayersperwave":
                if (args.length < 2) {
                    sender.sendMessage(Component.text("Usage: /queue setplayersperwave <number>"));
                } else {
                    try {
                        int count = Integer.parseInt(args[1]);
                        queueManager.setPlayersPerWave(count);
                        sender.sendMessage(Component.text("Players per wave set to " + count + "."));
                    } catch (NumberFormatException e) {
                        sender.sendMessage(Component.text("Invalid number for players per wave."));
                    }
                }
                break;
            case "settarget":
                if (args.length < 2) {
                    sender.sendMessage(Component.text("Usage: /queue settarget <server>"));
                } else {
                    String serverName = args[1];
                    queueManager.setTargetServer(serverName);
                    sender.sendMessage(Component.text("Target server set to " + serverName + "."));
                }
                break;
            case "status":
                sender.sendMessage(Component.text(
                        "Queue Status:\n" +
                                "Processing: " + (queueManager.isProcessing() ? "Enabled" : "Disabled") + "\n" +
                                "Wave Interval: " + queueManager.getWaveInterval() + " seconds\n" +
                                "Players per Wave: " + queueManager.getPlayersPerWave() + "\n" +
                                "Target Server: " + queueManager.getTargetServer() + "\n" +
                                "Queue Length: " + queueManager.getQueueLength()
                ));
                break;
            case "reload":
                // Reload configuration from file and update queue settings.
                configManager.loadConfig();
                queueManager.setWaveInterval(configManager.getWaveInterval());
                queueManager.setPlayersPerWave(configManager.getPlayersPerWave());
                queueManager.setTargetServer(configManager.getTargetServer());
                queueManager.rescheduleQueueTask();
                sender.sendMessage(Component.text("Configuration reloaded."));
                break;
            default:
                sender.sendMessage(Component.text("Unknown subcommand. Available: add, remove, start, stop, setwaveinterval, setplayersperwave, settarget, status, reload"));
                break;
        }
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        if (invocation.source() instanceof Player) {
            return invocation.source().hasPermission("grindvelocity.queue");
        }
        return true;
    }
}
