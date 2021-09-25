package ru.hack.greenmarchrootes.logistics;

import com.graphhopper.reader.ReaderRelation;
import com.graphhopper.reader.ReaderWay;
import com.graphhopper.routing.util.AbstractFlagEncoder;
import com.graphhopper.util.PMap;

public class CustomFlagEncoder extends AbstractFlagEncoder {
    public CustomFlagEncoder(PMap properties) {
        super(properties);
    }

    public CustomFlagEncoder(String propertiesStr) {
        super(propertiesStr);
    }

    protected CustomFlagEncoder(int speedBits, double speedFactor, int maxTurnCosts) {
        super(speedBits, speedFactor, maxTurnCosts);
    }

    @Override
    public long handleRelationTags(ReaderRelation relation, long oldRelationFlags) {
        return 0;
    }

    @Override
    public long acceptWay(ReaderWay way) {

        return 0;
    }

    @Override
    public long handleWayTags(ReaderWay way, long allowed, long relationFlags) {
        return 0;
    }

    @Override
    public int getVersion() {
        return 0;
    }
}
