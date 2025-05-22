package com.rex.textgen;

import org.bukkit.plugin.java.JavaPlugin;
import com.rex.textgen.commands.TextGenCommand;
import com.rex.textgen.util.TextGenerator;

public class TextGenPlugin extends JavaPlugin {
    private TextGenerator textGenerator;

    @Override
    public void onEnable() {
        // Initialize the text generator
        this.textGenerator = new TextGenerator(this);
        
        // Register command
        getCommand("textgen").setExecutor(new TextGenCommand(textGenerator));
        
        getLogger().info("TextGen plugin has been enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("TextGen plugin has been disabled!");
    }
}