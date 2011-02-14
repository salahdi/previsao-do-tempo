package br.com.android.weatherforecast.weather;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import br.com.android.weatherforecast.util.WeatherUtils;

/**
 * SAXHandler capable of extracting information out of the xml-data returned by
 * the Google Weather API.
 */
public class GoogleWeatherHandler extends DefaultHandler
{
	private WeatherSet myWeatherSet = null;
	private boolean in_forecast_information = false;
	private boolean in_current_conditions = false;
	private boolean in_forecast_conditions = false;
	private boolean usingSITemperature = false;

	public WeatherSet getWeatherSet()
	{
		return this.myWeatherSet;
	}

	@Override
	public void startDocument() throws SAXException
	{
		this.myWeatherSet = new WeatherSet();
	}

	@Override
	public void endDocument() throws SAXException
	{
	}

	@Override
	public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException
	{
		String dataAttribute;
		
		if (localName.equals("forecast_information"))
		{
			myWeatherSet.setWeatherForecastInformation(new WeatherForecastInformation());
			in_forecast_information = true;
		}
		else if (localName.equals("current_conditions"))
		{
			myWeatherSet.setWeatherCurrentCondition(new WeatherCurrentCondition());
			in_current_conditions = true;
		}
		else if (localName.equals("forecast_conditions"))
		{
			myWeatherSet.getWeatherForecastConditions().add(new WeatherForecastCondition());
			in_forecast_conditions = true;
		}
		else
		{
			dataAttribute = atts.getValue("data");
			if (localName.equals("problem_cause"))
				throw new SAXException(dataAttribute);
			else if (localName.equals("city"))
			{
				if (in_forecast_information)
					myWeatherSet.getWeatherForecastInformation().setCity(dataAttribute);
			}
			else if (localName.equals("postal_code"))
			{
			}
			else if (localName.equals("latitude_e6"))
			{
			}
			else if (localName.equals("longitude_e6"))
			{
			}
			else if (localName.equals("forecast_date"))
			{
			}
			else if (localName.equals("current_date_time"))
			{
			}
			else if (localName.equals("unit_system"))
			{
				if (dataAttribute.equals("SI"))
					usingSITemperature = true;
			}
			else if (localName.equals("day_of_week"))
			{
				if (in_current_conditions)
				{
					myWeatherSet.getWeatherCurrentCondition()
							.setDayofWeek(dataAttribute);
				}
				else if (in_forecast_conditions)
				{
					myWeatherSet.getLastWeatherForecastCondition()
							.setDayofWeek(dataAttribute);
				}
			}
			else if (localName.equals("icon"))
			{
				if (in_current_conditions)
				{
					myWeatherSet.getWeatherCurrentCondition().setIconURL(
							dataAttribute);
				}
				else if (in_forecast_conditions)
				{
					myWeatherSet.getLastWeatherForecastCondition()
							.setIconURL(dataAttribute);
				}
			}
			else if (localName.equals("condition"))
			{
				if (in_current_conditions)
				{
					myWeatherSet.getWeatherCurrentCondition()
							.setCondition(dataAttribute);
				}
				else if (in_forecast_conditions)
				{
					myWeatherSet.getLastWeatherForecastCondition()
							.setCondition(dataAttribute);
				}
			}
			else if (localName.equals("temp_f"))
			{
				myWeatherSet.getWeatherCurrentCondition()
						.setTempFahrenheit(Integer.parseInt(dataAttribute));
			}
			else if (localName.equals("temp_c"))
			{
				myWeatherSet.getWeatherCurrentCondition().setTempCelcius(
						Integer.parseInt(dataAttribute));
			}
			else if (localName.equals("humidity"))
			{
				myWeatherSet.getWeatherCurrentCondition().setHumidity(
						dataAttribute);
			}
			else if (localName.equals("wind_condition"))
			{
				myWeatherSet.getWeatherCurrentCondition()
						.setWindCondition(dataAttribute);
			}
			else if (localName.equals("low"))
			{
				int temp = Integer.parseInt(dataAttribute);
				if (usingSITemperature)
				{
					myWeatherSet.getLastWeatherForecastCondition()
							.setTempMinCelsius(temp);
				}
				else
				{
					myWeatherSet.getLastWeatherForecastCondition()
							.setTempMinCelsius(
									WeatherUtils.fahrenheitToCelsius(temp));
				}
			}
			else if (localName.equals("high"))
			{
				int temp = Integer.parseInt(dataAttribute);
				if (usingSITemperature)
				{
					myWeatherSet.getLastWeatherForecastCondition()
							.setTempMaxCelsius(temp);
				}
				else
				{
					myWeatherSet.getLastWeatherForecastCondition()
							.setTempMaxCelsius(
									WeatherUtils.fahrenheitToCelsius(temp));
				}
			}
		}
	}

	@Override
	public void endElement(String namespaceURI, String localName, String qName)
			throws SAXException
	{
		if (localName.equals("forecast_information"))
		{
			in_forecast_information = false;
		}
		else if (localName.equals("current_conditions"))
		{
			in_current_conditions = false;
		}
		else if (localName.equals("forecast_conditions"))
		{
			in_forecast_conditions = false;
		}
	}

	@Override
	public void characters(char ch[], int start, int length)
	{

	}
}
