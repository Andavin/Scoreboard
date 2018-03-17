package com.andavin.scoreboard.util;

import java.util.concurrent.TimeUnit;

/**
 * A class for limiting by time. An operation is measured
 * from the {@link #update() last update} and it
 * {@link #isLimited() is limited} if the last update happened
 * less than the limit ago.
 * <p>
 * If no limit is required, instead of giving a {@code null}
 * or zero value, the {@link NoLimit} limiter should be used.
 * <p>
 * Created on March 16, 2018
 *
 * @author Andavin
 */
public class Limiter {

    private long lastUpdate;
    private final long limit;

    /**
     * Create a new limiter giving the limit that needs to
     * be observed.
     *
     * @param limit The limit based on the time unit given.
     * @param unit The {@link TimeUnit unit} that should be used to translate the limit.
     */
    public Limiter(final long limit, final TimeUnit unit) {
        this.limit = unit.toMillis(limit);
    }

    /**
     * Let this limiter know that an operation has just executed.
     */
    public void update() {
        this.lastUpdate = System.currentTimeMillis();
    }

    /**
     * Tell if the operation is limited in that, if the
     * last operation execution happened less than the
     * limit ago, then the operation should be cancelled
     * for any current execution.
     *
     * @return If the operation is limited.
     */
    public boolean isLimited() {
        return System.currentTimeMillis() - this.limit < this.lastUpdate;
    }
}
