package me.michaelnelson.particles;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.TextView;

import org.w3c.dom.Text;

import me.michaelnelson.particles.R;


/**
 * Activity class for example program that detects OpenGL ES 2.0.
 **/
public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mGLSurfaceView = new GLSurfaceView(this);
        mGLSurfaceView = (GLSurfaceView)findViewById(R.id.glSurfaceView1);
        if (detectOpenGLES30())
        {
            // Tell the surface view we want to create an OpenGL ES 2.0-compatible
            // context, and set an OpenGL ES 2.0-compatible renderer.
            mGLSurfaceView.setEGLContextClientVersion(3);
            mRenderer = new ParticleSystemRenderer(this);
            mGLSurfaceView.setRenderer(mRenderer);


        }
        else
        {
            Log.e("ParticleSystem", "OpenGL ES 3.0 not supported on device.  Exiting...");
            finish();

        }

        CubeAmout();
        RandomAmount();
    }

    public void CubeAmout(){
        cube_amount = (SeekBar)findViewById(R.id.CubeseekBarID);
        cube_text = (TextView)findViewById(R.id.cubeText);



        cube_amount.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener(){
                    int cubes;
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser){
                        cubes = progress;
                        mRenderer.setCUR_PARTICLES(progress +10);
                        cube_text.setText("Cubes: " + String.valueOf(progress +10));

                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar){

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar){

                    }
                }
        );
    }

    public void RandomAmount(){
        random_amount = (SeekBar)findViewById(R.id.RandomseekBarID);
        random_text = (TextView)findViewById(R.id.randomText);


        random_amount.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener(){
                    int random;
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser){
                        if(progress<1){
                            random = 0;
                        }
                        else {
                            random = progress+1;
                        }
                        mRenderer.setRANDOM_FACTOR(progress+1);
                        random_text.setText("Randomness: " + String.valueOf((float)(random) /25 *100) +"%");
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar){

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar){

                    }
                }
        );
    }


    private boolean detectOpenGLES30()
    {
        ActivityManager am =
                (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        ConfigurationInfo info = am.getDeviceConfigurationInfo();
        return (info.reqGlEsVersion >= 0x30000);
    }

    @Override
    protected void onResume()
    {
        // Ideally a game should implement onResume() and onPause()
        // to take appropriate action when the activity looses focus
        super.onResume();
        mGLSurfaceView.onResume();
    }

    @Override
    protected void onPause()
    {
        // Ideally a game should implement onResume() and onPause()
        // to take appropriate action when the activity looses focus
        super.onPause();
        mGLSurfaceView.onPause();
    }

    private GLSurfaceView mGLSurfaceView;
    private ParticleSystemRenderer mRenderer;

    private  static SeekBar cube_amount;
    private  static SeekBar random_amount;
    private  static TextView cube_text;
    private  static TextView    random_text;
}
