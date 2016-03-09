package com.example.singhsur.cameraframes;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import android.app.Activity;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Camera.ErrorCallback;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.SurfaceHolder.Callback;
import android.view.View;
import android.widget.Button;

import test.cameratest.R;

public class MainActivity extends Activity  {
    SurfaceView mVideoCaptureView;
    Camera mCamera;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button button=(Button)findViewById(R.id.CameraStop);
        button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                finish();

            }
        });
        mVideoCaptureView = (SurfaceView) findViewById(R.id.surfaceview);
        SurfaceHolder videoCaptureViewHolder = mVideoCaptureView.getHolder();
        videoCaptureViewHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        videoCaptureViewHolder.addCallback(new Callback() {
            public void surfaceDestroyed(SurfaceHolder holder) {
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
            mCamera = Camera.open();
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
        parameters.setPreviewFpsRange(15000,30000);
        List<int[]> supportedPreviewFps=parameters.getSupportedPreviewFpsRange();
        Iterator<int[]> supportedPreviewFpsIterator=supportedPreviewFps.iterator();
        while(supportedPreviewFpsIterator.hasNext()){
            int[] tmpRate=supportedPreviewFpsIterator.next();
            StringBuffer sb=new StringBuffer();
            sb.append("supportedPreviewRate: ");
            for(int i=tmpRate.length,j=0;j<i;j++){
                sb.append(tmpRate[j]+", ");
            }
            Log.v("CameraTest",sb.toString());
        }

        List<Size> supportedPreviewSizes=parameters.getSupportedPreviewSizes();
        Iterator<Size> supportedPreviewSizesIterator=supportedPreviewSizes.iterator();
        while(supportedPreviewSizesIterator.hasNext()){
            Size tmpSize=supportedPreviewSizesIterator.next();
            Log.v("CameraTest","supportedPreviewSize.width = "+tmpSize.width+"supportedPreviewSize.height = "+tmpSize.height);
        }

        mCamera.setParameters(parameters);
        if (null != mVideoCaptureView)
            videoCaptureViewHolder = mVideoCaptureView.getHolder();
        try {
            mCamera.setPreviewDisplay(videoCaptureViewHolder);
        } catch (Throwable t) {
        }
        Log.v("CameraTest","Camera PreviewFrameRate = "+mCamera.getParameters().getPreviewFrameRate());
        Size previewSize=mCamera.getParameters().getPreviewSize();
        int dataBufferSize=(int)(previewSize.height*previewSize.width*
                (ImageFormat.getBitsPerPixel(mCamera.getParameters().getPreviewFormat())/8.0));
        mCamera.addCallbackBuffer(new byte[dataBufferSize]);
        mCamera.addCallbackBuffer(new byte[dataBufferSize]);
        mCamera.addCallbackBuffer(new byte[dataBufferSize]);
        mCamera.setPreviewCallbackWithBuffer(new Camera.PreviewCallback() {
            private long timestamp=0;
            public synchronized void onPreviewFrame(byte[] data, Camera camera) {
                Log.v("CameraTest","Time Gap = "+(System.currentTimeMillis()-timestamp));
                timestamp=System.currentTimeMillis();

                try{
                    camera.addCallbackBuffer(data);
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
    }
    private void stopVideo() {
        if(null==mCamera)
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
}
