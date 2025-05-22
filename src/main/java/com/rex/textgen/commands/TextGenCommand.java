package com.rex.textgen.commands;

import com.rex.textgen.util.TextGenerator;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TextGenCommand implements CommandExecutor {
    private final TextGenerator textGenerator;

    public TextGenCommand(TextGenerator textGenerator) {
        this.textGenerator = textGenerator;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cThis command can only be used by players!");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            player.sendMessage(
                    "§cUsage:\n§f/textgen <text> <block> <size> <spacing> <width> <style> [outline]\n§f/textgen undo");
            return true;
        }

        // Handle undo command
        if (args.length == 1 && args[0].equalsIgnoreCase("undo")) {
            if (textGenerator.undoLastGeneration(player)) {
                player.sendMessage("§aLast text generation has been removed!");
            } else {
                player.sendMessage("§cNo previous text generation found to undo!");
            }
            return true;
        }

        // Handle text generation command
        if (args.length != 6 && args.length != 7) {
            player.sendMessage("§cUsage: /textgen <text> <block> <size> <spacing> <width> <style> [outline]");
            return true;
        }

        try {
            String text = args[0];
            Material blockType;
            try {
                blockType = Material.valueOf(args[1].toUpperCase());
                if (!blockType.isBlock()) {
                    player.sendMessage("§cSpecified material must be a block!");
                    return true;
                }
            } catch (IllegalArgumentException e) {
                player.sendMessage("§cInvalid block type!");
                return true;
            }

            int size = Integer.parseInt(args[2]);
            int spacing = Integer.parseInt(args[3]);
            int width = Integer.parseInt(args[4]);
            String style = args[5].toUpperCase();

            Material outlineType = null;
            if (args.length == 7) {
                try {
                    outlineType = Material.valueOf(args[6].toUpperCase());
                    if (!outlineType.isBlock()) {
                        player.sendMessage("§cOutline material must be a block!");
                        return true;
                    }
                } catch (IllegalArgumentException e) {
                    player.sendMessage("§cInvalid outline block type!");
                    return true;
                }
            }

            if (size <= 0 || spacing < 0 || width <= 0) {
                player.sendMessage("§cSize, spacing, and width must be positive numbers!");
                return true;
            }

            if (!style.equals("B")) {
                player.sendMessage("§cInvalid style! Currently only 'B' (bold) is supported.");
                return true;
            }

            // Generate the text
            boolean success = textGenerator.generateText(player, text, blockType, size, spacing, width, style,
                    outlineType);

            if (success) {
                player.sendMessage("§aText generation completed successfully! Use /textgen undo to remove it.");
            } else {
                player.sendMessage("§cFailed to generate text. Make sure you're looking at a valid block!");
            }

        } catch (NumberFormatException e) {
            player.sendMessage("§cSize, spacing, and width must be valid numbers!");
            return true;
        }

        return true;
    }
}