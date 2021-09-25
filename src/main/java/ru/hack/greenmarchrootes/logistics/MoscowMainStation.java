package ru.hack.greenmarchrootes.logistics;


import lombok.Getter;
import lombok.Setter;
import ru.hack.greenmarchrootes.model.Station;

import java.util.HashMap;
import java.util.Map;
@Getter
@Setter
public class MoscowMainStation extends Station {
    private Map<String, Object> average;
    public MoscowMainStation(double longitude, double latitude) {
        super(longitude, latitude);
        this.average = new HashMap<>();
        this.average = calculateAverage();
    }

    private Map<String, Object> calculateAverage() {
        return Util.getMeasurements(getLatitude(), getLongitude());
    }


}
