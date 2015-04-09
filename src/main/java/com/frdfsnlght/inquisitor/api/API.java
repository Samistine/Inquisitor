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
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.frdfsnlght.inquisitor.api;

import com.frdfsnlght.inquisitor.PlayerStats;
import com.frdfsnlght.inquisitor.TypeMap;
import java.util.Map;

/**
 *
 * @author frdfsnlght <frdfsnlght@gmail.com>
 */
public final class API {

    public boolean isPlayerStatsStarted() {
        return PlayerStats.isStarted();
    }

    public Location getPlayerLastLocation(String name) {
        //return PlayerStats.getLocation(name);
        TypeMap stats = PlayerStats.getPlayerStats(name);
        String server = stats.getString("server");
        String world = stats.getString("world");
        double[] coords = PlayerStats.decodeCoords(stats.getString("coords"));
        Location location = null;
        if ((server != null) && (world != null) && (coords != null))
            location = new Location(server, world, coords);
        return location;
    }

    public Location getPlayerBedLocation(String name) {
        //return PlayerStats.getBedLocation(name);
        TypeMap stats = PlayerStats.getPlayerStats(name);
        String server = stats.getString("bedServer");
        String world = stats.getString("bedWorld");
        double[] coords = PlayerStats.decodeCoords(stats.getString("bedCoords"));
        Location location = null;
        if ((server != null) && (world != null) && (coords != null))
            location = new Location(server, world, coords);
        return location;
    }

    public void ignorePlayerJoin(String name) {
        PlayerStats.ignorePlayerJoin(name);
    }

    public Map<String,Object> getPlayerStats(String name) {
        TypeMap stats = PlayerStats.getPlayerStats(name);
        return stats;
    }

}
