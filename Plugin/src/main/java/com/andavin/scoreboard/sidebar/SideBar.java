package com.andavin.scoreboard.sidebar;

import com.andavin.scoreboard.SBPlugin;
import com.andavin.scoreboard.protocol.Scoreboard;
import com.andavin.scoreboard.util.Limiter;
import com.andavin.scoreboard.util.NoLimit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
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
     * Create a new sidebar for the given player and with the initial display name.
     * <p>
     * This default the {@link Limiter} to a limit of 100 milliseconds. This tends
     * to be a good balance for update spacing so as to disallow duplicating lines
     * in the sidebar while still being an extremely fast update speed.
     * <br>
     * This can be changed using the {@link #create(Player, String, Limiter)} method.
     *
     * @param player The player that this sidebar will be shown to.
     * @param displayName The initial display name of the sidebar.
     * @return The new {@link SideBar instance}.
     */
    @ParametersAreNonnullByDefault
    public static SideBar create(final Player player, final String displayName) {
        return SideBar.create(player, displayName, new Limiter(100L, TimeUnit.MILLISECONDS));
    }

    /**
     * Create a new sidebar for the given player and with the initial display name.
     * The {@link Limiter} can also be set to a custom setting here. Note that if
     * the limit is to be removed that {@link NoLimit} should be used over a zero
     * value (should never be {@code null}).
     *
     * @param player The player that this sidebar will be shown to.
     * @param displayName The initial display name of the sidebar.
     * @return The new {@link SideBar instance}.
     */
    @ParametersAreNonnullByDefault
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
        Scoreboard.setDisplayName(player, displayName, this.objName);
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
