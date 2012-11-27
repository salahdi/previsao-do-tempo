package br.com.android.weatherforecast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import org.json.JSONException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import br.com.android.weatherforecast.views.WeatherLine;
import br.com.android.weatherforecast.weather.WeatherCurrentCondition;
import br.com.android.weatherforecast.weather.WeatherForecastCondition;
import br.com.android.weatherforecast.weather.WeatherIcons;
import br.com.android.weatherforecast.weather.WeatherPreferences;
import br.com.android.weatherforecast.weather.WeatherSet;
import br.com.android.weatherforecast.weather.WeatherUtils;
import br.com.android.weatherforecast.weather.WundergroundDecoder;
import br.com.android.weatherforecast.widget.Widget.UpdateService;

/**
 * Classe para Gerenciamento da Aplicação de Previsão do Tempo
 * @author Felipe Cobello
 *
 */
public class WeatherForecast extends Activity
{
	public final static String DEBUG_TAG = "WEATHER_FORECAST";
	private EditText txtCidade;
	private ImageButton btnOk;
	private Event event = new Event();
	private WeatherPreferences weatherPref;
	private LocationManager location;
	private Geocoder geo;
	private boolean firstStart = true;

	@Override
	public void onCreate(Bundle icicle)
	{
		super.onCreate(icicle);
		setContentView(R.layout.main);
		txtCidade = ((EditText) findViewById(R.id.edit_input));
		btnOk = ((ImageButton) findViewById(R.id.cmd_submit));
		txtCidade.setOnKeyListener(event);
		txtCidade.setOnClickListener(event);
		btnOk.setOnClickListener(event);
	}

