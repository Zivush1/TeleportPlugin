package me.zivush.teleportplugin;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.UUID;

public class TeleportPlugin extends JavaPlugin {

    private FileConfiguration config;
    private HashMap<UUID, Long> cooldowns = new HashMap<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        config = getConfig();
        getLogger().info("TeleportPlugin has been enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("TeleportPlugin has been disabled!");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;

        if (command.getName().equalsIgnoreCase("tp")) {
            if (!player.hasPermission("teleportplugin.tp")) {
                player.sendMessage(colorize(config.getString("messages.no-permission")));
                return true;
            }

            if (args.length != 3) {
                player.sendMessage(colorize(config.getString("messages.usage")));
                return true;
            }

            if (isOnCooldown(player)) {
                player.sendMessage(colorize(config.getString("messages.cooldown")
                        .replace("%time%", String.valueOf(getRemainingCooldown(player)))));
                return true;
            }

            try {
                double x = Double.parseDouble(args[0]);
                double y = Double.parseDouble(args[1]);
                double z = Double.parseDouble(args[2]);

                Location location = new Location(player.getWorld(), x, y, z);
                player.teleport(location);
                player.sendMessage(colorize(config.getString("messages.teleported")
                        .replace("%x%", args[0])
                        .replace("%y%", args[1])
                        .replace("%z%", args[2])));

                setCooldown(player);
            } catch (NumberFormatException e) {
                player.sendMessage(colorize(config.getString("messages.invalid-coordinates")));
            }

            return true;
        }

        return false;
    }

    private String colorize(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    private boolean isOnCooldown(Player player) {
        if (!cooldowns.containsKey(player.getUniqueId())) {
            return false;
        }
        return System.currentTimeMillis() - cooldowns.get(player.getUniqueId()) < config.getInt("cooldown") * 1000L;
    }

    private void setCooldown(Player player) {
        cooldowns.put(player.getUniqueId(), System.currentTimeMillis());
    }

    private long getRemainingCooldown(Player player) {
        long timeElapsed = System.currentTimeMillis() - cooldowns.get(player.getUniqueId());
        return (config.getInt("cooldown") * 1000L - timeElapsed) / 1000L;
    }
}
