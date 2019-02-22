package com.andavin.scoreboard.name;

import com.andavin.scoreboard.SBPlugin;
import com.andavin.scoreboard.ScoreboardModule;
import com.andavin.scoreboard.protocol.Scoreboard;
import com.andavin.scoreboard.util.Logger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * @since February 21, 2019
 * @author Andavin
 */
public class PlayerName extends ScoreboardModule {

    public static final String METADATA = "sb-player-name";
    private final String team;
    private String prefix, suffix;

    public PlayerName(@Nonnull Player player) {

        super(player);
        this.team = "team-" + player.getName();
        List<MetadataValue> metadata = player.getMetadata(METADATA);
        if (!metadata.isEmpty()) {
            ((PlayerName) metadata.get(0).value()).destroy();
        }

        player.setMetadata(METADATA, new FixedMetadataValue(SBPlugin.getPlugin(), this));
    }

    /**
     * Update the prefix and suffix for the player to
     * every player that is currently online.
     * <p>
     * If both prefix and suffix are {@code null}, then
     * the prefixes will be removed and not updated.
     *
     * @param prefix The prefix to update to.
     * @param suffix The suffix to update to.
     */
    public void update(String prefix, String suffix) {

        if (this.destroyed) {
            throw new IllegalArgumentException("[Score] Attempting to update a destroyed sidebar.");
        }

        Player player = this.getPlayer();
        if (player == null) {
            Logger.warn("[Score] Sidebar update attempt after player has logged out.");
            return;
        }

        this.prefix = prefix != null ? prefix.length() <= 16 ? prefix : prefix.substring(0, 16) : null;
        this.suffix = suffix != null ? suffix.length() <= 16 ? suffix : suffix.substring(0, 16) : null;
        Player[] players = Bukkit.getOnlinePlayers().toArray(new Player[0]);
        for (Player online : players) {

            Scoreboard.removeTeam(online, this.team); // Remove the team just to be sure there's no conflicts
            if (prefix != null || suffix != null) {
                Scoreboard.createTeam(online, this.team, prefix, suffix, player.getDisplayName());
            }
        }
    }

    /**
     * Update the previously set prefix and suffix for
     * the player to another specific online player.
     */
    public void update(Player other) {

        if (this.destroyed) {
            throw new IllegalArgumentException("[Score] Attempting to update a destroyed sidebar.");
        }

        Player player = this.getPlayer();
        if (player == null) {
            Logger.warn("[Score] Sidebar update attempt after player has logged out.");
            return;
        }

        Scoreboard.removeTeam(other, this.team); // Remove the team just to be sure there's no conflicts
        if (prefix != null || suffix != null) {
            Scoreboard.createTeam(other, this.team, prefix, suffix, player.getDisplayName());
        }
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