	public void onStart()
	{
		super.onStart();
		if(firstStart)
		{
			weatherPref = new WeatherPreferences(getSharedPreferences("weatherPref", MODE_PRIVATE));
			txtCidade.setText(weatherPref.getCity());
			firstStart = false;
			update();
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		
		inflater.inflate(R.menu.menu, menu);
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
		Location myLocation;
		try
		{
			geo = new Geocoder(this, Locale.getDefault());
			location = (LocationManager)getSystemService(LOCATION_SERVICE);
			myLocation = location.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
			if(myLocation == null)
				myLocation = location.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			enderecos = geo.getFromLocation(myLocation.getLatitude(), myLocation.getLongitude(), 1);
			if(enderecos.size() > 0)
				txtCidade.setText(enderecos.get(0).getLocality());
			else
				txtCidade.setText(weatherPref.getCity());
			update();
		}
		catch (Exception e) 
		{
			Log.e(WeatherForecast.DEBUG_TAG, e.getMessage(), e);
			WeatherUtils.showMessage(WeatherForecast.this, getString(R.string.locationErrorMsg));
			resetWeatherInfoViews();
		}
	}

	/**
	 * Atualiza Previsão dos Proximos Dias
	 * @param aResourceID Identificador do Componentem que vai ser atualizado
	 * @param aWFIS Informaçoes do Tempo
	 * @throws MalformedURLException
	 */
	private void updateWeatherInfoView(int aResourceID, WeatherForecastCondition aWFIS)
	{
		((WeatherLine) findViewById(aResourceID)).setImageDrawable(getResources().getDrawable(WeatherIcons.getImageDrawable(aWFIS.getIconURL())));
		((WeatherLine) findViewById(aResourceID)).setTempString(aWFIS.getDayofWeek() + "\n" + aWFIS.getCondition() + "\n" + aWFIS.getTempMin() + "°C/" + aWFIS.getTempMax() + "°C" + "\n" + getString(R.string.rain) + ": " + aWFIS.getPrecipitation());
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
		((TextView) findViewById(R.id.weather_today_condition)).setText(aWCIS.getCondition() + "\n" + getString(R.string.wind) + " " + aWCIS.getWindCondition() + "\n" + getString(R.string.humidity) + ": " + aWCIS.getHumidity());
		((TextView) findViewById(R.id.lblProximosDias)).setText(getString(R.string.nextDays));
		
	}

	/**
	 * Reinicia Informacoes
	 */
	private void resetWeatherInfoViews()
	{
		((ImageView)findViewById(R.id.imgWeather)).setImageDrawable(getResources().getDrawable(R.drawable.undefined));
		((TextView) findViewById(R.id.weather_today_city)).setText(getString(R.string.city));
		((TextView) findViewById(R.id.weather_today_temp)).setText("0 °C");
		((TextView) findViewById(R.id.weather_today_condition)).setText(getString(R.string.condition));
		((WeatherLine) findViewById(R.id.weather_1)).reset();
		((WeatherLine) findViewById(R.id.weather_2)).reset();
		((WeatherLine) findViewById(R.id.weather_3)).reset();
	}

	private void searchWeatherInfo()
	{
		WeatherSet ws = null;

		try
		{
			
			if(weatherPref.getCity().equalsIgnoreCase(txtCidade.getText().toString()))
			{
				if((System.currentTimeMillis() - weatherPref.getLastUpdate().getTime()) > 1800000)
				{
					ws = getWeatherSet(txtCidade.getText().toString());
					saveCache(ws);
				}
				else{
					ws = restoreCache();
				}
			}
			else
			{
				weatherPref.setCity(txtCidade.getText().toString());
				ws = getWeatherSet(txtCidade.getText().toString());
				saveCache(ws);
			}
			if (ws != null)
			{
				updateWeatherInfoView(ws.getWeatherCurrentCondition());
				updateWeatherInfoView(R.id.weather_1, ws.getWeatherForecastConditions().get(1));
				updateWeatherInfoView(R.id.weather_2, ws.getWeatherForecastConditions().get(2));
				updateWeatherInfoView(R.id.weather_3, ws.getWeatherForecastConditions().get(3));
				((TextView)findViewById(R.id.lblAtualizacao)).setText(getString(R.string.lastUpdate) + ": " + new SimpleDateFormat("dd/MM/yyyy HH:mm").format(weatherPref.getLastUpdate()));
				WeatherForecast.this.startService(new Intent(WeatherForecast.this, UpdateService.class));
			}
			else
				WeatherUtils.showMessage(WeatherForecast.this, getString(R.string.forecastNotFound));
		}
		catch (Exception e)
		{
			WeatherUtils.showMessage(WeatherForecast.this, e.getMessage());
			Log.e(WeatherForecast.DEBUG_TAG, e.getMessage(), e);
		}
	}

	public WeatherSet getWeatherSet(String cityParam) throws InterruptedException, ExecutionException
	{
		WundergroundDecoder decoder = new WundergroundDecoder(WeatherForecast.this);
		decoder.execute(cityParam); 
		
		return decoder.get();
	}
	
	private void update()
	{
		Thread t; 
		final ProgressDialog processDialog;
		final Handler handler = new Handler();
		
		if (txtCidade.getText().toString().equals(""))
			WeatherUtils.showMessage(WeatherForecast.this, getString(R.string.enterCity));
		else
		{
			processDialog = ProgressDialog.show(WeatherForecast.this, "", getString(R.string.processMsg));
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
					processDialog.dismiss();
				}
			};
			t.start();
		}
	}
	
	private void saveCache(WeatherSet weatherset) throws IOException
	{
		File cache = new File(getCacheDir(), "cache.dat");
		ObjectOutputStream out;

		out = new ObjectOutputStream(new FileOutputStream(cache));
		out.writeObject(weatherset);
		out.close();
		weatherPref.setLastUpdate(new Date(System.currentTimeMillis()));
	}
	
	private WeatherSet restoreCache() throws FileNotFoundException, IOException, ClassNotFoundException, JSONException, InterruptedException, ExecutionException
	{
		File cache = new File(getCacheDir(), "cache.dat");
		WeatherSet ws = null;
		ObjectInputStream out;
		
		if(cache.exists())
		{
			out = new ObjectInputStream(new FileInputStream(new File(getCacheDir(), "cache.dat")));
			ws = (WeatherSet) out.readObject();
			out.close();
		}
		if(ws == null)
			ws = getWeatherSet(txtCidade.getText().toString());
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