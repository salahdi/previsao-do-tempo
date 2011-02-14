package br.com.android.weatherforecast.views;

import br.com.android.weatherforecast.R;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Componente para Exibir o Icone + os dados do Clima
 */
public class SingleWeatherInfoView extends LinearLayout
{
	private ImageView myWeatherImageView = null;
	private TextView myTempTextView = null;

	public SingleWeatherInfoView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		myWeatherImageView = new ImageView(context);
		myTempTextView = new TextView(context);
		myTempTextView.setTextSize(12);
		myTempTextView.setTextColor(Color.WHITE);
		myTempTextView.setTypeface(Typeface.create("Tahoma", Typeface.BOLD));
		addView(myWeatherImageView, new LinearLayout.LayoutParams(android.view.ViewGroup.LayoutParams.WRAP_CONTENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT));
		addView(myTempTextView, new LinearLayout.LayoutParams(android.view.ViewGroup.LayoutParams.WRAP_CONTENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT));
	}

	public void reset()
	{
		myWeatherImageView.setImageDrawable(getResources().getDrawable(R.drawable.undefined));
		myTempTextView.setText("");
	}

	public void setImageDrawable(Drawable drawable)
	{
		myWeatherImageView.setImageDrawable(drawable);
	}

	public void setTempCelcius(int aTemp)
	{
		myTempTextView.setText("" + aTemp + " °C");
	}

	public void setTempFahrenheit(int aTemp)
	{
		myTempTextView.setText("" + aTemp + " °F");
	}

	public void setTempFahrenheitMinMax(int aMinTemp, int aMaxTemp)
	{
		myTempTextView.setText("" + aMinTemp + "/" + aMaxTemp + " °F");
	}

	public void setTempCelciusMinMax(int aMinTemp, int aMaxTemp)
	{
		myTempTextView.setText("" + aMinTemp + "/" + aMaxTemp + " °C");
	}

	public void setTempString(String aTempString)
	{
		myTempTextView.setText(aTempString);
	}
}