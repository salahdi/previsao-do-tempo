package br.com.android.weatherforecast.weather;

import java.util.Calendar;
import br.com.android.weatherforecast.R;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Utilitarios
 * @author Felipe Cobello
 *
 */
public class WeatherUtils {

	public static int fahrenheitToCelsius(int tFahrenheit) 
	{
		return (int) ((5.0f / 9.0f) * (tFahrenheit - 32));
	}

	public static int celsiusToFahrenheit(int tCelsius) 
	{
		return (int) ((9.0f / 5.0f) * tCelsius + 32);
	}
	
	public static boolean checkInternet(Context ctx) 
	{
	    NetworkInfo info = (NetworkInfo) ((ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
	    
	    if (info == null || !info.isConnected()) 
	        return false;
	    if (info.isRoaming())
	        return false;
	    return true;
	}
	
	public static void showMessage(Context context, String msg)
	{	
		OnClickListener listener = new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				return;
			}
		};
		
		showMessage(context, msg, "OK", listener);
	}
	
	public static void showMessage(Context context, String msg, String button, OnClickListener listener)
	{
		AlertDialog alert = new AlertDialog.Builder(context).create();
		
		alert.setButton(button, listener);
		alert.setMessage(msg);
		alert.setIcon(android.R.drawable.ic_dialog_info);
		alert.show();
	}
	
	public static int getImageDrawable(String fileName) 
	{
		boolean day = Calendar.getInstance().get(Calendar.HOUR_OF_DAY) > 6 && Calendar.getInstance().get(Calendar.HOUR_OF_DAY) < 18;
    	int retorno = R.drawable.undefined;
    	
		if(fileName.startsWith("partly_cloudy") || fileName.startsWith("cloudy"))
			retorno = day ? R.drawable.cloud:R.drawable.cloud_night;
		else if(fileName.startsWith("mostly_cloudy"))
			retorno = day ? R.drawable.mostly_cloudy:R.drawable.cloud_night;
		else if(fileName.contains("rain_snow"))
			retorno = day ? R.drawable.rain_snow:R.drawable.snow_night;
		else if(fileName.contains("snow"))
			retorno = day ? R.drawable.snow:R.drawable.snow_night;
		else if(fileName.startsWith("storm"))
			retorno = day ? R.drawable.thunderstorm:R.drawable.thunderstorm_night;
		else if(fileName.contains("sunny"))
			retorno = day ? R.drawable.sunny:R.drawable.sunny_night;
		else if(fileName.contains("rain"))
			retorno = day ? R.drawable.rain:R.drawable.rain_night;
		else if(fileName.contains("haze") || fileName.contains("fog"))
			retorno = day ? R.drawable.fog:R.drawable.fog_night;
		else if(fileName.contains("mist"))
			retorno = day ? R.drawable.rain:R.drawable.rain_night;
		return retorno;
	}
	
	public static String captalizeWords(String words)
	{
		char[] caracteres = words.toCharArray();
		
		for (int i = 0; i < caracteres.length; i++)
		{
			if(i == 0 || caracteres[i-1] == ' ')
			{
				caracteres[i] = Character.toUpperCase(caracteres[i]);
			}
		}
		return new String(caracteres);
	}
}
