package com.andavin.scoreboard.name;

import com.andavin.scoreboard.SBPlugin;
import com.andavin.scoreboard.ScoreboardModule;
import com.andavin.scoreboard.protocol.Scoreboard;
import com.andavin.scoreboard.util.Logger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.scoreboard.DisplaySlot;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * @since February 13, 2019
 * @author Andavin
 */
public class BelowName extends ScoreboardModule {

    public static final String METADATA = "sb-below-name";
    private int score;
    private String displayName;
    private final String objName;

    public BelowName(@Nonnull Player player) {

        super(player);
        this.objName = "obj-" + Scoreboard.getNextId();
        List<MetadataValue> metadata = player.getMetadata(METADATA);
        if (!metadata.isEmpty()) {
            ((BelowName) metadata.get(0).value()).destroy();
        }

        player.setMetadata(METADATA, new FixedMetadataValue(SBPlugin.getPlugin(), this));
    }

    /**
     * Set the display name of the objective beneath the
     * player's name that shows for all other players on
     * the server.
     *
     * @param displayName The display name to set to.
     */
    public void setDisplayName(String displayName) {

        if (this.destroyed) {
            throw new IllegalArgumentException("[Below Name] Attempting to update a destroyed sidebar.");
        }

        Player player = this.getPlayer();
        if (player == null) {
            Logger.warn("[Below Name] Sidebar update attempt after player has logged out.");
            return;
        }

        this.displayName = displayName;
        Player[] players = Bukkit.getOnlinePlayers().toArray(new Player[0]);
        for (Player online : players) {

            if (!online.equals(player)) {
                Scoreboard.setDisplayName(online, displayName, this.objName);
            }
        }
    }

    /**
     * Set the score that shows in front of the display
     * name beneath the player's name.
     *
     * @param score The score to set to.
     */
    public void setScore(int score) {

        if (this.destroyed) {
            throw new IllegalArgumentException("[Below Name] Attempting to update a destroyed sidebar.");
        }

        Player player = this.getPlayer();
        if (player == null) {
            Logger.warn("[Below Name] Sidebar update attempt after player has logged out.");
            return;
        }

        this.score = score;
        Player[] players = Bukkit.getOnlinePlayers().toArray(new Player[0]);
        for (Player online : players) {

            if (!online.equals(player)) {
                Scoreboard.sendPacket(online, Scoreboard.getAddPacket(this.objName, player.getDisplayName(), score));
            }
        }
    }

    /**
     * Update the previously set display name and score
     * for the player to another specific online player.
     */
    public void update(Player other) {

        if (this.destroyed) {
            throw new IllegalArgumentException("[Below Name] Attempting to update a destroyed sidebar.");
        }

        Player player = this.getPlayer();
        if (player == null) {
            Logger.warn("[Below Name] Sidebar update attempt after player has logged out.");
            return;
        }

        Scoreboard.deleteObjective(other, this.objName); // Prevent duplicate objectives
        Scoreboard.createObjective(other, this.displayName, this.objName, DisplaySlot.BELOW_NAME);
        Scoreboard.sendPacket(other, Scoreboard.getAddPacket(this.objName, player.getDisplayName(), this.score));
    }

    @Override
    public void destroy() {

        super.destroy();
        Player player = this.getPlayer();
        if (player != null) {
            player.removeMetadata(METADATA, SBPlugin.getPlugin());
        }
    }
}
