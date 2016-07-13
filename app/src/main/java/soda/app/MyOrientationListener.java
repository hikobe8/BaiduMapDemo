package soda.app;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;


/**
 * Created by soda on 2016/7/12.
 */
public class MyOrientationListener implements SensorEventListener {

    private Context mContext;
    private SensorManager mSensorManager;
    private Sensor mSensor;
    private float lastX;
    private OnOrientationChangeListener mOnOrientationChangeListener;

    public MyOrientationListener(Context context) {
        mContext = context;
    }

    /**
     * 设置位置变化监听器
     * @param onOrientationChangeListener
     */
    public void setOnOrientationChangeListener(OnOrientationChangeListener onOrientationChangeListener){
        this.mOnOrientationChangeListener = onOrientationChangeListener;
    }

    public void start(){
        //获取传感器管理器
        mSensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
        if (mSensorManager != null) {
            //获得方向传感器
            mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
            if (mSensor != null) {
                mSensorManager.registerListener(this , mSensor, SensorManager.SENSOR_DELAY_UI);
            }
        }
    }

    public void stop() {
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        //接收方向信息
        if (sensorEvent.sensor.getType() == Sensor.TYPE_ORIENTATION){
            float x = sensorEvent.values[SensorManager.DATA_X];
            if (Math.abs(x-lastX) > 1.0){
                if (mOnOrientationChangeListener != null)
                    mOnOrientationChangeListener.onOrientationChange(x);
            }
            lastX = x;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }


    interface OnOrientationChangeListener{
        void onOrientationChange(float x);
    }
}
