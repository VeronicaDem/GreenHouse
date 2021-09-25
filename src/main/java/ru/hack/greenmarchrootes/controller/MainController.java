package ru.hack.greenmarchrootes.controller;

import com.graphhopper.util.PointList;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.hack.greenmarchrootes.model.Area;
import ru.hack.greenmarchrootes.repository.StationRepository;
import ru.hack.greenmarchrootes.service.MainService;

import java.util.List;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping(value = "api/")
public class MainController {
    private final MainService service;
   @GetMapping(value="get/areas/{update}")
    public ResponseEntity<List<Area>> getAreas(@PathVariable(name="update") boolean update ) {
       service.updateAreas(update);
       return MainService.areas != null && !MainService.areas.isEmpty() ?
               new ResponseEntity<>(MainService.areas, HttpStatus.OK) :
               new ResponseEntity<>(HttpStatus.NOT_FOUND);
   }
   @GetMapping(value = "get/route/vehicle={vehicle}/start={startLng};{startLat}/end={endLng};{endLat}")
   public ResponseEntity<PointList> getRoute(@PathVariable(name="startLng") double startLng,
                                             @PathVariable(name="startLat") double startLat,
                                             @PathVariable(name="endLng") double endLng,
                                             @PathVariable(name="endLat") double endLat,
                                             @PathVariable(name="vehicle") String vehicle) {
       PointList pointList = service.getRoute(startLng, startLat, endLng, endLat,vehicle);
       return pointList != null?
               new ResponseEntity<>(pointList, HttpStatus.OK) :
               new ResponseEntity<>(HttpStatus.NOT_FOUND);
   }


}
