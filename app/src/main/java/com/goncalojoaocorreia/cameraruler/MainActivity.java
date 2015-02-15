package com.goncalojoaocorreia.cameraruler;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


public class MainActivity extends ActionBarActivity {

    private DrawView drawView;
    private FrameLayout preview;
    private Button btn_ok;
    private Button btn_cancel;
    private Button btn_takePicture;
    private EditText scaleTxt;
    private File photoFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        preview = (FrameLayout) findViewById(R.id.camera_preview);

        btn_takePicture = (Button) findViewById(R.id.button_takePicture);
        btn_ok = (Button) findViewById(R.id.button_calculate);
        btn_cancel = (Button) findViewById(R.id.button_cancel);
        scaleTxt = (EditText) findViewById(R.id.txt_scale);

        addPictureButton();

        btn_takePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });

        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    double scale = Float.parseFloat(scaleTxt.getText().toString());
                    double res = drawView.calculate(scale);
                    DecimalFormat df = new DecimalFormat("#.00");
                    String result = df.format(res);
                    //Present result to user
                    ((TextView) findViewById(R.id.info_lbl)).setText(getResources().getString(R.string.result_lbl) + result);
                }catch(NumberFormatException ex){
                    Toast.makeText(MainActivity.this,
                            getResources().getString(R.string.error_numberFormat),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawView.clearCanvas();
                preview.removeAllViews();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        if(id == R.id.action_cleanStorage){
            cleanPhotoStorage();
        }
        if(id == R.id.action_choosePhoto){
            dispatchChoosePhotoIntent();
        }

        return super.onOptionsItemSelected(item);
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                Log.e("FileIO", "Error occurred while creating the image File");
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(photoFile));
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private void addPictureButton(){
        preview.removeAllViews();

        scaleTxt.setVisibility(View.GONE);
        btn_cancel.setVisibility(View.GONE);
        btn_ok.setVisibility(View.GONE);
        btn_takePicture.setVisibility(View.VISIBLE);
    }

    private void pictureTaken(){
        scaleTxt.setVisibility(View.VISIBLE);
        btn_cancel.setVisibility(View.VISIBLE);
        btn_ok.setVisibility(View.VISIBLE);
        btn_takePicture.setVisibility(View.GONE);

        ImageSurface image = new ImageSurface(this, photoFile);
        preview.addView(image);

        ((TextView) findViewById(R.id.info_lbl)).setText(getResources().getString(R.string.setReferencePoints));

        drawView = new DrawView(this);
        preview.addView(drawView);
    }

    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_SELECT_PHOTO = 2;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode){
            case REQUEST_IMAGE_CAPTURE:
                if(resultCode == RESULT_OK)
                        pictureTaken();
            case REQUEST_SELECT_PHOTO:
                if(resultCode == RESULT_OK){
                    Uri _uri = data.getData();
                    //User had pick an image.
                    Cursor cursor = getContentResolver().query(_uri, new String[] { android.provider.MediaStore.Images.ImageColumns.DATA }, null, null, null);
                    cursor.moveToFirst();

                    //Link to the image
                    final String filePath = cursor.getString(0);
                    cursor.close();
                    photoFile = new File(filePath);
                    pictureTaken();
                }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(null);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        return image;
    }

    private void cleanPhotoStorage(){
        File storageDir = getExternalFilesDir(null);
        File fList[] = storageDir.listFiles();
        //Search for pictures in the directory
        for(int i = 0; i < fList.length; i++){
            String pes = fList[i].getName();
            if(pes.endsWith(".jpg"))
                new File(fList[i].getAbsolutePath()).delete();
        }
    }

    private void dispatchChoosePhotoIntent(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, getResources().getString(R.string.action_choosePhoto)), REQUEST_SELECT_PHOTO);
    }
}