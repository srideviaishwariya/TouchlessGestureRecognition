package com.sridevi.touchlessgesturerecognition;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class HandDetector {
	
	private static final String TAG = "TouchlessGestureRecognition::GestureAnalyzer";
	  // Lower and Upper bounds for range checking in HSV color space
    private Scalar mLowerBound = new Scalar(0);
    private Scalar mUpperBound = new Scalar(0);
    // Minimum contour area in percent for contours filtering
    private static double mMinContourArea = 0.1;
    // Color radius for range checking in HSV color space
    private Scalar mColorRadius = new Scalar(25,50,50,0);
    private Mat mSpectrum = new Mat();
    private List<MatOfPoint> mContours = new ArrayList<MatOfPoint>();
 
    // Cache
    Mat mPyrDownMat = new Mat();
    Mat mHsvMat = new Mat();
    Mat mMask = new Mat();
    Mat mDilatedMask = new Mat();
    Mat mHierarchy = new Mat();
    
    public void setColorRadius(Scalar radius) {
        mColorRadius = radius;
    }

    public void setHsvColor(Scalar hsvColor) {
        double minH = (hsvColor.val[0] >= mColorRadius.val[0]) ? hsvColor.val[0]-mColorRadius.val[0] : 0;
        double maxH = (hsvColor.val[0]+mColorRadius.val[0] <= 255) ? hsvColor.val[0]+mColorRadius.val[0] : 255;

        mLowerBound.val[0] = minH;
        mUpperBound.val[0] = maxH;

        mLowerBound.val[1] = hsvColor.val[1] - mColorRadius.val[1];
        mUpperBound.val[1] = hsvColor.val[1] + mColorRadius.val[1];

        mLowerBound.val[2] = hsvColor.val[2] - mColorRadius.val[2];
        mUpperBound.val[2] = hsvColor.val[2] + mColorRadius.val[2];

        mLowerBound.val[3] = 0;
        mUpperBound.val[3] = 255;

        Mat spectrumHsv = new Mat(1, (int)(maxH-minH), CvType.CV_8UC3);

        for (int j = 0; j < maxH-minH; j++) {
            byte[] tmp = {(byte)(minH+j), (byte)255, (byte)255};
            spectrumHsv.put(0, j, tmp);
        }

        Imgproc.cvtColor(spectrumHsv, mSpectrum, Imgproc.COLOR_HSV2RGB_FULL, 4);
    }
    public Mat getSpectrum() {
        return mSpectrum;
    }

    public void setMinContourArea(double area) {
        mMinContourArea = area;
    }
    public void setRangeBounds(Scalar low, Scalar high){
    	mLowerBound = low;
    	mUpperBound = high;  	
    }
    
    public Mat process(Mat rgbaImage) {
        //Imgproc.pyrDown(rgbaImage, mPyrDownMat);
        //Imgproc.pyrDown(mPyrDownMat, mPyrDownMat);

        Imgproc.cvtColor(rgbaImage, mHsvMat, Imgproc.COLOR_RGB2HSV_FULL);     
        Core.inRange(mHsvMat, mLowerBound, mUpperBound, mMask);   
        Imgproc.dilate(mMask, mDilatedMask, new Mat());
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Imgproc.findContours(mDilatedMask, contours, mHierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        
        // Find max contour area
        double maxArea = 0;
        Iterator<MatOfPoint> each = contours.iterator();
        while (each.hasNext()) {
            MatOfPoint wrapper = each.next();
            double area = Imgproc.contourArea(wrapper);
            if (area > maxArea)
                maxArea = area;
        }     
        // Filter contours by area 
        mContours.clear();
        each = contours.iterator();
        while (each.hasNext()) {
            MatOfPoint contour = each.next();
            if (Imgproc.contourArea(contour) == maxArea & Imgproc.contourArea(contour) >=  mMinContourArea) {
                mContours.add(contour);  
            }
        }
        return mMask;
    }
    
    public List<MatOfPoint> getContours() {
        return mContours;
    }
    public List<MatOfPoint> getBoundingBox(){
    	return mContours; 	
    }
    
	public Mat filtercolor(Mat src){
    	/* Convert a color image from BGR to HSV
    	 * Split the HSV color image into its separate H, S and V components
    	 * Use Threshold() to look for the pixels that are in the correct range of Hue, Saturation and Value (Brightness).
    	*/
    	
		 Mat mRgba = new Mat();
    	 Mat mRgba2 = new Mat();
    	 Mat mHSV = new Mat();
    	 Mat mHSVThreshed = new Mat();
    	 Mat kernel = new Mat();
    	 
    	 // HSV range for skin 
    	 Scalar  hsv_min = new Scalar (19, 60, 240); 
         Scalar  hsv_max = new Scalar (25, 130, 200); 
         
    	 //Convert RGB to HSV
         Imgproc.cvtColor(src, mHSV, Imgproc.COLOR_BGR2HSV,3);
         
         //separate the HSV chanels
         Mat h = new Mat();
         Core.extractChannel(mHSV, h, 0);
         //Core.inRange(h, hsv_min, hsv_max, mHSV);  
         Mat s = new Mat();
         Core.extractChannel(mHSV, s, 1);
         Mat v = new Mat();
         Core.extractChannel(mHSV, v, 2); 
     
        //Apply threshold to the HSV         
         Core.inRange(mHSV, hsv_min, hsv_max, mHSVThreshed); // for skin color
         mHSV.release();    
         Imgproc.cvtColor(mHSVThreshed, mRgba2, Imgproc.COLOR_GRAY2BGR, 0);
         mHSVThreshed.release();
         Imgproc.cvtColor(mRgba2, mRgba, Imgproc.COLOR_BGR2RGBA, 0);
         mRgba2.release();
         kernel = Imgproc.getStructuringElement(Imgproc.MORPH_DILATE, new Size(3,3), new Point(1,1));
         Imgproc.dilate(mRgba, mRgba, kernel);
         
 /*        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
     	 Mat hierarchy = new Mat();
      	 Scalar wh = new Scalar(255, 255, 255, 255);
      	 
     	Imgproc.cvtColor(mRgba, mRgba, Imgproc.COLOR_RGBA2GRAY);   
      	mRgba.convertTo(mRgba, CvType.CV_8UC1);
        Imgproc.findContours(mRgba, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        Imgproc.drawContours(mRgba, contours, 10, wh);
   */      
         
         //Detect the contours
 /*        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
     	 Mat hierarchy = new Mat();
     	 double maxArea = 0;
     	 int indexMaxArea = 0;
     	 Scalar wh = new Scalar(255, 255, 255, 255);
         
         Imgproc.findContours(mRgba, contours, hierarchy,Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
     	 try {
           for (int i = 0; i < contours.size(); i++) {
			if(Imgproc.contourArea(contours.get(i)) > maxArea){
                   indexMaxArea = i;
                   maxArea = Imgproc.contourArea(contours.get(i));
               }
           } 
           Imgproc.drawContours(mRgba, contours, indexMaxArea, wh);
           Imgproc.moments(contours.get(indexMaxArea));
     	  } catch (Exception e) {
             e.printStackTrace();
     	  }
   */
         
         return mRgba;
    } 

}