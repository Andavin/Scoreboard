package com.andavin.scoreboard;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.UUID;

/**
 * @since February 13, 2019
 * @author Andavin
 */
public class ScoreboardModule {

    protected boolean destroyed;
    protected final UUID uuid;
    private WeakReference<Player> player;

    protected ScoreboardModule(@Nonnull Player player) {
        this.uuid = player.getUniqueId();
        this.player = new WeakReference<>(player);
    }

    /**
     * Get the player that this objective is being
     * displayed for.
     *
     * @return The player.
     */
    @Nullable
    public Player getPlayer() {

        Player player = this.player.get();
        if (player != null) {
            return player;
        }

        Player updated = Bukkit.getPlayer(this.uuid);
        if (updated != null) {
            this.player = new WeakReference<>(updated);
        }

        return updated;
    }

    /**
     * Tell if this objective is currently destroyed
     * and cannot be used again.
     *
     * @return If this objective is destroyed.
     */
    public boolean isDestroyed() {
        return destroyed;
    }

    /**
     * Destroy this objective and delete it from the player.
     * After calling this method, this objective can no
     * longer be used.
     */
    public void destroy() {
        this.destroyed = true;
    }
}
