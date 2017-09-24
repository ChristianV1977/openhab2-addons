/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.elerotransmitterstick.handler;

import static org.openhab.binding.elerotransmitterstick.EleroTransmitterStickBindingConstants.*;

import java.util.ArrayList;

import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.StopMoveType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.library.types.UpDownType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.elerotransmitterstick.stick.CommandType;
import org.openhab.binding.elerotransmitterstick.stick.ResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link EleroChannelHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Volker Bier - Initial contribution
 */
public class EleroChannelHandler extends BaseThingHandler implements StatusListener {
    private final Logger logger = LoggerFactory.getLogger(EleroChannelHandler.class);

    protected ArrayList<Integer> channelIds;
    protected EleroTransmitterStickHandler bridge;

    public EleroChannelHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        bridge = (EleroTransmitterStickHandler) getBridge().getHandler();

        setChannelIds();
        for (Integer channelId : channelIds) {
            bridge.addStatusListener(channelId, this);
        }

        if (bridge.getThing().getStatus() != ThingStatus.ONLINE) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
        }
    }

    protected void setChannelIds() {
        String channelIdStr = getThing().getProperties().get(PROPERTY_CHANNEL_ID);
        channelIds = new ArrayList<>();
        channelIds.add(Integer.valueOf(channelIdStr));
    }

    @Override
    public void dispose() {
        for (Integer channelId : channelIds) {
            bridge.removeStatusListener(channelId, this);
        }
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        if (bridgeStatusInfo.getStatus() == ThingStatus.OFFLINE) {
            logger.debug("Bridge for Elero channel handler for thing {} ({}) changed status to {}",
                    getThing().getLabel(), getThing().getUID(), bridgeStatusInfo.getStatus().toString());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Received command {} for channel {}", command, channelUID);

        if (channelUID.getIdWithoutGroup().equals(CONTROL_CHANNEL)) {
            if (command == UpDownType.UP) {
                bridge.getStick().sendCommand(CommandType.UP, channelIds);
            } else if (command == UpDownType.DOWN) {
                bridge.getStick().sendCommand(CommandType.DOWN, channelIds);
            } else if (command == StopMoveType.STOP) {
                bridge.getStick().sendCommand(CommandType.STOP, channelIds);
            } else if (command instanceof PercentType) {
                CommandType cmd = CommandType.getForPercent(((PercentType) command).intValue());
                if (cmd != null) {
                    bridge.getStick().sendCommand(cmd, channelIds);
                } else {
                    logger.warn("Unhandled command {}.", command);
                }
            } else if (command == RefreshType.REFRESH) {
                bridge.getStick().requestUpdate(channelIds);
            }
        }
    }

    @Override
    public void statusChanged(int channelId, ResponseStatus status) {
        logger.debug("Received updated state {} for thing {}", status, getThing().getUID().toString());

        updateState(STATUS_CHANNEL, new StringType(status.toString()));

        int percentage = ResponseStatus.getPercentageFor(status);
        if (percentage != -1) {
            updateState(CONTROL_CHANNEL, new PercentType(percentage));
        }

        updateStatus(ThingStatus.ONLINE);
    }
}
