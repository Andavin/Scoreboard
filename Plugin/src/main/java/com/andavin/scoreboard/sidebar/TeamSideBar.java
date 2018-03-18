package com.andavin.scoreboard.sidebar;

import com.andavin.scoreboard.protocol.Scoreboard;
import com.andavin.scoreboard.util.Limiter;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * Created on March 16, 2018
 *
 * @author Andavin
 */
class TeamSideBar extends SideBar {

    private static final int MAX_LINE_LENGTH = 64;

    TeamSideBar(@Nonnull final Player player, final String displayName, final Limiter limiter) {
        super(player, displayName, limiter);
    }

    @Override
    public void display(final String... lines) {

        if (this.limiter.isLimited()) {
            return;
        }

        this.limiter.update();
        Scoreboard.EXECUTOR.execute(() -> {

            // Queue up the packets so that they all send at the same time
            // with minimal delay due to reflection or other things we can avoid
            final List<Object> packets = new ArrayList<>(lines.length * 2);
            int i;
            for (i = 0; i < lines.length; i++) {

                String newLine = lines[i];
                if (newLine.length() > MAX_LINE_LENGTH) {
                    newLine = newLine.substring(0, MAX_LINE_LENGTH);
                }

                final String team = "team-" + i; // This is per player we'll never have dupes this way
                final String old = i < this.oldLines.size() ? this.oldLines.get(i) : null;
                if (old != null) {

                    if (!old.equals(newLine)) {
                        this.oldLines.set(i, newLine);
                        Scoreboard.updateTeam(this.player, team, old, newLine);
                        packets.add(Scoreboard.getRemovePacket(this.objName, this.getDisplayName(old)));
                        packets.add(Scoreboard.getAddPacket(this.objName, this.getDisplayName(newLine), lines.length - i));
                    }
                } else {
                    // If there was no old line for the index
                    // that means that lines were added
                    this.oldLines.add(newLine);
                    Scoreboard.createTeam(this.player, team, newLine);
                    packets.add(Scoreboard.getAddPacket(this.objName, this.getDisplayName(newLine), lines.length - i));
                }
            }

            // If say they removed some lines from last time
            // we need to account for those and remove them
            for (; i < this.oldLines.size(); i++) {
                packets.add(0, Scoreboard.getRemovePacket(this.objName, this.getDisplayName(this.oldLines.remove(i))));
            }

            Scoreboard.sendPacket(this.player, packets);
        });
    }

    private String getDisplayName(final String line) {
        return line.length() <= 32 ? line : line.length() <= 48 ?
                line.substring(16) : line.substring(16, 48);
    }
}
