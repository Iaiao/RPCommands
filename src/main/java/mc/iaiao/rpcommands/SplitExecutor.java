package mc.iaiao.rpcommands;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class SplitExecutor implements CommandExecutor {
    private final String format;
    private final String hoverFormat;
    private final String suggestCommand;
    private final int range;
    private final String splitBy;
    private final int randomDefaultMin;
    private final int randomDefaultMax;
    private final boolean randomInputRange;
    private final int randomPlayerMin;
    private final int randomPlayerMax;
    private final String randomError;
    private final String randomInvalidNumber;

    SplitExecutor(String format, String hoverFormat, String suggestCommand, int range, String splitBy, int randomDefaultMin, int randomDefaultMax, boolean randomInputRange, int randomPlayerMin, int randomPlayerMax, String randomError, String randomInvalidNumber) {
        this.format = format;
        this.hoverFormat = hoverFormat;
        this.suggestCommand = suggestCommand;
        this.range = range;
        this.splitBy = splitBy;
        this.randomDefaultMin = randomDefaultMin;
        this.randomDefaultMax = randomDefaultMax;
        this.randomInputRange = randomInputRange;
        this.randomPlayerMin = randomPlayerMin;
        this.randomPlayerMax = randomPlayerMax;
        this.randomError = randomError;
        this.randomInvalidNumber = randomInvalidNumber;
    }

    private static String replace(String input, Pattern regex, Function<Matcher, String> callback) {
        StringBuilder resultString = new StringBuilder();
        Matcher regexMatcher = regex.matcher(input);
        while (regexMatcher.find()) {
            regexMatcher.appendReplacement(resultString, callback.apply(regexMatcher));
        }
        regexMatcher.appendTail(resultString);
        return resultString.toString();
    }

    public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {
        String message = Arrays.stream(args).skip(randomInputRange ? 2L : 0L).collect(Collectors.joining(" "));
        String[] messages = message.split(splitBy);
        List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
        if (sender instanceof Player && range > 0) {
            players = ((Player) sender).getNearbyEntities(range, range, range).stream().filter(e -> e.getType() == EntityType.PLAYER).map(p -> (Player) p).collect(Collectors.toList());
            players.add((Player) sender);
        }
        String msg = format.replaceAll("\\{player}", sender instanceof Player ? ((Player) sender).getDisplayName() : sender.getName());
        try {
            int rndMin = randomInputRange && args.length >= 2 ? Integer.parseInt(args[0]) : randomDefaultMin;
            int rndMax = randomInputRange && args.length >= 2 ? Integer.parseInt(args[1]) + 1 : randomDefaultMax;
            if (rndMin < randomPlayerMin || rndMax - 1 > randomPlayerMax) {
                sender.sendMessage(randomInvalidNumber);
                return true;
            }
            while (msg.contains("{random}")) {
                msg = msg.replaceFirst("\\{random}", String.valueOf((int) Math.floor((double) rndMin + Math.random() * (double) (rndMax - rndMin))));
            }
            msg = msg.replaceAll("\\{fixedrandom}", String.valueOf((int) Math.floor((double) rndMin + Math.random() * (double) (rndMax - rndMin))));
        } catch (NumberFormatException ignored) {
            sender.sendMessage(randomError);
            return true;
        }
        msg = SplitExecutor.replace(msg, Pattern.compile("\\{message \\d}"), m -> {
            String a = m.group();
            int n = Integer.parseInt(a.substring("{message ".length(), a.length() - "}".length()));
            if (messages.length < n) {
                return "";
            }
            return messages[n - 1];
        });
        msg = msg.replaceAll("\\{message}", message);
        TextComponent component = new TextComponent(msg);
        if (!hoverFormat.isEmpty()) component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(
                ChatColor.translateAlternateColorCodes('&',
                        hoverFormat
                                .replaceAll("\\{message}", message)
                                .replaceAll("\\{player}", sender instanceof Player ? ((Player) sender).getDisplayName() : sender.getName())
                                .replaceAll("\\{finalMessage}", msg)))));
        if (!suggestCommand.isEmpty()) component.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND,
                suggestCommand
                        .replaceAll("\\{message}", message.replaceAll("\u00A7[a-fA-F0-9k-oK-O]", ""))
                        .replaceAll("\\{player}", sender instanceof Player ? ((Player) sender).getDisplayName() : sender.getName())
                        .replaceAll("\\{finalMessage}", msg)));
        players.stream().filter(p -> p.hasPermission("rpcommands." + cmd.getName() + ".hear")).forEach(p -> p.spigot().sendMessage(component));
        return true;
    }
}

