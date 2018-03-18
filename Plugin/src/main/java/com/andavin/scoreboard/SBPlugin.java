package com.andavin.scoreboard;

import com.andavin.scoreboard.sidebar.SidebarType;
import com.andavin.scoreboard.util.Logger;
import org.bukkit.plugin.java.JavaPlugin;

public final class SBPlugin extends JavaPlugin {

    private static SBPlugin plugin;
    private static boolean canChangeType = true;
    private static SidebarType sideBarType = SidebarType.SCORE;

    public SBPlugin() {
        plugin = this;
    }

    @Override
    public void onEnable() {
        SBPlugin.canChangeType = false;
        Logger.init(this); // Initialize the logger
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
    public static void setSideBarType(final SidebarType sideBarType) throws IllegalStateException {

        if (canChangeType) {
            SBPlugin.sideBarType = sideBarType;
        } else {
            throw new IllegalStateException("Can no longer change the SideBarType.");
        }
    }
}
