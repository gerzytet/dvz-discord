package com.github.gerzytet.dvzdiscord;

import java.util.logging.Logger;

import org.bukkit.plugin.java.JavaPlugin;

public class DvzBotPlugin extends JavaPlugin {
    @Override
    public void onEnable(){
    	DvzBot.setLogger(getLogger());
    }

    @Override
    public void onDisable(){
    }
}
