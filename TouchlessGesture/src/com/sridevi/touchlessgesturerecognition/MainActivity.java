package com.sridevi.touchlessgesturerecognition;

import java.util.Iterator;
import java.util.List;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;
import org.opencv.imgproc.Imgproc;

import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;

public class MainActivity extends Activity implements CvCameraViewListener2{

    protected static final String TAG = "TouchlessGestureRecognition::MainActivity";
  	private CameraBridgeViewBase  mOpenCvCameraView;
    private Mat mRgba;
    private Scalar mSkinColorHsv = new Scalar(0);
    private HandDetector mDetector;
    private GestureAnalyzer mGestureAnalyzer;
    private Scalar CONTOUR_COLOR;
    private double xStart, xEnd;
    private boolean gestureRecording = false;
    
    private BaseLoaderCallback  mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    // Load native library after(!) OpenCV initialization
                    System.loadLibrary("TouchlessGestureRecognition");
                    mOpenCvCameraView.enableView();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_main);

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.JavaCameraView01);
        mOpenCvCameraView.setCvCameraViewListener(this);
        
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main, menu); 
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
         
        switch (item.getItemId())
        {
        case R.id.menu_hand:
        	 mSkinColorHsv.val[0] = 24;
             mSkinColorHsv.val[1] = 76;
             mSkinColorHsv.val[2] = 242;
             mSkinColorHsv.val[3] = 0; 
             mDetector.setHsvColor(mSkinColorHsv); 
             return true;
        case R.id.menu_red:
        	mDetector.setRangeBounds(new Scalar(0, 100, 30), new Scalar(5, 255, 255));
            return true;        
        default:
            return super.onOptionsItemSelected(item);
        }
    }
    
    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }
    
    public void onCameraViewStarted(int width, int height) {
    	 mRgba = new Mat(height, width, CvType.CV_8UC4);
    	 mDetector = new HandDetector();
    	 mGestureAnalyzer = new GestureAnalyzer();
    	 CONTOUR_COLOR = new Scalar(255,0,0,255);
    	 mSkinColorHsv.val[0] = 24;
         mSkinColorHsv.val[1] = 76;
         mSkinColorHsv.val[2] = 242;
         mSkinColorHsv.val[3] = 0; 
         mDetector.setHsvColor(mSkinColorHsv); 
         //mDetector.setRangeBounds(new Scalar(0, 100, 30), new Scalar(5, 255, 255));
         mDetector.setMinContourArea(750);
    }

    public void onCameraViewStopped() {
    	  mRgba.release();
    }
    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
    	  mRgba = inputFrame.rgba();
    	  Mat mRgba2 = mDetector.process(mRgba);
    	  mRgba2.release();
          List<MatOfPoint> contours = mDetector.getContours();   
          Iterator<MatOfPoint> each = contours.iterator();
          Rect r = new Rect();
          while (each.hasNext()) {
        	  MatOfPoint c = each.next();
        	  r = Imgproc.boundingRect((MatOfPoint) c);
        	  Core.rectangle(mRgba, r.tl(), r.br(), new Scalar(0,255,0,255),3);
        	  Core.circle(mRgba, r.br(), 20, new Scalar(0,0,200,255),3);
        	  if(gestureRecording == false) {
        		  xStart = r.br().x;
        		  gestureRecording = true;
        	  }
        	  xEnd = r.br().x;  
          }
          Imgproc.drawContours(mRgba, contours, -1, CONTOUR_COLOR,3);
          displayAction();
          //Core.flip(mRgba, mRgba, 1);
          return mRgba;
    }
    public void displayAction(){
    	int i = mGestureAnalyzer.compute(xStart, xEnd);	
    	if( i > 0 ) gestureRecording = false;
    }
    protected void processFrame(VideoCapture capture) {
        capture.retrieve(mRgba, Highgui.CV_CAP_ANDROID_COLOR_FRAME_RGBA);
    }
    public void ToggleVideo(View view) {
    	 if(mOpenCvCameraView.getVisibility() == SurfaceView.VISIBLE){
             mOpenCvCameraView.setVisibility(SurfaceView.INVISIBLE);
         }else{
             mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
         }
    }
    
    public native void DrawCircle(long matAddrGr, long matAddrRgba);
}
