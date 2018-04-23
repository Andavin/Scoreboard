package com.andavin.scoreboard.sidebar.design;

import org.bukkit.ChatColor;

import javax.annotation.Nonnull;

/**
 * @author Andavin
 * @since April 20, 2018
 */
public class FlashSlideDesign extends DisplayDesign {

    private ChatColor current;

    FlashSlideDesign(@Nonnull final String displayName, @Nonnull final ChatColor... colors) throws IllegalArgumentException {
        super(null, displayName, colors);
        this.current = this.nextColor();
    }

    @Override
    public String getRotation() {
        return null;
    }
}
