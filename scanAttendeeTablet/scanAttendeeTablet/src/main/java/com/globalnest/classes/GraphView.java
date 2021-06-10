package com.globalnest.classes;

import com.globalnest.scanattendee.R;
import com.globalnest.utils.Util;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.view.View;

/**
 * GraphView creates a scaled line or bar graph with x and y axis labels. 
 * @author Arno den Hond
 *
 */
public class GraphView extends View {

	public static boolean BAR = true;
	public static boolean LINE = false;

	private Paint paint;
	private float[] values;
	private String[] horlabels;
	private String[] verlabels;
	private String[] barValue;
	private String title;
	private boolean type;
	int[] bar_colors = new int[] { Color.parseColor("#2ecc71"), Color.parseColor("#3498db"),
			Color.parseColor("#9b59b6"),Color.parseColor("#34495e"),Color.parseColor("#e98b39"),Color.parseColor("#e74c3c"),
			Color.parseColor("#95a5a6"),Color.parseColor("#f1c40f"),Color.parseColor("#d35400"),
			Color.parseColor("#2ecc71"),Color.parseColor("#3498db"),Color.parseColor("#9b59b6"),Color.parseColor("#34495e"),Color.parseColor("#e98b39"),
			Color.parseColor("#e74c3c"),Color.parseColor("#95a5a6"),Color.parseColor("#f1c40f"),Color.parseColor("#d35400")};
	
	public GraphView(Context context, float[] values, String title, String[] horlabels, String[] verlabels,String[] bar, boolean type) {
		super(context);
		if (values == null)
			values = new float[0];
		else
			this.values = values;
		if (title == null)
			title = "";
		else
			this.title = title;
		if (horlabels == null)
			this.horlabels = new String[0];
		else
			this.horlabels = horlabels;
		if (verlabels == null)
			this.verlabels = new String[0];
		else
			this.verlabels = verlabels;
		if (bar == null)
			this.barValue = new String[0];
		else
			this.barValue = bar;
		this.type = type;
		paint = new Paint();
	}
	
	
	
	@Override
	protected void onDraw(Canvas canvas) {
		float border = 20;
		float horstart = border * 2;
		float height = getHeight();
		float width = getWidth() - 1;
		float max = getMax();
		float min = getMin();
		float diff = max - min;
		float graphheight = height - (2 * border);
		float graphwidth = width - (2 * border);
		
		Rect textBounds = new Rect();
		String text="No. Tickets";
		paint.setTextAlign(Align.LEFT);
		//paint.getTextBounds(text, 0, text.length(), textBounds);
		//canvas.drawText("No. Tickets", textBounds.left + 5, textBounds.top + graphheight, paint);
		
		int vers = verlabels.length - 1;
		for (int i = 0; i < verlabels.length; i++) {
			paint.setColor(getResources().getColor(R.color.screen_bg_color));
			float y = ((graphheight / vers) * i) + border;
			paint.setStrokeWidth(3);
			canvas.drawLine(horstart, y, width, y, paint);
			paint.setColor(getResources().getColor(R.color.green_button_color));
			paint.setTextSize(16f);
			paint.setTypeface(Util.roboto_bold);
			canvas.drawText(verlabels[i], 10, y, paint);
		}
		int hors = horlabels.length;
		for (int i = 0; i < horlabels.length; i++) {
			paint.setColor(getResources().getColor(R.color.screen_bg_color));
			float x = ((graphwidth / hors) * i) + horstart;
			paint.setStrokeWidth(3);
			
			if(i == 0)
			canvas.drawLine(x, height - border, x, border, paint);
			paint.setTextAlign(Align.CENTER);
			  paint.setTextSize(16);
				paint.setColor(Color.BLACK);
				paint.setStyle(Style.FILL);
			if (i==horlabels.length-1){
				paint.setTextAlign(Align.RIGHT);
				//canvas.drawText(horlabels[i], x, height - 4, paint);
			}if (i==0){
				paint.setTextAlign(Align.LEFT);
				//canvas.drawText(horlabels[i], x+10, height - 4, paint);
			}
			  
			
		}

		paint.setTextAlign(Align.CENTER);
		//canvas.drawText(title, (graphwidth / 2) + horstart, border - 4, paint);

		if (max != min) {
			
			//Log.i(diff+"----MIN VALUE----",":"+min);
			
			if (type == BAR) {
				float datalength = values.length;
				float colwidth = (width - (2 * border)) / datalength;
				for (int i = 0; i < values.length; i++) {
					
					paint.setColor(bar_colors[i]);
					float val = values[i] - min;
					//Log.i(barValue[i]+"----BAR CHART VALUE----",":"+values[i]);
					float rat = val / diff;
					float h = graphheight * rat;
					canvas.drawRect((i * colwidth) + horstart+3, (border - h) + graphheight, ((i * colwidth) + horstart) + (colwidth - 1), height - (border - 1), paint);
				
					paint.setColor(Color.BLACK);
					
					paint.setTextSize(18f);
					//paint.setTypeface(Util.roboto_regular);
					paint.setTextAlign(Align.CENTER);
					paint.setTypeface(Util.roboto_regular);
					canvas.drawText(barValue[i],(i * colwidth) + horstart+30, (border - h) + graphheight, paint);
				}
			} else {
				float datalength = values.length;
				float colwidth = (width - (2 * border)) / datalength;
				float halfcol = colwidth / 2;
				float lasth = 0;
				for (int i = 0; i < values.length; i++) {
					float val = values[i] - min;
					float rat = val / diff; 
					float h = graphheight * rat;
					if (i > 0)
						canvas.drawLine(((i - 1) * colwidth) + (horstart + 1) + halfcol, (border - lasth) + graphheight, 
						(i * colwidth) + (horstart + 1) + halfcol, (border - h) + graphheight, paint);
					lasth = h;
				}
			}
			
		}
	}
	private int determineMaxTextSize(String str, float maxWidth)
	{
	    int size = 0;       
	    Paint paint = new Paint();

	    do {
	        paint.setTextSize(++ size);
	    } while(paint.measureText(str) < maxWidth);

	    return size;
	}
	private float getMax() {
		float largest = 0;
		for (int i = 0; i < values.length; i++)
			if (values[i] > largest)
				largest = values[i];
		return 100;
	}

	private float getMin() {
		float smallest =100;
		for (int i = 0; i < values.length; i++)
			if (values[i] < smallest)
				smallest = values[i];
		return 0;
	}

}
