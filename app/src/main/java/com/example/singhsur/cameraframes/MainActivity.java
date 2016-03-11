package com.example.singhsur.cameraframes;

import android.app.Activity;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.ErrorCallback;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import test.cameratest.R;


public class MainActivity extends Activity  {

    SurfaceView mVideoCaptureView;
    Camera mCamera;
    Bitmap bitimage;
    ImageView imgview;
    int count=1;
    float smilingProbability = 0;
    float leftEyeOpenProbability = 2;
    float rightEyeOpenProbability = 2;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button button=(Button)findViewById(R.id.CameraStop);

          imgview=(ImageView) findViewById(R.id.imageView);
      /*  button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
             ContextWrapper cw = new ContextWrapper(getApplicationContext());
                    File directory = Environment.getExternalStorageDirectory();
                    String imgPath=directory.getAbsolutePath();
                    loadImageFromStorage(imgPath);
                Log.e("count",count+"");
            }
        });*/
        mVideoCaptureView = (SurfaceView) findViewById(R.id.surfaceview);
        SurfaceHolder videoCaptureViewHolder = mVideoCaptureView.getHolder();
        videoCaptureViewHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        videoCaptureViewHolder.addCallback(new Callback() {
            public void surfaceDestroyed(SurfaceHolder holder) {
                finish();

            }

            public void surfaceCreated(SurfaceHolder holder) {
                startVideo();
            }

            public void surfaceChanged(SurfaceHolder holder, int format,
                                       int width, int height) {
            }
        });
    }
    private void startVideo() {
        SurfaceHolder videoCaptureViewHolder = null;

        try {
           mCamera=Camera.open();
        } catch (RuntimeException e) {
            Log.e("CameraTest", "Camera Open failed");
            return;
        }
        mCamera.setErrorCallback(new ErrorCallback() {
            public void onError(int error, Camera camera) {
            }
        });
        Camera.Parameters parameters = mCamera.getParameters();
        parameters.setPreviewFrameRate(30);
        parameters.setPreviewFpsRange(15000, 30000);
        List<int[]> supportedPreviewFps = parameters.getSupportedPreviewFpsRange();
        Iterator<int[]> supportedPreviewFpsIterator = supportedPreviewFps.iterator();
        while (supportedPreviewFpsIterator.hasNext()) {
            int[] tmpRate = supportedPreviewFpsIterator.next();
            StringBuffer sb = new StringBuffer();
            sb.append("supportedPreviewRate: ");
            for (int i = tmpRate.length, j = 0; j < i; j++) {
                sb.append(tmpRate[j] + ", ");
            }
            Log.v("CameraTest", sb.toString());
        }

        List<Size> supportedPreviewSizes = parameters.getSupportedPreviewSizes();
        Iterator<Size> supportedPreviewSizesIterator = supportedPreviewSizes.iterator();
        while (supportedPreviewSizesIterator.hasNext()) {
            Size tmpSize = supportedPreviewSizesIterator.next();
            Log.v("CameraTest", "supportedPreviewSize.width = " + tmpSize.width + "supportedPreviewSize.height = " + tmpSize.height);
        }

        mCamera.setParameters(parameters);
        if (null != mVideoCaptureView)
            videoCaptureViewHolder = mVideoCaptureView.getHolder();
        try {
            mCamera.setPreviewDisplay(videoCaptureViewHolder);
        } catch (Throwable t) {
        }
        Log.v("CameraTest", "Camera PreviewFrameRate = " + mCamera.getParameters().getPreviewFrameRate());
        Size previewSize = mCamera.getParameters().getPreviewSize();
        int dataBufferSize = (int) (previewSize.height * previewSize.width *
                (ImageFormat.getBitsPerPixel(mCamera.getParameters().getPreviewFormat()) / 8.0));
        for (int i = 0; i <=20; i++)
            mCamera.addCallbackBuffer(new byte[dataBufferSize]);
        //mCamera.addCallbackBuffer(new byte[dataBufferSize]);
        //mCamera.addCallbackBuffer(new byte[dataBufferSize]);
        mCamera.setPreviewCallbackWithBuffer(new Camera.PreviewCallback() {
            private long timestamp = 0;

            /*public synchronized void onPreviewFrame(byte[] data, Camera camera) {
                Log.v("CameraTest","Time Gap = "+(System.currentTimeMillis()-timestamp));
                timestamp=System.currentTimeMillis();
                int width=200,height=200;

                try{
                    YuvImage yuvImage = new YuvImage(data, ImageFormat.NV21,width, height, null);
                    Rect rectangle = new Rect(0, 0,width, height);
                    ByteArrayOutputStream out = new ByteArrayOutputStream();

                     yuvImage.compressToJpeg(rectangle, 100, out);
                    byte[] imageBytes = out.toByteArray();
                     bitimage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                    imgview.setImageBitmap(bitimage);

                   // saveToInternalStorage(bitimage);
                    /*ContextWrapper cw = new ContextWrapper(getApplicationContext());
                    File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
                    String imgPath=directory.getAbsolutePath();
                    loadImageFromStorage(imgPath);



                    //camera.addCallbackBuffer(data);
                }catch (Exception e) {
                    Log.e("CameraTest", "addCallbackBuffer error");
                    return;
                }
                return;
            }
        });
        try {
            mCamera.startPreview();
        } catch (Throwable e) {
            mCamera.release();
            mCamera = null;
            return;
        }
    }*/
            @Override
            public synchronized void onPreviewFrame(byte[] data, Camera camera) {
                Log.v("CameraTest","Time Gap = "+(System.currentTimeMillis()-timestamp));
                timestamp=System.currentTimeMillis();
                try {

                    Camera.Parameters parameters = camera.getParameters();
                    Size size = parameters.getPreviewSize();
                    YuvImage image = new YuvImage(data, parameters.getPreviewFormat(),
                            size.width, size.height, null);
                    File file = new File(Environment.getExternalStorageDirectory(), "out.jpg");
                    count++;
                    FileOutputStream filecon = new FileOutputStream(file);
                    image.compressToJpeg(
                            new Rect(0, 0, image.getWidth(), image.getHeight()), 90,
                            filecon);
                    Bitmap b = BitmapFactory.decodeStream(new FileInputStream(file));

                   ImageView img = (ImageView) findViewById(R.id.imageView);
                    img.setImageBitmap(b);


                   // ContextWrapper cw = new ContextWrapper(getApplicationContext());
                    File directory = Environment.getExternalStorageDirectory();
                    String imgPath=directory.getAbsolutePath();
                    loadImageFromStorage(imgPath);

                    if(smilingProbability<0){
                        Toast.makeText(MainActivity.this, "smiling", Toast.LENGTH_SHORT).show();
                    }
                    if(leftEyeOpenProbability==1 && rightEyeOpenProbability==1 ){
                        Toast.makeText(MainActivity.this, "eyes open", Toast.LENGTH_SHORT).show();
                    }
                    smilingProbability=0;
                    leftEyeOpenProbability=2;
                    rightEyeOpenProbability=2;



                } catch (FileNotFoundException e) {
                    Toast toast = Toast
                            .makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_LONG);
                    toast.show();
                }
            }
        });

                try {
                    mCamera.startPreview();
                } catch (Throwable e) {
                    mCamera.release();
                    mCamera = null;
                    return;
                }
            }


            private void stopVideo() {
                if (null == mCamera)
                    return;
                try {
                    mCamera.stopPreview();
                    mCamera.setPreviewDisplay(null);
                    mCamera.setPreviewCallbackWithBuffer(null);
                    mCamera.release();
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
                mCamera = null;
            }

    public void finish(){
        stopVideo();
        super.finish();
    };

           /* private synchronized void saveToInternalStorage(Bitmap bitmapImage) throws IOException {
                ContextWrapper cw = new ContextWrapper(getApplicationContext());
                // path to /data/data/yourapp/app_data/imageDir
                File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
                // Create imageDir
                File mypath = new File(directory, "profile.jpg");

                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(mypath);
                    // Use the compress method on the BitMap object to write image to the OutputStream
                    bitmapImage.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                    count++;

                    Toast.makeText(MainActivity.this, "saved", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    e.printStackTrace();
                } //finally {
                // fos.close();
                //}
                //return directory.getAbsolutePath();
            }*/

            private void loadImageFromStorage(String path) {

                try {
                        File f = new File(path, "out.jpg");
                    Bitmap myBitmap = BitmapFactory.decodeStream(new FileInputStream(f));
                    //ImageView img = (ImageView) findViewById(R.id.imageView);
                    //img.setImageBitmap(b);

                    //integration
                    Paint myRectPaint = new Paint();
                    myRectPaint.setStrokeWidth(5);
                    myRectPaint.setColor(Color.RED);
                    myRectPaint.setStyle(Paint.Style.STROKE);

                    Bitmap tempBitmap = Bitmap.createBitmap(myBitmap.getWidth(), myBitmap.getHeight(), Bitmap.Config.RGB_565);
                    Canvas tempCanvas = new Canvas(tempBitmap);
                    tempCanvas.drawBitmap(myBitmap, 0, 0, null);

                    FaceDetector faceDetector = new
                            FaceDetector.Builder(getApplicationContext()).setTrackingEnabled(false)
                            .build();
     /*   if (!faceDetector.isOperational()) {
            new AlertDialog.Builder(v.getContext()).setMessage("Could not set up the face detector!").show();
            return;
        }*/

                    Frame frame = new Frame.Builder().setBitmap(myBitmap).build();
                    SparseArray<Face> faces = faceDetector.detect(frame);

                    for (int i = 0; i < faces.size(); i++) {
                        Face thisFace = faces.valueAt(i);
                        float x1 = thisFace.getPosition().x;
                        float y1 = thisFace.getPosition().y;
                        float x2 = x1 + thisFace.getWidth();
                        float y2 = y1 + thisFace.getHeight();
                        tempCanvas.drawRoundRect(new RectF(x1, y1, x2, y2), 2, 2, myRectPaint);
                    }
                    //  myImageView.setImageDrawable(new BitmapDrawable(getResources(), tempBitmap));


                    // private void logFaceData() {

                    float eulerY = 0;
                    float eulerZ = 0;
                    String smiling, lefteyeopen, righteyeopen;
                    for (int i = 0; i < faces.size(); i++) {
                        Face face = faces.valueAt(i);

                        smilingProbability = face.getIsSmilingProbability();
                        leftEyeOpenProbability = face.getIsLeftEyeOpenProbability();
                        rightEyeOpenProbability = face.getIsRightEyeOpenProbability();
                        eulerY = face.getEulerY();
                        eulerZ = face.getEulerZ();
                        // int so=0,sc=0,lo=0,lc=0,ro=0,rc=0;
                      /*  int c = 0, sf = 0, ef = 0;
                        if (smilingProbability > 0 && sf == 0) {
                            c++;
                            sf = 1;
                        }
                        if (leftEyeOpenProbability < 0 && rightEyeOpenProbability < 0 && ef == 0) {
                            c++;
                            ef = 1;
                        }
                        int failed=0;
                        failed++;
                        if (c == 2) {
                            Toast.makeText(MainActivity.this, "success", Toast.LENGTH_LONG).show();
                            break;
                        }

                        else{
                            Log.i("failed",failed+"");
                        }*/
                        if(smilingProbability<0){
                            Toast.makeText(MainActivity.this, "smiling", Toast.LENGTH_SHORT).show();
                        }
                        if(leftEyeOpenProbability==1 && rightEyeOpenProbability==1 ){
                            Toast.makeText(MainActivity.this, "eyes open", Toast.LENGTH_SHORT).show();
                        }



                   /* if(smilingProbability<0){
                        smiling="No";
                    }
                    else{
                        smiling="Yes";
                    }
                    if(leftEyeOpenProbability<0){
                        lefteyeopen="No";
                    }
                    else{
                        lefteyeopen="Yes";
                    }
                    if(rightEyeOpenProbability<0){
                        righteyeopen="No";
                    }
                    else{
                        righteyeopen="Yes";
                    }
                    AlertDialog.Builder myAlert= new AlertDialog.Builder(context);
                    myAlert.setTitle("ImageDetails");
                    myAlert.setMessage("Smiling " + smiling + "\nlefteye " + lefteyeopen+"\nRighteye "+righteyeopen+"\nEulerY,Z "+Float.toString(eulerY)+" , "+Float.toString(eulerZ));
                    AlertDialog alertDialog = myAlert.create();

                    // show it
                    alertDialog.show();*/



                       /* try {
                            textView.setText("smiling"+Float.toString(smilingProbability)+
                                                "lefteye"+Float.toString(leftEyeOpenProbability)+
                                                    "righteye"+Float.toString(rightEyeOpenProbability)+
                                                    "euler y,z"+Float.toString(eulerY)+" , "+Float.toString(eulerZ));
                        }
                        catch (Exception e){
                            Log.e("error",e+"");
                        }*/
                        Log.e("Tuts+ Face Detection", "Smiling: " + smilingProbability);
                        Log.e("Tuts+ Face Detection", "Left eye open: " + leftEyeOpenProbability);
                        Log.e("Tuts+ Face Detection", "Right eye open: " + rightEyeOpenProbability);
                        Log.e("Tuts+ Face Detection", "Euler Y: " + eulerY);
                        Log.e("Tuts+ Face Detection", "Euler Z: " + eulerZ);


                    }



                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

            }
            }