/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.elerotransmitterstick.stick;

import java.util.Arrays;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * @author Volker Bier - Initial contribution
 */
public class Command implements Delayed {
    public static final int TIMED_PRIORITY = 30;
    public static final int COMMAND_PRIORITY = 20;
    public static final int FAST_INFO_PRIORITY = 10;
    public static final int INFO_PRIORITY = 0;

    private int[] channelId;
    private CommandType commandType;

    protected int priority = COMMAND_PRIORITY;

    public Command(final CommandType cmd, final int... channels) {
        channelId = channels;
        commandType = cmd;
    }

    @Override
    public String toString() {
        return "Command " + commandType + " on channels " + Arrays.toString(channelId) + " with priority " + priority;
    }

    @Override
    public int compareTo(Delayed delayed) {
        if (delayed == this) {
            return 0;
        }

        long d = 0 - delayed.getDelay(TimeUnit.MILLISECONDS);
        return ((d == 0) ? 0 : ((d < 0) ? -1 : 1));
    }

    @Override
    public long getDelay(TimeUnit unit) {
        return 0;
    }

    public int getPriority() {
        return priority;
    }

    public int[] getChannelIds() {
        return channelId;
    }

    public CommandType getCommandType() {
        return commandType;
    }
}
