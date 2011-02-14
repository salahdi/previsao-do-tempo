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
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
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
import br.com.android.weatherforecast.weather.WeatherPreferences;
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
	private EditText txtCidade;
	private Button btnOk;
	private Event event = new Event();
	private WeatherPreferences weatherPref;
	private ProgressDialog progressDialog;
	private Handler handler = new Handler();

	@Override
	public void onCreate(Bundle icicle)
	{
		super.onCreate(icicle);
		setContentView(R.layout.main);
		txtCidade = ((EditText) findViewById(R.id.edit_input));
		btnOk = ((Button) findViewById(R.id.cmd_submit));
		txtCidade.setOnKeyListener(event);
		txtCidade.setOnClickListener(event);
		btnOk.setOnClickListener(event);
	}

	public void onStart()
	{
		super.onStart();
		if (!WeatherUtils.checkInternet(this))
			WeatherUtils.showMessage(this, "Sem Conexão");
		weatherPref = new WeatherPreferences(getSharedPreferences("weatherPref", MODE_PRIVATE));
		txtCidade.setText(weatherPref.getCity());
		searchWeatherInfoOffLine();
	}

	private void updateWeatherInfoView(int aResourceID, WeatherForecastCondition aWFIS) throws MalformedURLException
	{
		((SingleWeatherInfoView) findViewById(aResourceID)).setImageDrawable(getResources().getDrawable(WeatherUtils.getImageDrawable(aWFIS.getIconURL().split("/")[4])));
		((SingleWeatherInfoView) findViewById(aResourceID)).setTempString(aWFIS.getDayofWeek() + ":\n" + aWFIS.getCondition() + " (" + aWFIS.getTempMinCelsius() + "°C/" + aWFIS.getTempMaxCelsius() + "°C)");
	}

	private void updateWeatherInfoView(int aResourceID, WeatherCurrentCondition aWCIS, WeatherForecastInformation aWFI)
	{
		((SingleWeatherInfoView) findViewById(aResourceID)).setImageDrawable(getResources().getDrawable(WeatherUtils.getImageDrawable(aWCIS.getIconURL().split("/")[4])));
		((SingleWeatherInfoView) findViewById(aResourceID)).setTempString("Agora:\n" + aWCIS.getCondition() + " (" + aWCIS.getTempCelcius() + "°C)");
		((TextView) findViewById(R.id.lblCidade)).setText(txtCidade.getText());
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

	private void searchWeatherInfo()
	{
		handler.post(new Runnable()
		{
			@Override
			public void run()
			{
				String cityParam = txtCidade.getText().toString();
				WeatherSet ws;

				try
				{
					if (cityParam.equals(""))
						WeatherUtils.showMessage(WeatherForecast.this, "Informe a Cidade");
					else
					{
						weatherPref.setCity(cityParam);
						ws = getWeatherSet(WeatherForecast.this, cityParam);
						if (ws != null)
						{
							updateWeatherInfoView(R.id.weather_today, ws.getWeatherCurrentCondition(), ws.getWeatherForecastInformation());
							updateWeatherInfoView(R.id.weather_1, ws.getWeatherForecastConditions().get(0));
							updateWeatherInfoView(R.id.weather_2, ws.getWeatherForecastConditions().get(1));
							updateWeatherInfoView(R.id.weather_3, ws.getWeatherForecastConditions().get(2));
							updateWeatherInfoView(R.id.weather_4, ws.getWeatherForecastConditions().get(3));
							((TextView)findViewById(R.id.lblAtualizacao)).setText("Atualizado: " + new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date(System.currentTimeMillis())));
							weatherPref.setTime(System.currentTimeMillis());
							WeatherForecast.this.startService(new Intent(WeatherForecast.this, UpdateService.class));
						}
					}
				}
				catch (UnknownHostException e)
				{
					WeatherUtils.showMessage(WeatherForecast.this, "Sem Conexão");
					searchWeatherInfoOffLine();
				}
				catch (Exception e)
				{
					WeatherUtils.showMessage(WeatherForecast.this, e.getMessage());
					Log.e(WeatherForecast.DEBUG_TAG, e.getMessage(), e);
					resetWeatherInfoViews();
				}
			}
		});
	}

	private void searchWeatherInfoOffLine()
	{
		WeatherSet ws;

		try
		{
			ws = getWeatherSetOffLine(this);
			if (ws != null)
			{
				updateWeatherInfoView(R.id.weather_today, ws.getWeatherCurrentCondition(), ws.getWeatherForecastInformation());
				updateWeatherInfoView(R.id.weather_1, ws.getWeatherForecastConditions().get(0));
				updateWeatherInfoView(R.id.weather_2, ws.getWeatherForecastConditions().get(1));
				updateWeatherInfoView(R.id.weather_3, ws.getWeatherForecastConditions().get(2));
				updateWeatherInfoView(R.id.weather_4, ws.getWeatherForecastConditions().get(3));
				((TextView)findViewById(R.id.lblAtualizacao)).setText("Atualizado:" + new SimpleDateFormat("dd/MM/yyyy HH:mm").format(weatherPref.getTime()));
			}
		}
		catch (Exception e)
		{
			WeatherUtils.showMessage(this, e.getMessage());
			Log.e(WeatherForecast.DEBUG_TAG, e.getMessage(), e);
			resetWeatherInfoViews();
		}
	}

	public WeatherSet getWeatherSet(Context context, String cityParam) throws MalformedURLException, IOException, SAXException, ParserConfigurationException
	{
		String line;
		String queryString;
		GoogleWeatherHandler gwh = new GoogleWeatherHandler();
		StringBuilder xml = new StringBuilder();
		URLConnection connection;
		BufferedReader reader;
		ByteArrayInputStream in;
		XMLReader xr;
		WeatherSet ws = null;

		if (cityParam.equals(""))
			return ws;
		if(weatherPref == null)
			weatherPref = new WeatherPreferences(context.getSharedPreferences("weatherPref", MODE_PRIVATE));
		queryString = "http://www.google.com/ig/api?weather=" + cityParam + "&hl=pt-br";
		connection = new URL(queryString.replace(" ", "%20")).openConnection();
		connection.setConnectTimeout(1000 * 5);
		reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), Charset.forName("UTF-8")));
		while ((line = reader.readLine()) != null)
			xml.append(line);
		weatherPref.setXml(xml.toString());
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
		String xml;
		ByteArrayInputStream in;
		XMLReader xr;
		WeatherSet ws = null;

		if(weatherPref == null)
			weatherPref = new WeatherPreferences(getSharedPreferences("weatherPref", MODE_PRIVATE));
		xml = weatherPref.readXml();
		if (!xml.equals(""))
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
			if (arg0.equals(btnOk))
			{
				txtCidade.setText(WeatherUtils.captalizeWords(txtCidade.getText().toString()));
				((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(arg0.getWindowToken(), 0);
				progressDialog = ProgressDialog.show(WeatherForecast.this, "Aguarde", "Consultando Google Weather");
				new Thread(){
					@Override
					public void run()
					{
						searchWeatherInfo();
						progressDialog.dismiss();
					}
				}.start();
			}
		}

		@Override
		public boolean onKey(View arg0, int arg1, KeyEvent arg2)
		{
			if (arg2.getAction() == KeyEvent.ACTION_DOWN && arg2.getKeyCode() == KeyEvent.KEYCODE_ENTER)
			{
				txtCidade.setText(WeatherUtils.captalizeWords(txtCidade.getText().toString()));
				((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(arg0.getWindowToken(), 0);
				searchWeatherInfo();
			}
			return false;
		}
	}
}