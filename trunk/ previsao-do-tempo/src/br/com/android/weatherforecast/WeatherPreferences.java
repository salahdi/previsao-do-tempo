package br.com.android.weatherforecast;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class WeatherPreferences
{
	private SharedPreferences preferences;
	
	public WeatherPreferences(SharedPreferences sharedPreferences)
	{
		preferences = sharedPreferences;
	}
	
	public void setXml(String xml)
	{
		Editor edit = preferences.edit();
		
		edit.putString("xml", xml);
		edit.commit();
	}
	
	public String readXml()
	{
		return preferences.getString("xml", "");
	}
	
	public void setCity(String city)
	{
		Editor edit = preferences.edit();
		
		edit.putString("city", city);
		edit.commit();
	}
	
	public String getCity()
	{
		return preferences.getString("city", "");
	}
}
