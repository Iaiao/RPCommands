package mc.iaiao.rpcommands;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public record TextExecutor(String format, String hoverFormat, String suggestCommand,
                           int range, int randomDefaultMin, int randomDefaultMax, boolean randomInputRange,
                           int randomPlayerMin, int randomPlayerMax, String randomError,
                           String randomInvalidNumber) implements CommandExecutor {

    public boolean onCommand(final CommandSender sender, final Command cmd, final String s, final String[] args) {
        String message = Arrays.stream(args).skip(randomInputRange ? 2L : 0L).collect(Collectors.joining(" "));
        List<CommandSender> players = new ArrayList<>(Bukkit.getOnlinePlayers());
        if (sender instanceof Player && range > 0) {
            players = ((Player) sender).getNearbyEntities(range, range, range).stream().filter(e -> e.getType() == EntityType.PLAYER).collect(Collectors.toList());
            players.add(sender);
        }
        String msg = format.replaceAll("\\{player}", sender instanceof Player ? ((Player) sender).getDisplayName() : sender.getName());
        try {
            int rndMin = (randomInputRange && args.length >= 2) ? Integer.parseInt(args[0]) : randomDefaultMin;
            int rndMax = (randomInputRange && args.length >= 2) ? (Integer.parseInt(args[1]) + 1) : randomDefaultMax;
            if (rndMin < randomPlayerMin || rndMax - 1 > randomPlayerMax) {
                sender.sendMessage(randomInvalidNumber);
                return true;
            }
            while (msg.contains("{random}")) {
                msg = msg.replaceFirst("\\{random}", String.valueOf((int) Math.floor(rndMin + Math.random() * (rndMax - rndMin))));
            }
            msg = msg.replaceAll("\\{fixedrandom}", String.valueOf((int) Math.floor(rndMin + Math.random() * (rndMax - rndMin))));
        } catch (NumberFormatException exception) {
            sender.sendMessage(randomError);
            return true;
        }
        msg = msg.replaceAll("\\{message}", message);
        TextComponent component = new TextComponent(msg);
        if (!hoverFormat.isEmpty()) component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(
                hoverFormat
                        .replaceAll("\\{message}", message)
                        .replaceAll("\\{player}", sender instanceof Player ? ((Player) sender).getDisplayName() : sender.getName())
                        .replaceAll("\\{finalMessage}", msg))));
        if (!suggestCommand.isEmpty()) component.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND,
                suggestCommand
                        .replaceAll("\\{message}", message.replaceAll("\u00A7[a-fA-F0-9k-oK-O]", ""))
                        .replaceAll("\\{player}", sender instanceof Player ? ((Player) sender).getDisplayName() : sender.getName())
                        .replaceAll("\\{finalMessage}", msg)));
        players.stream().filter(p -> p.hasPermission("rpcommands." + cmd.getName() + ".hear")).forEach(p -> p.spigot().sendMessage(component));
        return true;
    }
}
