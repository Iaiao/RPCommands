package mc.iaiao.rpcommands;

import java.util.*;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.ChatColor;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.Bukkit;

import java.util.stream.Collectors;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.CommandExecutor;

public class RandomTextExecutor implements CommandExecutor {
    private final HashMap<String, Integer> formats;
    private final String hoverFormat;
    private final String suggestCommand;
    private final int range;
    private final int randomDefaultMin;
    private final int randomDefaultMax;
    private final boolean randomInputRange;
    private final int randomPlayerMin;
    private final int randomPlayerMax;
    private final String randomError;
    private final String randomInvalidNumber;

    RandomTextExecutor(final HashMap<String, Integer> formats, String hoverFormat, String suggestCommand, final int range, final int randomDefaultMin, final int randomDefaultMax, final boolean randomInputRange, final int randomPlayerMin, final int randomPlayerMax, final String randomError, final String randomInvalidNumber) {
        this.formats = formats;
        this.hoverFormat = hoverFormat;
        this.suggestCommand = suggestCommand;
        this.range = range;
        this.randomDefaultMin = randomDefaultMin;
        this.randomDefaultMax = randomDefaultMax;
        this.randomInputRange = randomInputRange;
        this.randomPlayerMin = randomPlayerMin;
        this.randomPlayerMax = randomPlayerMax;
        this.randomError = randomError;
        this.randomInvalidNumber = randomInvalidNumber;
    }

    public boolean onCommand(final CommandSender sender, final Command cmd, final String s, final String[] args) {
        String message = Arrays.stream(args).skip(randomInputRange ? 2L : 0L).collect(Collectors.joining(" "));
        List<CommandSender> players = new ArrayList<>(Bukkit.getOnlinePlayers());
        if (sender instanceof Player && range > 0) {
            players = ((Player) sender).getNearbyEntities(range, range, range).stream().filter(e -> e.getType() == EntityType.PLAYER).collect(Collectors.toList());
            players.add(sender);
        }
        int i = (int) Math.floor(Math.random() * formats.values().stream().reduce(Integer::sum).orElseThrow());
        String format = "";
        for (final Map.Entry<String, Integer> e2 : formats.entrySet()) {
            i -= e2.getValue();
            if (i <= 0) {
                format = e2.getKey();
                break;
            }
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
