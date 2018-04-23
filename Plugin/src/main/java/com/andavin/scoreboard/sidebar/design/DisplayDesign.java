package com.andavin.scoreboard.sidebar.design;

import com.andavin.scoreboard.sidebar.Sidebar;
import com.google.common.base.Preconditions;
import org.bukkit.ChatColor;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Andavin
 * @since April 20, 2018
 */
public abstract class DisplayDesign {

    final String displayName;
    private final List<ChatColor> colors;

    /**
     * Create a new color design a {@link Sidebar} rotating through the given colors in patterns.
     *
     * @param sidebar The {@link Sidebar} that should be updated with the design.
     * @param displayName The display name that the design will be applied to.
     * @param colors The {@link ChatColor colors} to rotate between.
     * @throws IllegalArgumentException If there is not at least 1 color provided.
     */
    DisplayDesign(final Sidebar sidebar, @Nonnull final String displayName,
            @Nonnull final ChatColor... colors) throws IllegalArgumentException {
        Preconditions.checkArgument(colors.length != 0, "Must have at least 1 color.");
        this.displayName = ChatColor.stripColor(displayName);
        this.colors = new ArrayList<>(Arrays.asList(colors));
    }

    /**
     * Get the next color in rotation and rotate all of the colors.
     *
     * @return The next {@link ChatColor} in rotation.
     */
    ChatColor nextColor() {
        final ChatColor color = this.colors.remove(0);
        this.colors.add(color);
        return color;
    }

    /**
     * Get the next display name in the rotation with the applicable design.
     *
     * @return The display name.
     */
    public abstract String getRotation();
}
