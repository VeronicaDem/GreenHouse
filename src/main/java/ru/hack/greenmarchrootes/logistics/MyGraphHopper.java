package ru.hack.greenmarchrootes.logistics;

import com.graphhopper.GraphHopper;
import com.graphhopper.reader.ReaderWay;
import com.graphhopper.reader.osm.GraphHopperOSM;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.util.HintsMap;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.Graph;
import ru.hack.greenmarchrootes.logistics.Weighting.CommonWeighting;
import ru.hack.greenmarchrootes.logistics.Weighting.EcologyWeight;
import ru.hack.greenmarchrootes.logistics.Weighting.ShortFastestWeighting;
import ru.hack.greenmarchrootes.service.MainService;


import java.util.LinkedList;


public class MyGraphHopper extends GraphHopper {
    // TODO: Как узнать longitude и latitude. i - это что?
    @Override
    public Weighting createWeighting(HintsMap hintsMap, FlagEncoder encoder, Graph graph) {
        double lat = graph.getNodeAccess().getLatitude(0);
        double lng = graph.getNodeAccess().getLongitude(0);
        EcologyWeight ecologyWeight = new EcologyWeight(encoder, graph);
       ShortFastestWeighting shortestWeighting = new ShortFastestWeighting(encoder, 10.0, MainService.areas, graph);
        CommonWeighting commonWeighting = new CommonWeighting(ecologyWeight, shortestWeighting, encoder);
        System.out.println(commonWeighting);
        return commonWeighting;
    }

}
