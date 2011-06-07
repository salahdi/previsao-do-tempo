package br.com.android.weatherforecast;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import br.com.android.weatherforecast.views.WeatherLine;
import br.com.android.weatherforecast.weather.GoogleWeatherHandler;
import br.com.android.weatherforecast.weather.WeatherCurrentCondition;
import br.com.android.weatherforecast.weather.WeatherForecastCondition;
import br.com.android.weatherforecast.weather.WeatherIcons;
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
	private LocationManager location;
	private Geocoder geo;

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
		weatherPref = new WeatherPreferences(getSharedPreferences("weatherPref", MODE_PRIVATE));
		txtCidade.setText(weatherPref.getCity());
		update();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		super.getMenuInflater().inflate(R.menu.menu, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case R.id.itmSair:
				super.finish();
				return true;
			case R.id.itmAtualizar:
				update();
				return true;
			case R.id.itmLocation:
				searchLocation();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
	
	private void searchLocation()
	{
		List<Address> enderecos;
		
		try
		{
			geo = new Geocoder(this, Locale.getDefault());
			location = (LocationManager)getSystemService(LOCATION_SERVICE);
			enderecos = geo.getFromLocation(location.getLastKnownLocation(LocationManager.NETWORK_PROVIDER).getLatitude(), location.getLastKnownLocation(LocationManager.NETWORK_PROVIDER).getLongitude(), 1);
			if(enderecos.size() > 0)
				txtCidade.setText(enderecos.get(0).getLocality());
			else
				txtCidade.setText(weatherPref.getCity());
			update();
		}
		catch (IOException e) 
		{
			Log.e(WeatherForecast.DEBUG_TAG, e.getMessage(), e);
		}
	}

	/**
	 * Atualiza Previsão dos Proximos Dias
	 * @param aResourceID Identificador do Componentem que vai ser atualizado
	 * @param aWFIS Informaçoes do Tempo
	 * @throws MalformedURLException
	 */
	private void updateWeatherInfoView(int aResourceID, WeatherForecastCondition aWFIS) throws MalformedURLException
	{
		((WeatherLine) findViewById(aResourceID)).setImageDrawable(getResources().getDrawable(WeatherIcons.getImageDrawable(aWFIS.getIconURL())));
		((WeatherLine) findViewById(aResourceID)).setTempString(aWFIS.getDayofWeek() + ":\n" + aWFIS.getTempMinCelsius() + "°C/" + aWFIS.getTempMaxCelsius() + "°C");
	}

	/**
	 * Atualiza Previsão Atual
	 * @param aWCIS Informaçoes do Tempo
	 */
	private void updateWeatherInfoView(WeatherCurrentCondition aWCIS)
	{
		((ImageView) findViewById(R.id.imgWeather)).setImageDrawable(getResources().getDrawable(WeatherIcons.getImageDrawable(aWCIS.getIconURL())));
		((TextView) findViewById(R.id.weather_today_temp)).setText(aWCIS.getTempCelcius() + "°C");
		((TextView) findViewById(R.id.weather_today_city)).setText(txtCidade.getText().toString());
		((TextView) findViewById(R.id.weather_today_condition)).setText(aWCIS.getCondition() + "\n" + aWCIS.getWindCondition());
		((TextView) findViewById(R.id.lblProximosDias)).setText("Próximos Dias");
		
	}

	/**
	 * Reinicia Informacoes
	 */
	private void resetWeatherInfoViews()
	{
		((ImageView)findViewById(R.id.imgWeather)).setImageDrawable(getResources().getDrawable(R.drawable.undefined));
		((TextView) findViewById(R.id.weather_today_city)).setText("Cidade");
		((TextView) findViewById(R.id.weather_today_temp)).setText("0 °C");
		((TextView) findViewById(R.id.weather_today_condition)).setText("Condição");
		((WeatherLine) findViewById(R.id.weather_1)).reset();
		((WeatherLine) findViewById(R.id.weather_2)).reset();
		((WeatherLine) findViewById(R.id.weather_3)).reset();
	}

	private void searchWeatherInfo()
	{
		WeatherSet ws;

		try
		{
			if (txtCidade.getText().toString().equals(""))
				WeatherUtils.showMessage(WeatherForecast.this, "Informe a Cidade");
			else
			{
				weatherPref.setCity(txtCidade.getText().toString());
				ws = getWeatherSet(WeatherForecast.this, txtCidade.getText().toString());
				if (ws != null)
				{
					updateWeatherInfoView(ws.getWeatherCurrentCondition());
					updateWeatherInfoView(R.id.weather_1, ws.getWeatherForecastConditions().get(1));
					updateWeatherInfoView(R.id.weather_2, ws.getWeatherForecastConditions().get(2));
					updateWeatherInfoView(R.id.weather_3, ws.getWeatherForecastConditions().get(3));
					((TextView)findViewById(R.id.lblAtualizacao)).setText("Atualizado: " + new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date(System.currentTimeMillis())));
					WeatherForecast.this.startService(new Intent(WeatherForecast.this, UpdateService.class));
				}
			}
		}
		catch (UnknownHostException e)
		{
			WeatherUtils.showMessage(WeatherForecast.this, "Verifique a Conexão de Internet.");
			Log.e(DEBUG_TAG, e.getMessage(), e);
			searchWeatherInfoOffLine();
		}
		catch (Exception e)
		{
			WeatherUtils.showMessage(WeatherForecast.this, e.getMessage());
			Log.e(WeatherForecast.DEBUG_TAG, e.getMessage(), e);
			searchWeatherInfoOffLine();
		}
	}

	private void searchWeatherInfoOffLine()
	{
		WeatherSet ws;

		try
		{
			ws = getWeatherSetOffLine(WeatherForecast.this);
			if (ws != null)
			{
				updateWeatherInfoView(ws.getWeatherCurrentCondition());
				updateWeatherInfoView(R.id.weather_1, ws.getWeatherForecastConditions().get(1));
				updateWeatherInfoView(R.id.weather_2, ws.getWeatherForecastConditions().get(2));
				updateWeatherInfoView(R.id.weather_3, ws.getWeatherForecastConditions().get(3));
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
		queryString = "http://www.google.com/ig/api?weather=" + URLEncoder.encode(cityParam, "UTF-8") + "&hl=pt-br";
		connection = new URL(queryString.replace(" ", "%20")).openConnection();
		connection.setConnectTimeout(1000 * 5);
		reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), Charset.forName("UTF-8")));
		while ((line = reader.readLine()) != null)
			xml.append(line);
		weatherPref.setXml(xml.toString());
		weatherPref.setTime(System.currentTimeMillis());
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
	
	private void update()
	{
		Thread t; 
		
		progressDialog = ProgressDialog.show(WeatherForecast.this, "Aguarde", "Consultando Google Weather");
		t = new Thread(){
			@Override
			public void run()
			{
				handler.post(new Runnable()
				{
					@Override
					public void run()
					{
						searchWeatherInfo();
					}
				});
				progressDialog.dismiss();
			}
		};
		t.start();
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
				update();
			}
		}

		@Override
		public boolean onKey(View arg0, int arg1, KeyEvent arg2)
		{
			boolean retorno = false;
			
			if(arg0.equals(txtCidade))
			{
				if (arg2.getAction() == KeyEvent.ACTION_DOWN && arg2.getKeyCode() == KeyEvent.KEYCODE_ENTER)
				{
					retorno = true;
					txtCidade.setText(WeatherUtils.captalizeWords(txtCidade.getText().toString()));
					((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(arg0.getWindowToken(), 0);
					update();
				}
			}
			return retorno;
		}
	}
}