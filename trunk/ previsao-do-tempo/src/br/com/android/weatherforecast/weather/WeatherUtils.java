package br.com.android.weatherforecast.weather;

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
    	int retorno = R.drawable.partly_cloud;
    	
		if(fileName.contains("cloudy"))
			retorno = R.drawable.partly_cloud;
		else if(fileName.contains("snow"))
			retorno = R.drawable.snow;
		else if(fileName.startsWith("storm"))
			retorno = R.drawable.thunderstorm;
		else if(fileName.contains("sunny"))
			retorno = R.drawable.sunny;
		else if(fileName.contains("rain"))
			retorno = R.drawable.rain;
		else if(fileName.contains("haze") || fileName.contains("fog"))
			retorno = R.drawable.fog;
		else if(fileName.contains("mist"))
			retorno = R.drawable.rain;
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
