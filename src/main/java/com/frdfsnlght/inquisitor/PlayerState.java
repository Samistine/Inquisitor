/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.frdfsnlght.inquisitor;

import org.bukkit.Location;
import org.bukkit.block.Biome;

/**
 *
 * @author Samuel
 */
public class PlayerState {

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
