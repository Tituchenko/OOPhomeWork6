package org.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.List;

public class YandexModel implements WeatherModel{
    private class Coordinate{
        public Coordinate(String coordString) {
            String[] arrayCoord=coordString.split(" ");

            this.lon = Double.parseDouble(arrayCoord[0]);
            this.lat =Double.parseDouble(arrayCoord[1]);
        }

        public String getLat() {
            return Double.toString(lat);
        }

        public String getLon() {
            return Double.toString(lon);
        }

        double lat;
        double lon;
    }

//    public static void main(String[] args) throws IOException{
////        getCityCoord("Москва");
//        String moscow="37.617698 55.755864";
//        Coordinate cityCoordinate = new Coordinate(moscow);
////        System.out.println(cityCoordinate);
//        getWeather(cityCoordinate,Period.NOW);
//
//    }
//    GET https://api.weather.yandex.ru/v2/forecast?
// lat=<широта>
// & lon=<долгота>
// & [lang=<язык ответа>]
// & [limit=<срок прогноза>]
// & [hours=<наличие почасового прогноза>]
// & [extra=<подробный прогноз осадков>]
//
//X-Yandex-API-Key: <значение ключа>
    private static final String PROTOKOL = "https";
    private static final String BASE_HOST = "api.weather.yandex.ru";
    private static final String VERSION = "v2";
    private static final String FORECASTS = "forecast";
    private static final String LAT_PARAM = "lat";
    private static final String LON_PARAM = "lon";

    private static final String LIMIT_PARAM = "limit";
    private static final String KEY_PARAM = "X-Yandex-API-Key";
    private static final String KEY = "9f3ee80b-2026-48cc-9198-d581d1d44981";
    private static final String ONE_DAY = "1";
    private static final String FIVE_DAY = "5";

    private static final OkHttpClient okHttpClient = new OkHttpClient();
    private static  final ObjectMapper objectMapper = new ObjectMapper();

    private String getWeatherResponse (Coordinate selectedCity,String day_period) throws IOException {
        HttpUrl httpUrl = new HttpUrl.Builder()
                .scheme(PROTOKOL)
                .host(BASE_HOST)
                .addPathSegment(VERSION)
                .addPathSegment(FORECASTS)
                .addQueryParameter(LAT_PARAM, selectedCity.getLat())
                .addQueryParameter(LON_PARAM, selectedCity.getLon())
                .addQueryParameter(LIMIT_PARAM, day_period)
                .build();

        Request request = new Request.Builder()
                .url(httpUrl)
                .header(KEY_PARAM, KEY)
                .build();

        Response oneDayForecastResponse = okHttpClient.newCall(request).execute();
        String weatherResponse = oneDayForecastResponse.body().string();
//        System.out.println(weatherResponse);

        JsonNode nodes = objectMapper.readTree(weatherResponse);
        List<JsonNode> fieldNames = nodes.findValues("forecasts");
        String result = "";

        for (JsonNode node : fieldNames.get(0)
        ) {
           result += parseJSON(node);

        }


        return result;
    }

    private String parseJSON (JsonNode node){
        String date = node.findValues("date").get(0).asText();
        int minTemperature = Integer.parseInt(node.findValues("day").get(0)
                .findValues("temp_min").get(0).asText());
        int maxTemperature = Integer.parseInt(node.findValues("day").get(0)
                .findValues("temp_max").get(0).asText());


        String day = node.findValues("day").get(0)
                .findValues("condition").get(0).asText();
        String night = node.findValues("night").get(0)
                .findValues("condition").get(0).asText();

        return "Дата:" + date + "\n"
                + "Минимальная температура:" + minTemperature + "\n"
                + "Максимальная температура:" + maxTemperature + "\n"
                + "Днем:" + day + "\n"
                + "Ночью:" + night + "\n";
    }

    public void getWeather(String city, Period period) throws IOException {
        String coordCity=getCityCoord(city);
        Coordinate selectedCity = new Coordinate(coordCity);
////

        switch (period) {
            case NOW:

                System.out.println(getWeatherResponse(selectedCity,ONE_DAY));
                //TODO: сделать человекочитаемый вывод погоды. Выбрать параметры для вывода на свое усмотрение
                //Например: Погода в городе Москва - 5 градусов по цельсию Expect showers late Monday night
                //dataBaseRepository.saveWeatherToDataBase(new Weather()) - тут после парсинга добавляем данные в БД
                break;
            case FIVE_DAYS:
                System.out.println(getWeatherResponse(selectedCity,FIVE_DAY));

                //TODO*: реализовать вывод погоды на 5 дней
                break;
        }
    }

    //https://geocode-maps.yandex.ru/1.x
    // ? geocode=<string>
    // & apikey=<string>
//     & [format=<string>]

    private static final String BASE_HOST_GEOCODE = "geocode-maps.yandex.ru";
    private static final String VERSION_GEOCODE = "1.x";
    private static final String API_KEY_GEOCODE = "6744ac14-30d1-427a-932a-d9cafeddda7d";
    private static final String API_KEY_GEOCODE_PARAM = "apikey";
    private static final String API_FORMAT = "json";
    private static final String API_FORMAT_PARAM = "format";

    private static final String GEOCODE_PARAM = "geocode";



    private String getCityCoord(String selectCity) throws IOException {
        HttpUrl httpUrl = new HttpUrl.Builder()
                .scheme(PROTOKOL)
                .host(BASE_HOST_GEOCODE)
                .addPathSegment(VERSION_GEOCODE)
                .addQueryParameter(API_KEY_GEOCODE_PARAM, API_KEY_GEOCODE)
                .addQueryParameter(GEOCODE_PARAM, selectCity)
                .addQueryParameter(API_FORMAT_PARAM, API_FORMAT)
                .build();

        Request request = new Request.Builder()
                .url(httpUrl)
                .get()
                .addHeader("accept", "application/json")
                .build();

        Response response = okHttpClient.newCall(request).execute();
        String responseString = response.body().string();
        //System.out.println(responseString);
        String cityKey = objectMapper.readTree(responseString).findValues("pos").get(0).asText();
        return cityKey;
    }
}
