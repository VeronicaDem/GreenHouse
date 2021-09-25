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

    /**
     * Получает точки в количестве N штук
     */
    public void calculateArea() {
      calculateOnePart(1e-3, 1e-3);
      calculateOnePart(1e-3, 0);
      calculateOnePart(0, 1e-3);
      calculateOnePart(-1e-3, -1e-3);
      calculateOnePart(-1e-3, 0);
      calculateOnePart(0, -1e-3);

    }

    public boolean isInner(double latitude, double longitude) {
        Station nearestStation = Util.getNearestStation(longitude, latitude);
        System.out.println("longitude:" + nearestStation.getLongitude() +",latitude: " + nearestStation.getLatitude());
        System.out.println("main_station latitude == latitude: " + (nearestStation.getLatitude() == main_station.getLatitude()));
        System.out.println("main_station longitude = longitude:" + (nearestStation.getLongitude() == main_station.getLongitude()));
        return Util.getNearestStation(longitude, latitude).equals(main_station);
    }



}
