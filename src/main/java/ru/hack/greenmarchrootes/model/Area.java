package ru.hack.greenmarchrootes.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import ru.hack.greenmarchrootes.logistics.Util;

import javax.persistence.*;
import java.util.HashMap;
import java.util.Map;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name="area")
public class Area {
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Long id_area;
    @OneToOne
    @JoinColumn(name = "station.id_station")
    private Station main_station;
    @ElementCollection
    private Map<String, Boolean> isIncludedPoints;

    private static final double EPSILON = 1e-3;
    public Area(Station station) {
        main_station = station;
        isIncludedPoints = new HashMap<>();
    }

    @Override
    public String toString() {
        return "Area{" +
                "main_station=" + main_station +
                ", isIncludedPoints=" + isIncludedPoints +
                '}';
    }
    private void calculateOnePart(double epsilon1, double epsilon2) {
        double latitude = main_station.getLatitude();
        double longitude = main_station.getLongitude();
        boolean isInnerFlag = true;
        while(isInnerFlag) {

            latitude += epsilon1;
            longitude += epsilon2;
            isInnerFlag = isInner(latitude, longitude);
            if(isInnerFlag) {
                isIncludedPoints.put(latitude + ";" + longitude, true);
            }
        }
    }

    public void calculateArea() {
      calculateOnePart(EPSILON, EPSILON);
      calculateOnePart(EPSILON, 0);
      calculateOnePart(0, EPSILON);
      calculateOnePart(-EPSILON, -EPSILON);
      calculateOnePart(-EPSILON, 0);
      calculateOnePart(0, -EPSILON);

    }

    public boolean isInner(double latitude, double longitude) {
        return Util.getNearestStation(longitude, latitude).equals(main_station);
    }



}
