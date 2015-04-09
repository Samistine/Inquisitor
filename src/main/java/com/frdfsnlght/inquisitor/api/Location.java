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

/**
 *
 * @author frdfsnlght <frdfsnlght@gmail.com>
 */
public final class Location {

    private String server;
    private String world;
    private double[] coords;

    public Location(String server, String world, double[] coords) {
        this.server = server;
        this.world = world;
        this.coords = coords;
    }

    public Location(String server, String world, double x, double y, double z) {
        this(server, world, new double[] {x, y, z});
    }

    public String getServer() { return server; }
    public String getWorld() { return world; }
    public double[] getCoords() { return coords; }
    public double getX() { return coords[0]; }
    public double getY() { return coords[1]; }
    public double getZ() { return coords[2]; }

}
