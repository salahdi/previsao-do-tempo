package br.com.android.weatherforecast.weather;

public class WeatherForecastInformation
{
	private String city;
	private long time;

	public void setCity(String city)
	{
		this.city = city;
	}

	public String getCity()
	{
		return city;
	}
	
	public void setTime(long time)
	{
		this.time = time;
	}
	
	public long getTime()
	{
		return time;
	}

}
