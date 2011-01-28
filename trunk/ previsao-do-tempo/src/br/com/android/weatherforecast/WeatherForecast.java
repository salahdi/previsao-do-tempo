package br.com.android.weatherforecast;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import br.com.android.weatherforecast.views.SingleWeatherInfoView;
import br.com.android.weatherforecast.weather.GoogleWeatherHandler;
import br.com.android.weatherforecast.weather.WeatherCurrentCondition;
import br.com.android.weatherforecast.weather.WeatherForecastCondition;
import br.com.android.weatherforecast.weather.WeatherForecastInformation;
import br.com.android.weatherforecast.weather.WeatherSet;
import br.com.android.weatherforecast.weather.WeatherUtils;
import br.com.android.weatherforecast.widget.Widget.UpdateService;

/**
 * Classe para Gerenciamento da Aplicação de Previsão do Tempo
 * @author Felipe Cobello
 * @version 1.0
 *
 */
public class WeatherForecast extends Activity
{
	public final static String DEBUG_TAG = "WEATHER_FORECAST";
	private Event event = new Event();

	@Override
	public void onCreate(Bundle icicle)
	{
		super.onCreate(icicle);
		setContentView(R.layout.main);
		((Button) findViewById(R.id.cmd_submit)).setOnClickListener(event);
		((EditText) findViewById(R.id.edit_input)).setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
		((EditText) findViewById(R.id.edit_input)).setOnKeyListener(event);
		((EditText) findViewById(R.id.edit_input)).setOnClickListener(event);
	}

	public void onStart()
	{
		WeatherPreferences preferences;

		super.onStart();
		if (!WeatherUtils.checkInternet(this))
		{
			WeatherUtils.showMessage(this, "Sem Conexão");
			searchWeatherInfoOffLine(this);
		}
		preferences = new WeatherPreferences(getSharedPreferences("weatherPref", MODE_PRIVATE));
		((EditText) findViewById(R.id.edit_input)).setText(preferences.getCity());
		searchWeatherInfoOffLine(this);
	}

	private void updateWeatherInfoView(int aResourceID, WeatherForecastCondition aWFIS) throws MalformedURLException
	{
		((SingleWeatherInfoView) findViewById(aResourceID)).setImageDrawable(getResources().getDrawable(WeatherUtils.getImageDrawable(aWFIS.getIconURL().split("/")[4])));
		((SingleWeatherInfoView) findViewById(aResourceID)).setTempString(aWFIS.getDayofWeek() + ":\n" + aWFIS.getCondition() + " (" + aWFIS.getTempMinCelsius() + "°C/" + aWFIS.getTempMaxCelsius()
				+ "°C)");
	}

	private void updateWeatherInfoView(int aResourceID, WeatherCurrentCondition aWCIS, WeatherForecastInformation aWFI)
	{
		((SingleWeatherInfoView) findViewById(aResourceID)).setImageDrawable(getResources().getDrawable(WeatherUtils.getImageDrawable(aWCIS.getIconURL().split("/")[4])));
		((TextView) findViewById(R.id.lblCidade)).setText(aWFI.getCity());
		((SingleWeatherInfoView) findViewById(aResourceID)).setTempString("Agora:\n" + aWCIS.getCondition() + " (" + aWCIS.getTempCelcius() + "°C)");
	}

	private void resetWeatherInfoViews()
	{
		((TextView) findViewById(R.id.lblCidade)).setText("");
		((SingleWeatherInfoView) findViewById(R.id.weather_today)).reset();
		((SingleWeatherInfoView) findViewById(R.id.weather_1)).reset();
		((SingleWeatherInfoView) findViewById(R.id.weather_2)).reset();
		((SingleWeatherInfoView) findViewById(R.id.weather_3)).reset();
		((SingleWeatherInfoView) findViewById(R.id.weather_4)).reset();
	}

	private void searchWeatherInfo(Context context)
	{
		String cityParam = ((EditText) findViewById(R.id.edit_input)).getText().toString();
		Editor editor = context.getSharedPreferences("weatherPref", MODE_PRIVATE).edit();
		WeatherSet ws;

		try
		{
			if (cityParam.equals(""))
				WeatherUtils.showMessage(context, "Informe a Cidade");
			else
			{
				ws = getWeatherSet(context, cityParam);
				if (ws != null)
				{
					updateWeatherInfoView(R.id.weather_today, ws.getWeatherCurrentCondition(), ws.getWeatherForecastInformation());
					updateWeatherInfoView(R.id.weather_1, ws.getWeatherForecastConditions().get(0));
					updateWeatherInfoView(R.id.weather_2, ws.getWeatherForecastConditions().get(1));
					updateWeatherInfoView(R.id.weather_3, ws.getWeatherForecastConditions().get(2));
					updateWeatherInfoView(R.id.weather_4, ws.getWeatherForecastConditions().get(3));
					editor.putString("city", cityParam);
					editor.commit();
					context.startService(new Intent(context, UpdateService.class));
				}
			}
		}
		catch (UnknownHostException e)
		{
			WeatherUtils.showMessage(context, "Sem Conexão");
			searchWeatherInfoOffLine(context);
		}
		catch (Exception e)
		{
			WeatherUtils.showMessage(context, e.getMessage());
			Log.e(WeatherForecast.DEBUG_TAG, e.getMessage(), e);
			resetWeatherInfoViews();
		}
	}

