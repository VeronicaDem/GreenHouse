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

    public static final String MAIN_ECO = "AQI";
    public static final Map<String, Standard> measureToStandard = new HashMap<>();

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
           HttpResponse response = Request.Get("https://suggestions.dadata.ru/suggestions/api/4_1/rs/geolocate/address?lat=" + latitude + "&lon=" + longitude)
                    .addHeader("Authorization", AUTHORIZATION)
                    .execute().returnResponse();
            int responseCode = response.getStatusLine().getStatusCode();
            System.out.println("https://suggestions.dadata.ru/suggestions/api/4_1/rs/geolocate/address?lat=" + latitude + "&lon=" + longitude + " \ncode=" + responseCode);
            HttpEntity entity = response.getEntity();


            String nameOfStreetInOSM = (String) new JSONObject(EntityUtils.toString(entity)).getJSONArray("suggestions").getJSONObject(0).get("value");
            station = new Station(nameOfStreetInOSM);



        } catch (IOException e) {
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


          File dir = new File("src/main/resources/Данные по датчикам/Данные по коробкам");
          File[] arrFiles = dir.listFiles();
          List<File> lst = Arrays.asList(arrFiles);
          for(int i = 0; i < lst.size(); i++) {
              readFromExcel(lst.get(i).getAbsolutePath(), streetToMeasures);
          }
          if(streetToMeasures.isEmpty()) System.out.println("streetToMeasures is empty");
          for(String street : streetToMeasures.keySet()) {
              // простите, меня вынудил API
              String bodyString = new JSONArray(List.of(street).toString()).toString();
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

               Station station = new Station(longitude, latitude);


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
