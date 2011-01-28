package br.com.android.weatherforecast.widget;

import java.net.UnknownHostException;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;
import br.com.android.weatherforecast.R;
import br.com.android.weatherforecast.WeatherForecast;
import br.com.android.weatherforecast.WeatherPreferences;
import br.com.android.weatherforecast.weather.WeatherSet;
import br.com.android.weatherforecast.weather.WeatherUtils;

/**
 * Classe para Gerenciamento do Widget de Previsão do Tempo
 * @author Felipe Cobello
 * @version 1.0
 */
public class Widget extends AppWidgetProvider
{
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
	{
		context.startService(new Intent(context, UpdateService.class));
	}

	/**
	 * Classe para o Gerencimento do Serviço Atualização
	 * @author Felipe Cobello
	 * @version 1.0
	 */
	public static class UpdateService extends Service
	{
		@Override
		public void onStart(Intent intent, int startId)
		{
			RemoteViews updateViews = buildUpdate(this);
			ComponentName thisWidget = new ComponentName(this, Widget.class);
			AppWidgetManager manager = AppWidgetManager.getInstance(this);

			manager.updateAppWidget(thisWidget, updateViews);
		}

		/**
		 * Retorna o {@link RemoteViews} com a Atualização do Clima
		 * @param context Context
		 * @return RemoteViews
		 */
		public RemoteViews buildUpdate(Context context)
		{
			RemoteViews updateViews = new RemoteViews(context.getPackageName(), R.layout.widget);
			WeatherPreferences preferences = new WeatherPreferences(getSharedPreferences("weatherPref", MODE_PRIVATE));

			updateViews = updateWeatherInfo(context, preferences.getCity());
			return updateViews;
		}

		/**
		 * Retorna o {@link RemoteViews} com a Atualização do Clima On-Line
		 * @param context Context
		 * @param city Cidade
		 * @return RemoteViews
		 */
		private RemoteViews updateWeatherInfo(Context context, String city)
		{
			RemoteViews updateViews = new RemoteViews(context.getPackageName(), R.layout.widget);
			Intent defineIntent = new Intent(context, WeatherForecast.class);
			PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, defineIntent, 0);
			WeatherSet weather;

			try
			{
				weather = new WeatherForecast().getWeatherSet(context, city);
				updateViews.setOnClickPendingIntent(R.id.widget, pendingIntent);
				if (weather != null)
				{
					updateViews.setTextViewText(R.id.definition, weather.getWeatherForecastInformation().getCity().split("[,]")[0] + "\n" + weather.getWeatherCurrentCondition().getTempCelcius()
							+ "°C");
					updateViews.setImageViewResource(R.id.image, WeatherUtils.getImageDrawable(weather.getWeatherCurrentCondition().getIconURL().split("/")[4]));
				}
			}
			catch (UnknownHostException e)
			{
				updateViews = updateWeatherInfoOffline(context);
			}
			catch (Exception e)
			{
				Log.e(WeatherForecast.DEBUG_TAG, e.getMessage(), e);
			}
			return updateViews;
		}

		/**
		 * Retorna o {@link RemoteViews} com a Atualização do Clima Off-Line
		 * @param context Context
		 * @return RemoteViews
		 */
		private RemoteViews updateWeatherInfoOffline(Context context)
		{
			RemoteViews updateViews = new RemoteViews(context.getPackageName(), R.layout.widget);
			Intent defineIntent = new Intent(context, WeatherForecast.class);
			PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, defineIntent, 0);
			WeatherSet weather;

			try
			{
				weather = new WeatherForecast().getWeatherSetOffLine(context);
				updateViews.setOnClickPendingIntent(R.id.widget, pendingIntent);
				if (weather != null)
				{
					updateViews.setTextViewText(R.id.definition, weather.getWeatherForecastInformation().getCity().split("[,]")[0] + "\n" + weather.getWeatherCurrentCondition().getTempCelcius()
							+ "°C");
					updateViews.setImageViewResource(R.id.image, WeatherUtils.getImageDrawable(weather.getWeatherCurrentCondition().getIconURL().split("/")[4]));
				}
			}
			catch (Exception e)
			{
				Log.e(WeatherForecast.DEBUG_TAG, e.getMessage(), e);
			}
			return updateViews;
		}

		@Override
		public IBinder onBind(Intent intent)
		{
			return null;
		}
	}
}
