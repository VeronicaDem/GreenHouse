package ru.hack.greenmarchrootes.logistics.Weighting;

import com.graphhopper.GraphHopper;
import com.graphhopper.routing.util.AllEdgesIterator;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.util.HintsMap;
import com.graphhopper.routing.weighting.AbstractWeighting;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.NodeAccess;
import com.graphhopper.util.EdgeIterator;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.PointAccess;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Веса для графа.
 * Учитывают экологические параметры.
 * Экологические параметры:
-  pm25
-  pm10
 o3: "Ozone",
 no2: "Nitrogen Dioxide",
 so2: "Sulphur Dioxide",
 co: "Carbon Monoxyde",
 t: "Temperature",
 w: "Wind",
 r: "Rain (precipitation)",
 h: "Relative Humidity",
 d: "Dew",
 p: "Atmostpheric Pressure"
 *  Класс получает данные в реальном времени из aqicn
 *  https://aqicn.org/json-api/doc/
 *  Юзается токен создателя этого кода. В будущем - вынесу в конфиги
 */
public class EcologyWeight extends AbstractWeighting {
   private final String TOKEN = "15fe6177b55545a422167f019b00eb11be25df56";
   private double longitude;
   private double latitude;
   private Graph graph;

   static double min = Double.POSITIVE_INFINITY;
   private static Map<String, Boolean> LatLngStations;
   public EcologyWeight(FlagEncoder flagEncoder,Graph graph ) {
      super(flagEncoder);
      NodeAccess na = graph.getNodeAccess();
      int length = graph.getNodes();
       double lat = na.getLatitude(length - 1);
       double lng = na.getLongitude(length - 1);
       this.graph = graph;
      this.longitude = lng;
      this.latitude = lat;
   }
   private  Map<String, Object> getMeasurements(double lat, double lng) {
      HttpResponse response = null;
      Map<String, Object> res = new HashMap<String, Object>();
       System.out.println("lat:" + lat + ",lng=" + lng);
      try {
         response = Request.Get("https://api.waqi.info/feed/geo:" + lat + ";" + lng + "/?token=" + TOKEN).execute().returnResponse();
         int responseCode = response.getStatusLine().getStatusCode();
          System.out.println("url=" + "https://api.waqi.info/feed/geo:" + lat + ";" + lng + "/?token=" + TOKEN + " \ncode=" + responseCode);
         HttpEntity entity = response.getEntity();
         JSONObject reply = new JSONObject(EntityUtils.toString(entity));
         String geo = String.valueOf(reply.getJSONObject("data").getJSONObject("city").get("geo"));
         geo = geo.substring(1, geo.length() - 1);

         JSONObject iaqi = reply.getJSONObject("data").getJSONObject("iaqi");
         res.put("o3", iaqi.has("o3")  ? iaqi.getJSONObject("o3").get("v") : 0);
         res.put("no2", iaqi.has("no2") ? iaqi.getJSONObject("no2").get("v") : 0);
         res.put("so2",iaqi.has("so2") ? iaqi.getJSONObject("so2").get("v") : 0);
         res.put("co",iaqi.has("co") ? iaqi.getJSONObject("co").get("v") : 0);
         res.put("pm10",iaqi.has("pm10")? iaqi.getJSONObject("pm10").get("v") : 0);
         res.put("pm25", iaqi.has("pm25")? iaqi.getJSONObject("pm25").get("v") : 0);
      } catch (IOException e) {
         e.printStackTrace();
      }

      return res;
   }
   private double getNorma(Map<String, Object> response) {
      double sum = 0;
      for (String key : response.keySet()) {
            try {
               sum += (Integer) response.get(key);
            } catch (Exception e) {
               sum += ((BigDecimal) response.get(key)).doubleValue();

            }

         }

      return Math.sqrt(sum);
   }
   private double getWeight(Map<String, Object> measures) {
       double weight = 0;
       double norma = getNorma(measures);
      for(String key: measures.keySet()) {
         try {
            weight += ((Integer) measures.get(key)) / norma;
         }
         catch (Exception e){
            weight += ((BigDecimal) measures.get(key)).doubleValue() / norma;
         }
      }

       return weight;
   }

   @Override
   public double getMinWeight(double v) {
      min = min > v ? v : min;
      return min;
   }

   @Override
   public double calcWeight(EdgeIteratorState edgeIteratorState, boolean b, int i) {
      NodeAccess na = graph.getNodeAccess();
      int index = edgeIteratorState.getAdjNode();


      System.out.println("indexNode: " + index);
      double lat = na.getLatitude(index);
      double lng = na.getLongitude(index);
      Map<String, Object> measures = getMeasurements(lat, lng);
      double weight = getWeight(measures);
      System.out.println("ecology weight:" + weight);
      return weight;
   }

   @Override
   public String getName() {
      return "ecology";
   }
}
