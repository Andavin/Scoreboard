package com.andavin.scoreboard;

import com.andavin.scoreboard.sidebar.SideBarType;
import com.andavin.scoreboard.util.Logger;
import org.bukkit.plugin.java.JavaPlugin;

public final class SBPlugin extends JavaPlugin {

    private static SBPlugin plugin;
    private static boolean canChangeType = true;
    private static SideBarType sideBarType = SideBarType.TEAM;

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
}
