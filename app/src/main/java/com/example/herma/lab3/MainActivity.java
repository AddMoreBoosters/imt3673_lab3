package com.example.herma.lab3;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener2;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Display;
import android.view.View;

public class MainActivity extends AppCompatActivity implements SensorEventListener2 {

    private float xPos, xAccel, xVel = 0.0f;
    private float yPos, yAccel, yVel = 0.0f;
    private float xMax, yMax;
    private Bitmap ball;
    private SensorManager sensorManager;
    private Vibrator vibrator;
    private ToneGenerator tg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        BallView ballView = new BallView(this);
        setContentView(ballView);

        Point size = new Point();
        Display display = getWindowManager().getDefaultDisplay();
        display.getSize(size);
        xMax = (float) size.x - 100;
        yMax = (float) size.y - 235;
        xPos = xMax / 2.0f;
        yPos = yMax / 2.0f;
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        tg = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
    }

    @Override
    protected void onStart() {
        //  Register the listener
        super.onStart();
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onStop() {
        //  Unregister the listener before freeing up space
        sensorManager.unregisterListener(this);
        super.onStop();
    }

    @Override
    public void onFlushCompleted(Sensor sensor) {
        //  Do nothing
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        //  If the sensor changed was the rotation vector, retrieve data from it
        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
            xAccel = -sensorEvent.values[1];
            yAccel = -sensorEvent.values[0];
            updateBall();
        }
    }

    private void updateBall() {
        final float velocityRetained = 0.7f;    //  How much velocity is kept when hitting walls
        float frameTime = 0.666f;
        xVel += (xAccel * frameTime);
        yVel += (yAccel * frameTime);

        float xS = (xVel / 2) * frameTime;
        float yS = (yVel / 2) * frameTime;

        xPos -= xS;
        yPos -= yS;

        //  Bounce on walls, losing a little speed
        if ((xPos > xMax && xVel < 0) || (xPos < 0 && xVel > 0)) {
            xVel *= -velocityRetained;
            giveFeedback();
        }

        if ((yPos > yMax && yVel < 0) || (yPos < 0 && yVel > 0)) {
            yVel *= -velocityRetained;
            giveFeedback();
        }
    }

    private void giveFeedback() {
        vibrator.vibrate(200);
        tg.startTone(ToneGenerator.TONE_PROP_BEEP);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        //  Do nothing
    }

    private class BallView extends View {

        public BallView(Context context) {
            super(context);
            Bitmap ballSrc = BitmapFactory.decodeResource(getResources(), R.drawable.ball);
            final int dstWidth = 100;
            final int dstHeight = 100;
            ball = Bitmap.createScaledBitmap(ballSrc, dstWidth, dstHeight, true);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            canvas.drawBitmap(ball, xPos, yPos, null);
            invalidate();
        }
    }
}
