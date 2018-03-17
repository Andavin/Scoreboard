package com.andavin.scoreboard.sidebar;

import com.andavin.scoreboard.SBPlugin;
import com.andavin.scoreboard.protocol.Scoreboard;
import com.andavin.scoreboard.util.Limiter;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created on March 13, 2018
 *
 * @author Andavin
 */
public abstract class SideBar {

    /**
     * The maximum amount of characters allowed for a single
     * line of text in a sidebar.
     */
    public static final int MAX_LINE_LENGTH;

    static {

        switch (SBPlugin.getSideBarType()) {
            case TEAM:
                MAX_LINE_LENGTH = 64;
                break;
            default:
                MAX_LINE_LENGTH = 40;
                break;
        }
    }

    public static SideBar create(final Player player, final String displayName) {
        return SideBar.create(player, displayName, new Limiter(100L, TimeUnit.MILLISECONDS));
    }

    public static SideBar create(final Player player, final String displayName, final Limiter limiter) {
        return SBPlugin.getSideBarType().newInstance(player, displayName, limiter);
    }

    final Player player;
    final String objName;
    final Limiter limiter;
    final List<String> oldLines = Collections.synchronizedList(new ArrayList<>(19));

    SideBar(@Nonnull final Player player, final String displayName, final Limiter limiter) {
        this.player = player;
        this.limiter = limiter;
        this.objName = "obj-" + Scoreboard.getNextId();
        Scoreboard.createObjective(player, displayName, this.objName, DisplaySlot.SIDEBAR);
    }

    /**
     * Set the display name of this sidebar to the given text.
     *
     * @param displayName The new display name.
     */
    public void setDisplayName(final String displayName) {

        if (!this.limiter.isLimited()) {
            this.limiter.update();
            Scoreboard.setDisplayName(player, displayName, this.objName);
        }
    }

    /**
     * Display lines of text in the sidebar for the player
     * that this is wrapping.
     * <p>
     * This method will schedule the task and then immediately
     * return. The update does not happen in this method, but
     * an unspecified amount of time after this method is called.
     *
     * @param lines The lines to send to the player.
     */
    public void display(final List<String> lines) {
        this.display(lines.toArray(new String[lines.size()]));
    }

    /**
     * Display lines of text in the sidebar for the player
     * that this is wrapping.
     * <p>
     * This method will schedule the task and then immediately
     * return. The update does not happen in this method, but
     * an unspecified amount of time after this method is called.
     *
     * @param lines The lines to send to the player.
     */
    public abstract void display(final String... lines);
}
