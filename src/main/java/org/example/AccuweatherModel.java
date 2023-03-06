package org.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.List;

public class AccuweatherModel implements WeatherModel {
    //http://dataservice.accuweather.com/forecasts/v1/daily/1day/349727
    private static final String PROTOKOL = "https";
    private static final String BASE_HOST = "dataservice.accuweather.com";
    private static final String FORECASTS = "forecasts";
    private static final String VERSION = "v1";
    private static final String DAILY = "daily";
    private static final String ONE_DAY = "1day";
    private static final String FIVE_DAY = "5day";
    private static final String API_KEY = "1BG5m3fiGuQGIXhD5ZpEgEnyjWdDID95";
    private static final String API_KEY_QUERY_PARAM = "apikey";

    private static final String METRIC_PARAM="metric";
    private static final String METRIC="true";

    private static final String LOCATIONS = "locations";
    private static final String CITIES = "cities";
    private static final String AUTOCOMPLETE = "autocomplete";

    private static final OkHttpClient okHttpClient = new OkHttpClient();
    private static  final ObjectMapper objectMapper = new ObjectMapper();

    //private DataBaseRepository dataBaseRepository = new DataBaseRepository();
    private int converFtoC (Double FTemp){
        return (int) Math.round((FTemp-32)/1.8);
    }
    private String parseJSON (JsonNode node){
        String date = node.findValues("Date").get(0).asText().substring(0, 10);
        int minTemperature = converFtoC(Double.parseDouble(node.findValues("Temperature").get(0)
                .findValues("Minimum").get(0).findValues("Value").get(0).asText()));
        int maxTemperature = converFtoC(Double.parseDouble(node.findValues("Temperature").get(0)
                .findValues("Maximum").get(0).findValues("Value").get(0).asText()));
        String day = node.findValues("Day").get(0)
                .findValues("IconPhrase").get(0).asText();
        String night = node.findValues("Night").get(0)
                .findValues("IconPhrase").get(0).asText();

        return "Дата:" + date + "\n"
                + "Минимальная температура:" + minTemperature + "\n"
                + "Максимальная температура:" + maxTemperature + "\n"
                + "Днем:" + day + "\n"
                + "Ночью:" + night + "\n";
    }
    private String getWeatherResponse (String selectedCity,String day_period) throws IOException{
        HttpUrl httpUrl = new HttpUrl.Builder()
                .scheme(PROTOKOL)
                .host(BASE_HOST)
                .addPathSegment(FORECASTS)
                .addPathSegment(VERSION)
                .addPathSegment(DAILY)
                .addPathSegment(day_period)
                .addPathSegment(detectCityKey(selectedCity))
                .addQueryParameter(API_KEY_QUERY_PARAM, API_KEY)
                .addQueryParameter(METRIC_PARAM, METRIC)
                .build();

        Request request = new Request.Builder()
                .url(httpUrl)
                .build();

        Response oneDayForecastResponse = okHttpClient.newCall(request).execute();
        String weatherResponse = oneDayForecastResponse.body().string();

        JsonNode nodes = objectMapper.readTree(weatherResponse);
        List<JsonNode> fieldNames = nodes.findValues("DailyForecasts");
        String result="";
        if (fieldNames.get(0).isArray()){

            for (JsonNode node:fieldNames.get(0)
             ) {
                result+=parseJSON(node);

            }
        } else {
            result+=parseJSON(fieldNames.get(0));
        }
        return result;
    }
    public void getWeather(String selectedCity, Period period) throws IOException {
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

//    @Override
//    public List<Weather> getSavedToDBWeather() {
//        return dataBaseRepository.getSavedToDBWeather();
//    }

    private String detectCityKey(String selectCity) throws IOException {
        //http://dataservice.accuweather.com/locations/v1/cities/autocomplete
        HttpUrl httpUrl = new HttpUrl.Builder()
                .scheme(PROTOKOL)
                .host(BASE_HOST)
                .addPathSegment(LOCATIONS)
                .addPathSegment(VERSION)
                .addPathSegment(CITIES)
                .addPathSegment(AUTOCOMPLETE)
                .addQueryParameter(API_KEY_QUERY_PARAM, API_KEY)
                .addQueryParameter("q", selectCity)
                .build();

        Request request = new Request.Builder()
                .url(httpUrl)
                .get()
                .addHeader("accept", "application/json")
                .build();

        Response response = okHttpClient.newCall(request).execute();
        String responseString = response.body().string();

        String cityKey = objectMapper.readTree(responseString).get(0).at("/Key").asText();


        return cityKey;
    }
}