package ru.hack.greenmarchrootes.logistics;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.apache.http.util.EntityUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.NPOIFSFileSystem;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONArray;
import org.json.JSONObject;
import ru.hack.greenmarchrootes.logistics.Weighting.State;
import ru.hack.greenmarchrootes.logistics.measuresStandard.Standard;
import ru.hack.greenmarchrootes.model.Area;
import ru.hack.greenmarchrootes.model.Station;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
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
public class Util {
    public static final String TOKEN = "15fe6177b55545a422167f019b00eb11be25df56";
    public static final String AUTHORIZATION = "Token 94f6ab1dca96e4e1638a58532459aa5a5ed3feb9";
    public static final String X_Secret = "2a5663965f511252afbed1dcdb46d177f416c0df";

    public static final List<String> measuresNames = Arrays.asList(
            "Температура", // UPPER = 30, MIDDLE = 20
            "Влажность",
            "СО2",
            "ЛОС",
            "Пыль pm 1.0",
            "Пыль pm 2.5"	,
            "Пыль pm 10"	, // MIDDLE = 16, UPPER = 20
            "Давление"	, // UPPER = 737, MIDDLE = 703
            "AQI"	, // UPPER = 101, MIDDLE = 51
            "Формальдегид"
            );
    public static final Map<String, Double> streetToLng = new HashMap<>();
    public static final Map<String, Double> streetToLat = new HashMap<>();
    public static final String MAIN_ECO = "AQI";
    public static final Map<String, Standard> measureToStandard = new HashMap<>();

