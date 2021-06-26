package mc.iaiao.rpcommands;

import org.bukkit.command.CommandMap;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.util.Objects;

public class RPCommands extends JavaPlugin {
    public void onEnable() {
        saveDefaultConfig();
        for (final String cmd : getConfig().getKeys(false)) {
            if (cmd.equals("use permissions")) {
                continue;
            }
            getLogger().info("Loading command " + cmd);
            final CommandMap commands = getCommandMap();
            final org.bukkit.command.Command command = new Command(cmd, Objects.requireNonNull(getConfig().getString(cmd + ".type")), this);
            commands.register(cmd, command);
        }
    }

    public CommandMap getCommandMap() {
        try {
            Field field = getServer().getClass().getDeclaredField("commandMap");
            boolean wasAccessible = field.canAccess(getServer());
            field.setAccessible(true);
            CommandMap map = (CommandMap) field.get(getServer());
            field.setAccessible(wasAccessible);
            return map;
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
            throw new IllegalStateException("Cannot get CraftServer#commandMap");
        }
    }
}
