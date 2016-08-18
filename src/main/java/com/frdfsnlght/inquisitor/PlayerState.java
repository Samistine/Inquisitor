/*
 * Copyright 2012 frdfsnlght <frdfsnlght@gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS", BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.frdfsnlght.inquisitor;

import com.frdfsnlght.inquisitor.api.TravelMode;
import org.bukkit.Location;
import org.bukkit.block.Biome;

/**
 *
 * @author Samuel Seidel
 */
public final class PlayerState {

    long joinTime;
    float totalTimeBase;
    Location lastLocation;
    long lastTime;
    TravelMode lastMode;
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
