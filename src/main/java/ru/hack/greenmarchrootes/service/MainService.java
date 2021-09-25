package ru.hack.greenmarchrootes.service;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.weighting.TurnWeighting;
import com.graphhopper.util.PointList;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.hack.greenmarchrootes.logistics.MoscowMainStation;
import ru.hack.greenmarchrootes.logistics.MyGraphHopper;
import ru.hack.greenmarchrootes.logistics.Util;
import ru.hack.greenmarchrootes.model.Area;
import ru.hack.greenmarchrootes.model.Station;
import ru.hack.greenmarchrootes.repository.AreaRepository;
import ru.hack.greenmarchrootes.repository.StationRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Slf4j
@AllArgsConstructor
@Service
public class MainService {
    private final AreaRepository areaRepository;
    private final StationRepository stationRepository;
    //public static final MoscowMainStation moscowMainStation = new MoscowMainStation(	37.618423, 55.751244);
    public static List<Station> stations = new ArrayList<>();
    public static List<Area> areas = new ArrayList<>();

    public void updateAreas(boolean update) {
        stations = getAllStations();
        areas = getAllAreas();
        if(stations.isEmpty() || areas.isEmpty() || update) {
            stations = Util.getAllStations();
            areas = Util.getAreas(stations);
            saveStations(stations);
            saveAreas(areas);
        }
    }

    public List<Station> getAllStations() {
        return stationRepository.findAll();
    }
    public List<Area> getAllAreas() {
        return areaRepository.findAll();
    }

    public List<Station> saveStations(List<Station> stations) {
        return stationRepository.saveAll(stations);
    }
    public List<Area> saveAreas(List<Area> areas) {
        return areaRepository.saveAll(areas);
    }

    public PointList getRoute(double fromLongitude, double fromLatitude, double toLongitude, double toLatitude, String vehicle) {
        EncodingManager encodingManager = new EncodingManager(vehicle);
        GraphHopper graphHopper = new MyGraphHopper()
                .setDataReaderFile("src/main/resources/RU-MOW.pbf")
                .forServer()
                .setStoreOnFlush(true)
                .setGraphHopperLocation("src/main/result")
                .setEncodingManager(encodingManager)
                .setCHEnabled(false)
                ;
        //encodingManager.supports(String.valueOf(TurnWeighting.class));
        //graphHopper.setGraphHopperStorage(graph);
        //.setCHEnabled(false)

       // System.out.println(graphHopper);
        // System.out.println(encodingManager.supports(TurnWeighting.class.getName()));
        GraphHopper hopper = graphHopper.importOrLoad();
        GHRequest req = new GHRequest(fromLatitude, fromLongitude, toLatitude, toLongitude).
                setWeighting("common").
                setVehicle(vehicle).
                setLocale(Locale.US);
        GHResponse rsp = hopper.route(req);
        System.out.println("points");
        PointList pointList = null;
        try {
            pointList = rsp.getBest().getPoints();
        }catch(Exception e) {
            e.printStackTrace();
        }
        return pointList;
    }
}
