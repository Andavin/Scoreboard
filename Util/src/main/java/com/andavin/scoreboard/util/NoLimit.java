package com.andavin.scoreboard.util;

import java.util.concurrent.TimeUnit;

/**
 * Created on March 16, 2018
 *
 * @author Andavin
 */
public class NoLimit extends Limiter {

    /**
     * Create a new limiter that does not limit.
     */
    public NoLimit() {
        super(0L, TimeUnit.MILLISECONDS);
    }

    @Override
    public void update() {
    }

    @Override
    public boolean isLimited() {
        return false;
    }
}
