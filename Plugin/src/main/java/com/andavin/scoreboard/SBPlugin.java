package com.andavin.scoreboard;

import com.andavin.scoreboard.name.PlayerName;
import com.andavin.scoreboard.sidebar.SidebarType;
import com.andavin.scoreboard.util.Logger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public final class SBPlugin extends JavaPlugin implements Listener {

    private static SBPlugin plugin;
    private static boolean canChangeType = true;
    private static SidebarType sideBarType = SidebarType.SCORE;

    public SBPlugin() {
        plugin = this;
    }

    @Override
    public void onEnable() {
        canChangeType = false;
        Logger.init(this); // Initialize the logger
        this.saveDefaultConfig();
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    /**
     * Get the main instance of the Scoreboard plugin.
     *
     * @return The main plugin instance.
     */
    public static SBPlugin getPlugin() {
        return plugin;
    }

    /**
     * Get the current {@link SidebarType} setting.
     *
     * @return The current setting.
     */
    public static SidebarType getSideBarType() {
        return sideBarType;
    }

    /**
     * Change the current {@link SidebarType} setting for the
     * Scoreboard plugin.
     * <p>
     * Default {@link SidebarType#SCORE}
     *
     * @param sideBarType The setting to change to.
     * @throws IllegalStateException If this method is called after {@link #onEnable()}.
     */
    public static void setSideBarType(SidebarType sideBarType) throws IllegalStateException {

        if (canChangeType) {
            SBPlugin.sideBarType = sideBarType;
        } else {
            throw new IllegalStateException("Can no longer change the SideBarType.");
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {

        Player player = event.getPlayer();
        new PlayerName(player).update("Prefix", "Suffix");

        Player[] players = Bukkit.getOnlinePlayers().toArray(new Player[0]);
        for (Player online : players) {

            List<MetadataValue> metadata = online.getMetadata(PlayerName.METADATA);
            if (!metadata.isEmpty()) {
                ((PlayerName) metadata.get(0).value()).update(player);
            }
        }
    }
}
