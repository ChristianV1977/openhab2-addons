/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.elerotransmitterstick.handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.openhab.binding.elerotransmitterstick.config.EleroGroupConfig;
import org.openhab.binding.elerotransmitterstick.stick.ResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link EleroGroupHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Volker Bier - Initial contribution
 */
public class EleroGroupHandler extends EleroChannelHandler {
    private final Logger logger = LoggerFactory.getLogger(EleroGroupHandler.class);

    Map<Integer, ResponseStatus> channelStatusMap = new HashMap<>();

    public EleroGroupHandler(Thing thing) {
        super(thing);
    }

    @Override
    protected void setChannelIds() {
        channelIds = parseChannelIds(getConfig().as(EleroGroupConfig.class).channelids);
        channelStatusMap.clear();
    }

    private ArrayList<Integer> parseChannelIds(String channelids) {
        String[] idsArr = channelids.split(",");
        ArrayList<Integer> ids = new ArrayList<>();

        for (String idStr : idsArr) {
            try {
                int id = Integer.parseInt(idStr);

                if (id > 0 && id < 16) {
                    ids.add(id);
                } else {
                    throw new IllegalArgumentException(
                            "id " + idStr + " specified in thing configuration is out of range 1..15");
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("invalid id " + idStr + " specified in thing configuration");
            }
        }

        return ids;
    }

    @Override
    public void statusChanged(int channelId, ResponseStatus respStatus) {
        logger.debug("Received updated state {} for thing {}", respStatus, getThing().getUID().toString());

        if (channelIds.contains(channelId)) {
            channelStatusMap.put(channelId, respStatus);
        }

        ResponseStatus commonStatus = null;
        for (ResponseStatus channelStatus : channelStatusMap.values()) {
            if (commonStatus == null) {
                commonStatus = channelStatus;
            } else if (commonStatus != channelStatus) {
                commonStatus = null;
                break;
            }
        }

        // if all channels have the same status use this as the group status. otherwise return NO_INFORMATION
        if (commonStatus != null) {
            super.statusChanged(channelId, commonStatus);
        } else {
            super.statusChanged(channelId, ResponseStatus.NO_INFORMATION);
        }

        updateStatus(ThingStatus.ONLINE);
    }
}