    public static void initializeStreets() {
        streetToLng.put("1905 года улица", 37.5594649);
        streetToLat.put("1905 года улица", 55.7616887);

        streetToLng.put("Баррикадная улица", 37.5818858);
        streetToLat.put("Баррикадная улица", 55.7596403);

        streetToLng.put("Бережковская набережная", 37.564446);
        streetToLat.put("Бережковская набережная", 55.738074);

        streetToLng.put("Варшавское шоссе", 37.6246433);
        streetToLat.put("Варшавское шоссе", 55.6211591);

        streetToLng.put("Волгоградский проспект", 37.749274);
        streetToLat.put("Волгоградский проспект", 55.707644);

        streetToLng.put("Воробьёвская набережная", 37.539722);
        streetToLat.put("Воробьёвская набережная", 55.717778);

        streetToLng.put("Госпитальная набережная", 37.695043);
        streetToLat.put("Госпитальная набережная", 55.771991);

        streetToLng.put("Дмитровское шоссе", 37.520);
        streetToLat.put("Дмитровское шоссе", 56.344);

        streetToLng.put("Звенигородское шоссе", 37.524201);
        streetToLat.put("Звенигородское шоссе", 55.767965);

        streetToLng.put("Каширское шоссе", 37.7199742);
        streetToLat.put("Каширское шоссе", 55.6054076);

        streetToLng.put("Кетчерская улица", 37.829027);
        streetToLat.put("Кетчерская улица", 55.744612);

        streetToLng.put("Королева улица", 37.8256);
        streetToLat.put("Королева улица", 55.9142);

        streetToLng.put("Космодамианская набережная", 37.637);
        streetToLat.put("Космодамианская набережная", 55.746);

        streetToLng.put("Кремлевская набережная", 37.616953);
        streetToLat.put("Кремлевская набережная", 55.748691);

        streetToLng.put("Крымская набережная", 37.605122);
        streetToLat.put("Крымская набережная", 55.689999);

        streetToLng.put("Кутузовский проспект", 37.523);
        streetToLat.put("Кутузовский проспект", 55.737);

        streetToLng.put("Ленинградское шоссе", 37.497696);
        streetToLat.put("Ленинградское шоссе", 55.818102);

        streetToLng.put("Лужнецкая набережная", 37.559173);
        streetToLat.put("Лужнецкая набережная", 55.711316);

        streetToLng.put("Люблинская улица", 37.738135);
        streetToLat.put("Люблинская улица", 55.680671);

        streetToLng.put("Минское шоссе", 37.3848394);
        streetToLat.put("Минское шоссе", 55.7134921);

        streetToLng.put("Мосфильмовская улица", 37.5272);
        streetToLat.put("Мосфильмовская улица", 55.7222);

        streetToLng.put("Моховая улица", 37.611634);
        streetToLat.put("Моховая улица", 55.753539);

        streetToLng.put("Набережная Тараса Шевченко", 37.554035);
        streetToLat.put("Набережная Тараса Шевченко", 55.750408);

        streetToLng.put("Нагатинская набережная", 37.661392);
        streetToLat.put("Нагатинская набережная", 55.685041);

        streetToLng.put("Никитская Б. улица", 37.6041);
        streetToLat.put("Никитская Б. улица", 55.7568);

        streetToLng.put("Новодевичья набережная", 37.553194);
        streetToLat.put("Новодевичья набережная", 55.728667);

        streetToLng.put("Новый Арбат", 37.589096);
        streetToLat.put("Новый Арбат", 55.753129);

        streetToLng.put("Петровский бульвар", 37.617384);
        streetToLat.put("Петровский бульвар", 55.767595);

        streetToLng.put("Проспект Вернадского", 37.536);
        streetToLat.put("Проспект Вернадского", 55.693);

        streetToLng.put("Профсоюзная улица", 37.532565);
        streetToLat.put("Профсоюзная улица", 55.649576);

        streetToLng.put("Пятницкая улица", 37.6281967);
        streetToLat.put("Пятницкая улица", 55.7382355);

        streetToLng.put("Рождественский бульвар", 37.623);
        streetToLat.put("Рождественский бульвар", 55.766);

        streetToLng.put("Садовая-каретная улица", 37.60841);
        streetToLat.put("Садовая-каретная улица", 55.772978);

        streetToLng.put("Садовая-самотечная улица",  37.612);
        streetToLat.put("Садовая-самотечная улица", 55.773);

        streetToLng.put("Солженицына улица", 37.661716);
        streetToLat.put("Солженицына улица", 55.743659);

        streetToLng.put("Сретенский бульвар", 37.633886);
        streetToLat.put("Сретенский бульвар", 55.766248);

        streetToLng.put("Страстной бульвар", 37.607008);
        streetToLat.put("Страстной бульвар", 55.766719);

        streetToLng.put("Таганская улица", 37.664303);
        streetToLat.put("Таганская улица", 55.739732);

        streetToLng.put("Тверская улица", 37.606379);
        streetToLat.put("Тверская улица", 55.763923);

        streetToLng.put("Цветной бульвар", 37.622199);
        streetToLat.put("Цветной бульвар", 55.770461);

        streetToLng.put("Щелковское шоссе", 37.80139);
        streetToLat.put("Щелковское шоссе", 55.81028);

        streetToLng.put("Энтузиастов шоссе", 37.767896);
        streetToLat.put("Энтузиастов шоссе", 55.761487);
    }
    public static void initializeStandards() {
        measureToStandard.put("Температура", new Standard(30, 20));
        measureToStandard.put("Пыль pm 10", new Standard(20, 16));
        measureToStandard.put("Давление", new Standard(737, 703));
        measureToStandard.put("AQI", new Standard(101, 51));
    }
    public static Map<String, Object> getMeasurements(double lat, double lng) {
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


    public static Station getNearestStation(double longitude, double latitude) {
        Station station = null;
        try {
           /* HttpResponse response = Request.Get("https://suggestions.dadata.ru/suggestions/api/4_1/rs/geolocate/address?lat=" + latitude + "&lon=" + longitude)
                    .addHeader("Authorization", AUTHORIZATION)
                    .execute().returnResponse();
            int responseCode = response.getStatusLine().getStatusCode();
            System.out.println("https://suggestions.dadata.ru/suggestions/api/4_1/rs/geolocate/address?lat=" + latitude + "&lon=" + longitude + " \ncode=" + responseCode);
            HttpEntity entity = response.getEntity();


            String nameOfStreetInOSM = (String) new JSONObject(EntityUtils.toString(entity)).getJSONArray("suggestions").getJSONObject(0).get("value");
            station = new Station(nameOfStreetInOSM);

            */
            double min_distance = Double.POSITIVE_INFINITY;
            double p = 0.017453292519943295;    // Math.PI / 180
            for(String key : streetToLat.keySet()) {
                var a = 0.5 - Math.cos((longitude - streetToLng.get(key)) * p) / 2 +
                        Math.cos(streetToLng.get(key) * p) * Math.cos(streetToLat.get(key) * p) *
                                (1 - Math.cos((longitude - streetToLat.get(key)) * p)) / 2;

                double distance = 12742 * Math.asin(Math.sqrt(a)); // 2 * R; R = 6371 km
                if(min_distance > distance) {
                    min_distance = distance;
                    station = new Station(key);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return station;
    }
    private static double convertIntoDouble(Object measure) {
        try {
            return (Integer) measure;
        } catch (Exception e) {
            return ((BigDecimal) measure).doubleValue();

        }
    }

    private static void readFromExcel(String file, Map<String, JSONObject> dataOfStreet) throws Exception{
        //NPOIFSFileSystem fs = new NPOIFSFileSystem(new File(file));
        Workbook myExcelBook = WorkbookFactory.create(new FileInputStream(file));
        Sheet sheet = myExcelBook.getSheetAt(0);
        //String numberOfMeasuring = file.substring(file.indexOf("_"), file.indexOf(".xls"));
        String key = file.substring(file.lastIndexOf("\\") + 1, file.indexOf("_"));
        //JSONArray jsonArray = dataOfStreet.get(key)==null ? new JSONArray() : dataOfStreet.get(key);
        JSONObject additionalInfo = dataOfStreet.get(key) == null ? new JSONObject() : dataOfStreet.get(key);
        System.out.println("key : " + key);
        String currentMeasureDate = sheet.getRow(sheet.getLastRowNum())
                .getCell(0).getStringCellValue();
        if(additionalInfo.isEmpty()) {
            Row row = sheet.getRow(0);
            additionalInfo.put("currentMeasureDate", currentMeasureDate);
        }
       // jsonArray.put(new JSONObject().put("numberOfMeasuring", numberOfMeasuring));

        for(int i = 1; i < sheet.getLastRowNum(); i++) {
           Row row = sheet.getRow(i);
            String dateOfMeasuring = row.getCell(0).getStringCellValue();
            int numberOfCells = row.getPhysicalNumberOfCells();
            for(int j = 1; j < numberOfCells; j++) {
                String nameOfColumn = sheet.getRow(0).getCell(j).getStringCellValue();
                if(row.getCell(j) == null) continue;
                double measure = row.getCell(j).getNumericCellValue();
                if(!additionalInfo.has("min" + nameOfColumn)  || (Double)additionalInfo.get("min" + nameOfColumn) > measure) {
                    additionalInfo.put("min" + nameOfColumn, measure);
                }
                if(!additionalInfo.has("max" + nameOfColumn)  || (Double) additionalInfo.get("max" + nameOfColumn) < measure) {
                    additionalInfo.put("max" + nameOfColumn, measure);
                }



            }


        }
        Row row = sheet.getRow(sheet.getLastRowNum());
        int numberOfCells = row.getPhysicalNumberOfCells();
        for(int j = 1; j < numberOfCells; j++) {
            if(row.getCell(j) == null) continue;
            String nameOfColumn = sheet.getRow(0).getCell(j).getStringCellValue();
            double measure = row.getCell(j).getNumericCellValue();
            if(!additionalInfo.has("min" + nameOfColumn + currentMeasureDate)  || (Double)additionalInfo.get("min" + nameOfColumn + currentMeasureDate) > measure) {
                additionalInfo.put("min" + nameOfColumn + currentMeasureDate, measure);
            }
            if(!additionalInfo.has("max" + nameOfColumn + currentMeasureDate)  || (Double) additionalInfo.get("max" + nameOfColumn + currentMeasureDate) < measure) {
                additionalInfo.put("max" + nameOfColumn + currentMeasureDate, measure);
            }
        }
        dataOfStreet.put(key, additionalInfo);
        myExcelBook.close();

    }
    public static List<Station> getAllStations() {
      List<Station> res = new ArrayList<>();
      Map<String, JSONObject> streetToMeasures = new HashMap<>();
      try {
         /* HttpResponse response = Request.Get("https://api.waqi.info/search/?token=" + TOKEN + "&keyword=moscow").execute().returnResponse();
          System.out.println("https://api.waqi.info/search/?token=" + TOKEN + "&keyword=moscow");
          int responseCode = response.getStatusLine().getStatusCode();
          HttpEntity entity = response.getEntity();
          JSONObject reply = new JSONObject(EntityUtils.toString(entity));
          JSONArray array = reply.getJSONArray("data");
          System.out.println(array);

          */
          File dir = new File("src/main/resources/Данные по датчикам/Данные по коробкам");
          File[] arrFiles = dir.listFiles();
          List<File> lst = Arrays.asList(arrFiles);
          for(int i = 0; i < lst.size(); i++) {
              readFromExcel(lst.get(i).getAbsolutePath(), streetToMeasures);
          }
          if(streetToMeasures.isEmpty()) System.out.println("streetToMeasures is empty");
          for(String street : streetToMeasures.keySet()) {
              // простите, меня вынудил API
              /*String bodyString = new JSONArray(List.of(street).toString()).toString();
              HttpResponse response = Request.Post("https://cleaner.dadata.ru/api/v1/clean/address")
                                             .addHeader("Authorization", AUTHORIZATION)
                                             .addHeader("X-Secret", X_Secret)
                                             .bodyString(bodyString, ContentType.APPLICATION_JSON)
                                             .execute().returnResponse();
              HttpEntity entity = response.getEntity();
              System.out.println(EntityUtils.toString(entity));
              JSONObject reply = new JSONArray(EntityUtils.toString(entity)).getJSONObject(0);


              double latitude = Double.parseDouble(reply.get("geo_lat").toString());
              double longitude = Double.parseDouble(reply.get("geo_lon").toString());
              String nameOfStreetInOSM = (String) reply.get("result");



              double latitude = Double.parseDouble(reply.get("geo_lat").toString());
              double longitude = Double.parseDouble(reply.get("geo_lon").toString());
              String nameOfStreetInOSM = (String) reply.get("result");
              Station station = new Station(longitude, latitude, street);

               */
              String nameOfStreetInOSM = street;
              System.out.println(street);
              double latitude = streetToLat.get(street);
              double longitude = streetToLng.get(street);
              Station station = new Station(longitude, latitude, street);
              List<State> states = new ArrayList<>();
              Map<String, State> badOrMiddleParameters = new HashMap<>();
             String currentDateOfMeasuring = streetToMeasures.get(street).get("currentMeasureDate").toString();
              State stateOfMainEco = null;
              for(String measureName : measuresNames) {
                  double min = Double.parseDouble(streetToMeasures.get(street).get("min" + measureName).toString());
                  double max = Double.parseDouble(streetToMeasures.get(street).get("max" + measureName).toString());
                  double currentMeasure = (Double) streetToMeasures.get(street).get("max" + measureName + currentDateOfMeasuring);
                  if(measureToStandard.get(measureName) != null) {
                      State stateOfOneParameter = measureToStandard.get(measureName).calculateStateByOneMeasure(currentMeasure);
                      if (stateOfOneParameter == State.MIDDLE || stateOfOneParameter == State.BAD) {
                          badOrMiddleParameters.put(measureName, stateOfOneParameter);
                      }
                      states.add(stateOfOneParameter);
                  }
                  if(measureName.equals(MAIN_ECO)) stateOfMainEco = measureToStandard.get(MAIN_ECO)
                                                                      .calculateStateByOneMeasure(currentMeasure);
              }

              station.setState(stateOfMainEco);
              station.setBadOrMiddleParameters(badOrMiddleParameters);
              station.setNameOfStreetInOSM(nameOfStreetInOSM);
              res.add(station);
          }
      }catch (Exception e) {
          e.printStackTrace();
      }
      return res;
    }

    public static List<Area> getAreas(List<Station> stations) {
        List<Area> res = new ArrayList<>();

        for(Station station: stations) {
            Area area = new Area(station);
            area.calculateArea();
            res.add(area);
        }
        return res;
    }
}