	private void searchWeatherInfoOffLine(Context context)
	{
		WeatherSet ws;

		try
		{
			ws = getWeatherSetOffLine(context);
			if (ws != null)
			{
				updateWeatherInfoView(R.id.weather_today, ws.getWeatherCurrentCondition(), ws.getWeatherForecastInformation());
				updateWeatherInfoView(R.id.weather_1, ws.getWeatherForecastConditions().get(0));
				updateWeatherInfoView(R.id.weather_2, ws.getWeatherForecastConditions().get(1));
				updateWeatherInfoView(R.id.weather_3, ws.getWeatherForecastConditions().get(2));
				updateWeatherInfoView(R.id.weather_4, ws.getWeatherForecastConditions().get(3));
			}
		}
		catch (Exception e)
		{
			WeatherUtils.showMessage(context, e.getMessage());
			Log.e(WeatherForecast.DEBUG_TAG, e.getMessage(), e);
			resetWeatherInfoViews();
		}
	}

	public WeatherSet getWeatherSet(Context context, String cityParam) throws MalformedURLException, IOException, SAXException, ParserConfigurationException
	{
		String line;
		String queryString;
		GoogleWeatherHandler gwh = new GoogleWeatherHandler();
		WeatherPreferences preferences = new WeatherPreferences(context.getSharedPreferences("weatherPref", MODE_PRIVATE));
		StringBuilder xml = new StringBuilder();
		URLConnection connection;
		BufferedReader reader;
		ByteArrayInputStream in;
		XMLReader xr;
		WeatherSet ws = null;

		if (cityParam.equals(""))
			return ws;
		queryString = "http://www.google.com/ig/api?weather=" + cityParam + "&hl=pt-br";
		connection = new URL(queryString.replace(" ", "%20")).openConnection();
		connection.setConnectTimeout(1000 * 5);
		reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), Charset.forName("UTF-8")));
		while ((line = reader.readLine()) != null)
			xml.append(line);
		preferences.setXml(xml.toString());
		in = new ByteArrayInputStream(xml.toString().getBytes());
		xr = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
		xr.setContentHandler(gwh);
		xr.parse(new InputSource(in));
		ws = gwh.getWeatherSet();
		reader.close();
		in.close();
		connection = null;
		reader = null;
		in = null;
		xr = null;
		return ws;
	}

	public WeatherSet getWeatherSetOffLine(Context context) throws SAXException, ParserConfigurationException, IOException
	{
		GoogleWeatherHandler gwh = new GoogleWeatherHandler();
		WeatherPreferences preferences = new WeatherPreferences(context.getSharedPreferences("weatherPref", MODE_PRIVATE));
		String xml;
		ByteArrayInputStream in;
		XMLReader xr;
		WeatherSet ws = null;

		xml = preferences.readXml();
		if (!xml.isEmpty())
		{
			in = new ByteArrayInputStream(xml.toString().getBytes());
			xr = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
			xr.setContentHandler(gwh);
			xr.parse(new InputSource(in));
			ws = gwh.getWeatherSet();
			in.close();
			in = null;
			xr = null;
		}
		return ws;
	}

	/**
	 * Eventos da Tela
	 * @author Felipe Cobello
	 *
	 */
	private class Event implements OnClickListener, OnKeyListener
	{
		@Override
		public void onClick(View arg0)
		{
			if (arg0.equals((Button) findViewById(R.id.cmd_submit)))
			{
				((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(arg0.getWindowToken(), 0);
				searchWeatherInfo(arg0.getContext());
			}
		}

		@Override
		public boolean onKey(View arg0, int arg1, KeyEvent arg2)
		{
			if (arg2.getAction() == KeyEvent.ACTION_DOWN && arg2.getKeyCode() == KeyEvent.KEYCODE_ENTER)
			{
				((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(arg0.getWindowToken(), 0);
				searchWeatherInfo(arg0.getContext());
			}
			return false;
		}
	}
}