package me.taylorkelly.bigbrother;

import java.io.*;
import java.util.logging.*;
import me.taylorkelly.bigbrother.datablock.BBDataBlock;
import me.taylorkelly.bigbrother.datablock.DataBlockSender;
import me.taylorkelly.bigbrother.fixes.Fix;
import me.taylorkelly.bigbrother.fixes.Fix13;
import me.taylorkelly.bigbrother.fixes.Fix14;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.java.*;

public class BigBrother extends JavaPlugin {
    private BBPlayerListener playerListener;
    private BBBlockListener blockListener;
    private Watcher watcher;

    public static Logger log;
    public final String name = this.getDescription().getName();
    public final String version = this.getDescription().getVersion();
    public final static String premessage = ChatColor.AQUA + "[BBROTHER]: " + ChatColor.WHITE;
    public final static String directory = "BigBrother";
    
    public BigBrother(PluginLoader pluginLoader, Server instance, PluginDescriptionFile desc, File folder, File plugin, ClassLoader cLoader) {
        super(pluginLoader, instance, desc, folder, plugin, cLoader);
    }

    public void onDisable() {
        DataBlockSender.disable();
    }

    public void onEnable() {
        log = Logger.getLogger("Minecraft");
        if (!new File(directory).exists()) {
            try {
                (new File(directory)).mkdir();
            } catch (Exception e) {
                BigBrother.log.log(Level.SEVERE, "[BBROTHER]: Unable to create bigbrother/ directory");
            }
        }
        playerListener = new BBPlayerListener(this);
        blockListener = new BBBlockListener(this);
        registerEvents();
        BBSettings.initialize();
        watcher = BBSettings.getWatcher(getServer());
        BBDataBlock.initialize();
        DataBlockSender.initialize();
        Fix fix = new Fix13();
        fix.apply();
        Fix fix2 = new Fix14();
        fix2.apply();
        log.info(name + " " + version + " initialized");
    }

    private void registerEvents() {
        getServer().getPluginManager().registerEvent(Event.Type.PLAYER_COMMAND, playerListener, Priority.Normal, this);
        getServer().getPluginManager().registerEvent(Event.Type.PLAYER_JOIN, playerListener, Priority.Normal, this);
        getServer().getPluginManager().registerEvent(Event.Type.PLAYER_QUIT, playerListener, Priority.Normal, this);
        getServer().getPluginManager().registerEvent(Event.Type.PLAYER_ITEM, playerListener, Priority.Normal, this);
        getServer().getPluginManager().registerEvent(Event.Type.PLAYER_CHAT, playerListener, Priority.Normal, this);
        getServer().getPluginManager().registerEvent(Event.Type.BLOCK_PLACED, blockListener, Priority.Normal, this);
        getServer().getPluginManager().registerEvent(Event.Type.BLOCK_DAMAGED, blockListener, Priority.Normal, this);
        getServer().getPluginManager().registerEvent(Event.Type.PLAYER_TELEPORT, playerListener, Priority.Normal, this);
        getServer().getPluginManager().registerEvent(Event.Type.BLOCK_IGNITE, blockListener, Priority.Normal, this);
        getServer().getPluginManager().registerEvent(Event.Type.BLOCK_INTERACT, blockListener, Priority.Normal, this);

        // getServer().getPluginManager().registerEvent(Event.Type.BLOCK_RIGHTCLICK,
        // playerListener, Priority.Normal, this);
    }

    public boolean watching(Player player) {
        return watcher.watching(player);
    }

    public boolean toggleWatch(String player) {
        return watcher.toggleWatch(player);
    }

    public String getWatchedPlayers() {
        return watcher.getWatchedPlayers();
    }

    public boolean haveSeen(Player player) {
        return watcher.haveSeen(player);
    }

    public void markSeen(Player player) {
        watcher.markSeen(player);
    }

    public void watchPlayer(Player player) {
        watcher.watchPlayer(player);
    }

    public String getUnwatchedPlayers() {
        return watcher.getUnwatchedPlayers();
    }
}