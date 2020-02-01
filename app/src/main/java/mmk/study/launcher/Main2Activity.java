package mmk.study.launcher;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import mmk.study.launcher.activities.ContactsActivity;

public class Main2Activity extends AppCompatActivity  {

    private TextView dateTxt,timeTxt;
    private LinearLayout callLayout,messageLayout,cameraLayout,galleryLayout,flashLayout,whatsappLayout,contactsLayout;
    private ImageView flashLightImage;
    private boolean isFlashOn=false;
    private CameraManager mCameraManager;
    private String mCameraId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);


        initView();
        setClicks();
        setupFlashLight();

        getTimeDate();
    }

    private void getTimeDate() {



       final Handler handler=new Handler();

       handler.post(new Runnable() {
            @Override
            public void run() {
                String currentTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
                timeTxt.setText(currentTime);
                String currentDate = new SimpleDateFormat("d MMM yyy", Locale.getDefault()).format(new Date());
                dateTxt.setText(currentDate);
                handler.post(this);

            }
        });



    }

    @Override
    public void onBackPressed() {

    }

    private void setClicks() {


        flashLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                boolean hasFlash = getApplicationContext().getPackageManager()
                        .hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);


                if (!hasFlash) {
                    noFlashErrorMessage();
                    return;
                }

                if (!isFlashOn){
                    //turn flash
                    flashLightImage.setBackgroundResource(R.drawable.torch_on);
                    flashLayout.setBackgroundColor( getResources().getColor( android.R.color.holo_orange_light));
                    isFlashOn=true;
                    switchFlashLight(isFlashOn);
                }

                else{
                    flashLayout.setBackgroundColor( getResources().getColor( android.R.color.transparent));
                    flashLightImage.setBackgroundResource(R.drawable.torch_off);
                    isFlashOn=false;
                    switchFlashLight(isFlashOn);
                }
            }
        });

        cameraLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA);
                startActivity(intent);

            }
        });
        galleryLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(Intent.ACTION_VIEW, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);


                startActivity(intent);

            }
        });
        whatsappLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent launchAppIntent = getApplicationContext().getPackageManager().getLaunchIntentForPackage("com.whatsapp");
                if (launchAppIntent != null) {
                    getApplicationContext().startActivity(launchAppIntent);
                }
                else{
                    Toast.makeText(Main2Activity.this, "There is no such app in your phone", Toast.LENGTH_SHORT).show();
                }
            }
        });
        callLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(android.content.Intent.ACTION_VIEW);
                intent.setAction(Intent.ACTION_DIAL);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
        messageLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.setType("vnd.android-dir/mms-sms");
                startActivity(intent);
            }
        });

        contactsLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(Main2Activity.this,ContactsActivity.class);
                startActivity(intent);
            }
        });
    }

    private void noFlashErrorMessage() {

        // device doesn't support flash
        // Show alert message and close the application
        AlertDialog alert = new AlertDialog.Builder(Main2Activity.this).create();
        alert.setTitle("Error");
        alert.setMessage("Sorry, your device doesn't support flash light!");
        alert.setButton(DialogInterface.BUTTON_NEUTRAL,"OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // closing the application
                finish();
            }
        });
        alert.show();
    }

    private void setupFlashLight() {

        mCameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            mCameraId = mCameraManager.getCameraIdList()[0];
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public void switchFlashLight(boolean status) {
        try {
            mCameraManager.setTorchMode(mCameraId, status);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }




    private void initView() {

        dateTxt=findViewById(R.id.dateTxt);
        timeTxt=findViewById(R.id.timeTxt);

        callLayout=findViewById(R.id.item1);
        messageLayout=findViewById(R.id.item2);
        cameraLayout=findViewById(R.id.item3);
        galleryLayout=findViewById(R.id.item4);
        whatsappLayout=findViewById(R.id.item5);
        flashLayout=findViewById(R.id.item6);
        contactsLayout=findViewById(R.id.item7);
        flashLightImage=findViewById(R.id.torchImageView);
        flashLightImage.setBackgroundResource(R.drawable.torch_off);
    }

    public void openOtherApp(String packageName){
        Intent launchAppIntent = getApplicationContext().getPackageManager().getLaunchIntentForPackage(packageName);
        if (launchAppIntent != null) {
            getApplicationContext().startActivity(launchAppIntent);
        }
        else{
            Toast.makeText(this, "There is no such app in your phone", Toast.LENGTH_SHORT).show();
        }
    }


}
