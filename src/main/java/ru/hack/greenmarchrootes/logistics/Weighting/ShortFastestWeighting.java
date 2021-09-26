/*
 *  Licensed to GraphHopper GmbH under one or more contributor
 *  license agreements. See the NOTICE file distributed with this work for
 *  additional information regarding copyright ownership.
 *
 *  GraphHopper GmbH licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except in
 *  compliance with the License. You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package ru.hack.greenmarchrootes.logistics.Weighting;

import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.weighting.FastestWeighting;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.NodeAccess;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.PMap;

import ru.hack.greenmarchrootes.model.Area;

import java.util.List;

import static ru.hack.greenmarchrootes.logistics.Weighting.TurnCostProvider.NO_TURN_COST_PROVIDER;

/**
 * Calculates the fastest route with distance influence controlled by a new parameter.
 * <p>
 *
 * @author Peter Karich
 */
public class ShortFastestWeighting extends FastestWeighting {
    private static final String NAME = "short_fastest";
    private static final String TIME_FACTOR = "short_fastest.time_factor";
    private static final String DISTANCE_FACTOR = "short_fastest.distance_factor";
    private final double distanceFactor;
    private final double timeFactor;
    private List<Area> areas;
    private Graph graph;
    public ShortFastestWeighting(FlagEncoder encoder, PMap map, TurnCostProvider turnCostProvider, List<Area> areas, Graph graph) {
        super(encoder, map);
        timeFactor = checkBounds(TIME_FACTOR, map.getDouble(TIME_FACTOR, 1), 0, 10);

        // default value derived from the cost for time e.g. 25€/hour and for distance 0.5€/km
        distanceFactor = checkBounds(DISTANCE_FACTOR, map.getDouble(DISTANCE_FACTOR, 0.07), 0, 10);
       this.areas = areas;
       this.graph = graph;
        if (timeFactor < 1e-5 && distanceFactor < 1e-5)
            throw new IllegalArgumentException("[" + NAME + "] one of distance_factor or time_factor has to be non-zero");
    }

    static double checkBounds(String key, double val, double from, double to) {
        if (val < from || val > to)
            throw new IllegalArgumentException(key + " has invalid range should be within [" + from + ", " + to + "]");

        return val;
    }
    public ShortFastestWeighting(FlagEncoder encoder, double distanceFactor, List<Area> areas, Graph graph) {
        this(encoder, distanceFactor, NO_TURN_COST_PROVIDER, areas, graph);

    }

    public ShortFastestWeighting(FlagEncoder encoder, double distanceFactor, TurnCostProvider turnCostProvider, List<Area> areas, Graph graph) {
        super(encoder, new PMap());
        this.distanceFactor = checkBounds(DISTANCE_FACTOR, distanceFactor, 0, 10);
        this.timeFactor = 1;
        this.areas = areas;
        this.graph = graph;
    }

    @Override
    public double getMinWeight(double distance) {
        return super.getMinWeight(distance) * timeFactor + distance * distanceFactor;
    }

    @Override
    public double calcWeight(EdgeIteratorState edgeState, boolean reverse, int i) {
        double time = super.calcWeight(edgeState, reverse, i);
        NodeAccess na = graph.getNodeAccess();
        int index = edgeState.getAdjNode();
        double lat = na.getLatitude(index);
        double lng = na.getLongitude(index);
        boolean isOk = false;
        for(Area area: areas) {
            if(area.isInner(lat,lng)) isOk = true;
        }
        if(!isOk) return Double.POSITIVE_INFINITY;
        return time * timeFactor + edgeState.getDistance() * distanceFactor;
    }

    @Override
    public String getName() {
        return NAME;
    }
}