package ru.hack.greenmarchrootes.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.hack.greenmarchrootes.logistics.MoscowMainStation;
import ru.hack.greenmarchrootes.logistics.Weighting.State;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name="station")
public class Station {
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private long id_station;
    private double longitude;
    private double latitude;
    @Enumerated(EnumType.ORDINAL)
    private State state;
    private String nameOfStreet;
    @ElementCollection(targetClass = State.class)
    private Map<String, State> badOrMiddleParameters;
    private String nameOfStreetInOSM;
    public Station(double longitude, double latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
    }
    public Station(double longitude, double latitude, String nameOfStreet) {
        this(longitude, latitude);
        this.nameOfStreet = nameOfStreet;
    }
    public Station(String nameOfStreetInOSM) {
        this.nameOfStreetInOSM = nameOfStreetInOSM;
    }
    public State calculateState(Map<String, Object > measures, MoscowMainStation moscowMainStation) {
        int countBads = 0;
        State res;
        for(String key : moscowMainStation.getAverage().keySet()) {
            if(measures.containsKey(key)) {
                if (convertIntoDouble(measures.get(key)) > convertIntoDouble(moscowMainStation.getAverage().get(key))) {
                    countBads++;
                }
            }
        }
        // TODO
        int countMoscow = moscowMainStation.getAverage().keySet().size() / 2;
        if(countBads > countMoscow) {
            res = State.BAD;
        } else if(countBads >= countMoscow - 2) {
            res = State.MIDDLE;
        } else {
            res = State.GOOD;
        }
        return res;
    }

    private double convertIntoDouble(Object measure) {
        try {
            return (Integer) measure;
        } catch (Exception e) {
            return ((BigDecimal) measure).doubleValue();

        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Station station = (Station) o;
        return station.getNameOfStreetInOSM().contains(this.getNameOfStreetInOSM());
    }

    @Override
    public int hashCode() {
        return Objects.hash(longitude, latitude);
    }
}
