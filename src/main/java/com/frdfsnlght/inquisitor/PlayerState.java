/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.frdfsnlght.inquisitor;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.block.Biome;

/**
 *
 * @author Samuel
 */
public class PlayerState {

    private static final Map<UUID, PlayerState> playerStates = Collections.synchronizedMap(new HashMap<UUID, PlayerState>());

    public static Map<UUID, PlayerState> getPlayerStates() {
        return playerStates;
    }

    long joinTime;
    float totalTimeBase;
    Location lastLocation;
    long lastTime;
    PlayerStats.TravelMode lastMode;
    Biome lastBiome;

    PlayerState(float totalTimeBase) {
        this.joinTime = System.currentTimeMillis();
        this.totalTimeBase = totalTimeBase;
        reset();
    }

    final void reset() {
        lastLocation = null;
        lastTime = 0;
        lastMode = null;
        lastBiome = null;
    }

}
