package br.com.android.weatherforecast.weather;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;
import br.com.android.weatherforecast.R;
import br.com.android.weatherforecast.WeatherForecast;

public class WundergroundDecoder extends AsyncTask<String, Void, WeatherSet>{
	
	private WeatherSet weatherSet = new WeatherSet();
	private Context context;
	String[] keys = new String[]{"1e37ada8b850301c", "5cbebed2c0375bea", "26d700ac14bcb117"};
	
	public WundergroundDecoder(Context context) {
		this.context = context;
	}
	
	public WeatherSet getWeatherSet(String cityParam) throws MalformedURLException, IOException, JSONException
	{
		String line;
		String queryString;
		StringBuilder response = new StringBuilder();
		URLConnection connection;
		BufferedReader reader;
		String keyC = keys[(int) (1 + (3*Math.random())) - 1];
		String keyF = keys[(int) (1 + (3*Math.random())) - 1];

		if (cityParam.equals(""))
			return weatherSet;
		queryString = "http://api.wunderground.com/api/" + keyF + "/forecast/lang:BR/q/Brazil/" + cityParam + ".json";
		connection = new URL(queryString.replace(" ", "%20")).openConnection();
		connection.setConnectTimeout(1000 * 5);
		reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), Charset.forName("UTF-8")));
		while ((line = reader.readLine()) != null)
			response.append(line);
		decodeJSONForecast(response.toString());
		reader.close();
		connection = null;
		reader = null;
		response = new StringBuilder();
		queryString = "http://api.wunderground.com/api/" + keyC + "/conditions/lang:BR/q/Brazil/" + cityParam + ".json";
		connection = new URL(queryString.replace(" ", "%20")).openConnection();
		connection.setConnectTimeout(1000 * 5);
		reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), Charset.forName("UTF-8")));
		while ((line = reader.readLine()) != null)
			response.append(line);
		decodeJSONConditions(response.toString());
		weatherSet.setWeatherForecastInformation(new WeatherForecastInformation());
		weatherSet.getWeatherForecastInformation().setCity(cityParam);
		weatherSet.getWeatherForecastInformation().setTime(System.currentTimeMillis());
		return weatherSet;
	}
	
	private void decodeJSONConditions(String response) throws JSONException{
		JSONObject json = new JSONObject(response.toString());
		WeatherCurrentCondition current = new WeatherCurrentCondition();
		JSONObject condition = (JSONObject) json.get("current_observation");
		
		current.setTempCelcius(condition.getInt("temp_c"));
		current.setCondition(condition.getString("weather"));
		current.setWindCondition(condition.getString("wind_dir") + " a " + condition.getString("wind_kph") + " km/h");
		current.setIconURL(condition.getString("icon"));
		current.setHumidity(condition.getString("relative_humidity"));
		weatherSet.setWeatherCurrentCondition(current);
	}
	
	private void decodeJSONForecast(String response) throws JSONException{
		JSONObject json = new JSONObject(response.toString());
		JSONObject forecast = (JSONObject) json.get("forecast");
		JSONObject simpleforecast = (JSONObject) forecast.get("simpleforecast");
		JSONArray forecastDay = (JSONArray) simpleforecast.get("forecastday");
		JSONObject day;
		
		for(int i = 0; i < forecastDay.length(); i++){
			
			WeatherForecastCondition dayForecast = new WeatherForecastCondition();
			day = (JSONObject) forecastDay.get(i);
			
			dayForecast.setDayofWeek(day.getJSONObject("date").getString("weekday"));
			dayForecast.setCondition(day.getString("conditions"));
			dayForecast.setIconURL(day.getString("icon"));
			dayForecast.setTempMax(day.getJSONObject("high").getInt("celsius"));
			dayForecast.setTempMin(day.getJSONObject("low").getInt("celsius"));
			dayForecast.setPrecipitation(day.getString("pop") + "%");
			weatherSet.getWeatherForecastConditions().add(dayForecast);
		}
	}

	@Override
	protected WeatherSet doInBackground(String... params) {
		WeatherSet ws = null;
		
		try {
			ws = getWeatherSet(params[0]);
		} catch (MalformedURLException e) {
			Log.e(WeatherForecast.DEBUG_TAG, e.getMessage(), e);
		} catch (IOException e) {
			Toast.makeText(context, context.getString((R.string.internetErrorMsg)), Toast.LENGTH_LONG).show();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return ws;
	}
}
