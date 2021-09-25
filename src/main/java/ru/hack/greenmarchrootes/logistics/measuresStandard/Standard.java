package ru.hack.greenmarchrootes.logistics.measuresStandard;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import ru.hack.greenmarchrootes.logistics.Weighting.State;

@Getter
@Setter
@AllArgsConstructor
public class Standard {
    double UPPER;
    double MIDDLE;


    public State calculateStateByOneMeasure(double measure) {
        if(measure >= UPPER) return State.BAD;
        else if(measure >= MIDDLE) return State.MIDDLE;
        else return State.GOOD;
    }
}
