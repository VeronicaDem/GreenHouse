package ru.hack.greenmarchrootes.logistics.Weighting;

import com.carrotsearch.hppc.IntSet;
import com.graphhopper.coll.GHIntHashSet;
import com.graphhopper.routing.weighting.AbstractAdjustedWeighting;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.util.EdgeIteratorState;

/**
 * Increases the weight for a certain set of edges by a given factor and thus makes them less likely to be part of
 * a shortest path
 *
 * @author Robin Boldt
 */
public class AvoidEdgesWeighting extends AbstractAdjustedWeighting {
    // contains the edge IDs of the already visited edges
    protected IntSet avoidedEdges = new GHIntHashSet();
    private double edgePenaltyFactor = 5.0;

    public AvoidEdgesWeighting(Weighting superWeighting) {
        super(superWeighting);
    }

    public AvoidEdgesWeighting setEdgePenaltyFactor(double edgePenaltyFactor) {
        this.edgePenaltyFactor = edgePenaltyFactor;
        return this;
    }

    public AvoidEdgesWeighting setAvoidedEdges(IntSet avoidedEdges) {
        this.avoidedEdges = avoidedEdges;
        return this;
    }
    // TODO: Что это?
    @Override
    public double getMinWeight(double v) {
        return 0;
    }

    @Override
    public double calcWeight(EdgeIteratorState edgeIteratorState, boolean b, int i) {
        double weight = superWeighting.calcWeight(edgeIteratorState, b, i);
        if (avoidedEdges.contains(edgeIteratorState.getEdge()))
            return weight * edgePenaltyFactor;

        return weight;
    }

    @Override
    public String getName() {
        return "avoid_edges";
    }
}
