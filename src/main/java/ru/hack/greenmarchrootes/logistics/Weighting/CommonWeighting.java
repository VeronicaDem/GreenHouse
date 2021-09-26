package ru.hack.greenmarchrootes.logistics.Weighting;

import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.util.HintsMap;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.Graph;
import com.graphhopper.util.EdgeIteratorState;

import java.util.Map;

public class CommonWeighting implements Weighting {
    private EcologyWeight ecologyWeight;
    private ShortFastestWeighting shortestWeighting;

    private FlagEncoder encoder;

    public CommonWeighting(EcologyWeight ecologyWeight, ShortFastestWeighting shortestWeighting, FlagEncoder encoder) {
        this.ecologyWeight = ecologyWeight;
        this.shortestWeighting = shortestWeighting;
        this.encoder = encoder;
    }

    @Override
    public double getMinWeight(double v) {
        return 0;
    }

    @Override
    public double calcWeight(EdgeIteratorState edgeIteratorState, boolean b, int i) {
        double weightShortest = shortestWeighting.calcWeight(edgeIteratorState, b, i);
        double weight = ecologyWeight.calcWeight(edgeIteratorState, b, i)  + shortestWeighting.calcWeight(edgeIteratorState, b, i);
        System.out.println("shortest weight: " + weightShortest);
        System.out.println("common weight:" + weight);
        return weight;
    }

    @Override
    public long calcMillis(EdgeIteratorState edgeIteratorState, boolean b, int i) {
        return 0;
    }

    @Override
    public FlagEncoder getFlagEncoder() {
        return encoder;
    }

    @Override
    public String getName() {
        return "common";
    }

    @Override
    public boolean matches(HintsMap hintsMap) {
        return false;
    }
}
