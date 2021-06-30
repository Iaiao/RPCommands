package mc.iaiao.rpcommands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.HashMap;
import java.util.Objects;

public class Command extends org.bukkit.command.Command {
    private final CommandExecutor executor;

    protected Command(final String name, final String type, final RPCommands plugin) {
        super(name, "Command /" + name, "/" + name + " <message>", Collections.emptyList());
        CommandType type1 = CommandType.valueOf(type.toUpperCase());
        if (plugin.getConfig().getBoolean("use permissions")) {
            setPermission("rpcommands." + name);
        }
        executor = switch (type1) {
            case TEXT -> new TextExecutor(
                    getString(plugin, "format"),
                    getString(plugin, "hover"),
                    getString(plugin, "click"),
                    getInt(plugin, "range"),
                    getInt(plugin, "random.default-min"),
                    getInt(plugin, "random.default-max"),
                    getBoolean(plugin, "random.input-range"),
                    getInt(plugin, "random.player-min"),
                    getInt(plugin, "random.player-max"),
                    getString(plugin, "random.error"),
                    getString(plugin, "random.invalid-player-range"));
            case RANDOM_TEXT -> new RandomTextExecutor(
                    getStringIntHashmap(plugin, "chances"),
                    getString(plugin, "hover"),
                    getString(plugin, "click"),
                    getInt(plugin, "range"),
                    getInt(plugin, "random.default-min"),
                    getInt(plugin, "random.default-max"),
                    getBoolean(plugin, "random.input-range"),
                    getInt(plugin, "random.player-min"),
                    getInt(plugin, "random.player-max"),
                    getString(plugin, "random.error"),
                    getString(plugin, "random.invalid-player-range"));
            case SPLIT -> new SplitExecutor(
                    getString(plugin, "format"),
                    getString(plugin, "hover"),
                    getString(plugin, "click"),
                    getInt(plugin, "range"),
                    getString(plugin, "split-by"),
                    getInt(plugin, "random.default-min"),
                    getInt(plugin, "random.default-max"),
                    getBoolean(plugin, "random.input-range"),
                    getInt(plugin, "random.player-min"),
                    getInt(plugin, "random.player-max"),
                    getString(plugin, "random.error"),
                    getString(plugin, "random.invalid-player-range"));
        };
    }

    private static String color(final String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    public boolean execute(final CommandSender sender, final String s, final String[] args) {
        return executor.onCommand(sender, this, s, args);
    }

    public HashMap<String, Integer> getStringIntHashmap(final RPCommands plugin, final String path) {
        final HashMap<String, Integer> map = new HashMap<>();
        for (final String key : Objects.requireNonNull(plugin.getConfig().getConfigurationSection(getName() + "." + path)).getKeys(false)) {
            map.put(ChatColor.translateAlternateColorCodes('&', key), getInt(plugin, path + "." + key));
        }
        return map;
    }

    public int getInt(final RPCommands plugin, final String path) {
        return plugin.getConfig().getInt(getName() + "." + path, -1);
    }

    public boolean getBoolean(final RPCommands plugin, final String path) {
        return plugin.getConfig().getBoolean(getName() + "." + path, false);
    }

    public String getString(final RPCommands plugin, final String path) {
        return color(Objects.requireNonNull(plugin.getConfig().getString(getName() + "." + path, "")));
    }

    private enum CommandType {
        TEXT,
        RANDOM_TEXT,
        SPLIT
    }
}
