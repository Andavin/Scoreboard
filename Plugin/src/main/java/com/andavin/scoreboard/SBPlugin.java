package com.andavin.scoreboard;

import com.andavin.scoreboard.sidebar.SideBar;
import com.andavin.scoreboard.sidebar.SideBarType;
import com.andavin.scoreboard.util.Logger;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class SBPlugin extends JavaPlugin implements Listener {

    private static SBPlugin plugin;
    private static boolean canChangeType = true;
    private static SideBarType sideBarType = SideBarType.SCORE;
    private final Map<UUID, SideBar> bars = new HashMap<>();

    public SBPlugin() {
        plugin = this;
    }

    @Override
    public void onEnable() {
        SBPlugin.canChangeType = false;
        Logger.init(this); // Initialize the logger
        this.getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
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
     * Get the current {@link SideBarType} setting.
     *
     * @return The current setting.
     */
    public static SideBarType getSideBarType() {
        return sideBarType;
    }

    /**
     * Change the current {@link SideBarType} setting for the
     * Scoreboard plugin.
     * <p>
     * Default {@link SideBarType#SCORE}
     *
     * @param sideBarType The setting to change to.
     * @throws IllegalStateException If this method is called after {@link #onEnable()}.
     */
    public static void setSideBarType(final SideBarType sideBarType) throws IllegalStateException {

        if (canChangeType) {
            SBPlugin.sideBarType = sideBarType;
        } else {
            throw new IllegalStateException("Can no longer change the SideBarType.");
        }
    }

    @EventHandler
    public void onJoin(final PlayerJoinEvent event) {

        Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
            final SideBar bar = SideBar.create(event.getPlayer(), event.getPlayer().getName());
            this.bars.put(event.getPlayer().getUniqueId(), bar);
            bar.setDisplayName(event.getPlayer().getDisplayName());
        });
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMove(final PlayerMoveEvent event) {

        final Location from = event.getFrom(), to = event.getTo();
        if (from.getBlockX() == to.getBlockX() &&
            from.getBlockY() == to.getBlockY() &&
            from.getBlockZ() == to.getBlockZ()) {
            return;
        }

        final SideBar bar = this.bars.get(event.getPlayer().getUniqueId());
        if (bar != null) {
            final List<String> strings = new ArrayList<>(4);
            strings.add("X " + to.getBlockX());
            strings.add("Y " + to.getBlockY());
            strings.add("Z " + to.getBlockZ());
            if (event.getPlayer().getGameMode() == GameMode.SURVIVAL) {
                strings.add("Survive!");
            }

            bar.display(strings);
        }
    }
}
