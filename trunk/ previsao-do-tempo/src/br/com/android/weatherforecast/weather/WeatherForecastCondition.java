package br.com.android.weatherforecast.weather;

/**
 * Holds the information between the <forecast_conditions>-tag of what the
 * Google Weather API returned.
 */
public class WeatherForecastCondition
{
	private String dayofWeek = null;
	private Integer tempMin = null;
	private Integer tempMax = null;
	private String iconURL = null;
	private String condition = null;

	public String getDayofWeek()
	{
		String retorno = dayofWeek;

		if (dayofWeek.startsWith("seg"))
			retorno = "Segunda - Feira";
		if (dayofWeek.startsWith("ter"))
			retorno = "Terça - Feira";
		if (dayofWeek.startsWith("qua"))
			retorno = "Quarta - Feira";
		if (dayofWeek.startsWith("qui"))
			retorno = "Quinta - Feira";
		if (dayofWeek.startsWith("sex"))
			retorno = "Sexta - Feira";
		if (dayofWeek.endsWith("b"))
			retorno = "Sábado";
		if (dayofWeek.startsWith("dom"))
			retorno = "Domingo";
		return retorno;
	}

	public void setDayofWeek(String dayofWeek)
	{
		this.dayofWeek = dayofWeek;
	}

	public Integer getTempMinCelsius()
	{
		return tempMin;
	}

	public void setTempMinCelsius(Integer tempMin)
	{
		this.tempMin = tempMin;
	}

	public Integer getTempMaxCelsius()
	{
		return tempMax;
	}

	public void setTempMaxCelsius(Integer tempMax)
	{
		this.tempMax = tempMax;
	}

	public String getIconURL()
	{
		return iconURL;
	}

	public void setIconURL(String iconURL)
	{
		this.iconURL = iconURL;
	}

	public String getCondition()
	{
		return condition;
	}

	public void setCondition(String condition)
	{
		this.condition = condition;
	}
}
