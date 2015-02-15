package com.goncalojoaocorreia.cameraruler;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Gonçalo on 13/02/2015.
 */
public class DrawView extends SurfaceView {
    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    List<Point> circlePoints;
    private Context context;

    private static int REFERENCE_POINT_COLOR = Color.YELLOW;
    private static int MEASURE_POINT_COLOR = Color.RED;

    public DrawView(Context context){
        super(context);
        this.context = context;
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.FILL);
        circlePoints = new ArrayList<>();
        setWillNotDraw (false);
        getHolder().setFormat(PixelFormat.TRANSLUCENT);
    }

    @Override
    protected void onDraw(Canvas canvas){
        int size = circlePoints.size();
        for(int i = 0; i < size; i++){
            //Set color based on order. First 2 points are the reference points.
            if(i < 2) paint.setColor(REFERENCE_POINT_COLOR);
            else paint.setColor(MEASURE_POINT_COLOR);
            Point p = circlePoints.get(i);
            canvas.drawCircle(p.x, p.y, 10, paint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event){
        if(event.getAction() == MotionEvent.ACTION_DOWN){
            if(circlePoints.size() < 4) {
                circlePoints.add(new Point(Math.round(event.getX()), Math.round(event.getY())));
                invalidate();
                if(circlePoints.size() == 2){
                    ((TextView) ((Activity)context).findViewById(R.id.info_lbl)).setText(getResources().getString(R.string.setMeasurePoints));
                }
                if(circlePoints.size() == 4){
                    ((TextView) ((Activity)context).findViewById(R.id.info_lbl)).setText(getResources().getString(R.string.setScaleValue));
                }
            }
        }
        return false;
    }

    public void clearCanvas(){
        circlePoints.clear();
        ((TextView) ((Activity)context).findViewById(R.id.info_lbl)).setText(getResources().getString(R.string.setPicture));
        invalidate();
    }

    public double calculate(double scale){
        if(circlePoints.size() != 4){
            Toast.makeText(context, getResources().getString(R.string.error_noPoints), Toast.LENGTH_SHORT);
            return 0;
        }
        return Ruler.compute(circlePoints, scale);
    }


}
