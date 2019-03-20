//Code for Face, Eye and Pupil Detection
//source for Face n eye Detection: [https://github.com/mesutpiskin/opencv-object-detection/blob/master/src/FaceAndEyeDetection/DetectFace.java]

import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
//import org.opencv.core.Size;
import org.opencv.core.Core;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.videoio.VideoCapture;

import org.opencv.core.KeyPoint;
import org.opencv.core.MatOfKeyPoint;
//import org.opencv.core.MatOfPoint;
import org.opencv.features2d.FeatureDetector;


//import java.io.File;
//import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
//import java.io.OutputStream;
//import java.util.ArrayList;
import java.util.List;



public class EyeTracker{
	
	static JFrame frame;
	static JFrame frame1;
	static JLabel lbl;
	static ImageIcon icon;

	
	public static void main(String[] args) {		
		
		//Loading of CascadeClassifiers for Face and Eye Detection
		//CascadeClassifiers have been loaded with pre-trained features from OpenCV.
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		
		//Haar Based Features Used For Face Cascade Classifier [Do note here, for achieving face detection at high speed,we can use LBP Classifiers as well]
		CascadeClassifier cascadeFaceClassifier = new CascadeClassifier(
				"D:/opencv/build/etc/haarcascades/haarcascade_frontalface_default.xml");
		
		//Haar Based Features Used For Eye Cascade Classifier [Do note here, for achieving eye detection at high speed,we can use LBP Classifiers as well]
		CascadeClassifier cascadeEyeClassifier = new CascadeClassifier(
				"D://opencv/build/etc/haarcascades/haarcascade_eye.xml");
		
		// Webcam capture Starting
		VideoCapture videoDevice = new VideoCapture();
		videoDevice.open(0);
		if (videoDevice.isOpened()) {
			while (true) {		
				
				Mat frameCapture = new Mat();
				videoDevice.read(frameCapture);

				//faceDetection function  performs FACE Detection
				faceDetection(frameCapture, cascadeFaceClassifier, cascadeEyeClassifier );

				// pupilSegmentation function performs Eyes Detection & Pupil Segmentation
			    pupilSegmentation(frameCapture, cascadeEyeClassifier);
			    
			    //PushImage function performs Mat to Image Bytes format conversion and display the image in Java Frame 
				PushImage(ConvertMat2Image(frameCapture));
			}
		} else {
			System.out.println("Video can't be opened.");
			return;
		}
		//webcam capture ends
		
	}
	
	private static void faceDetection(Mat frameCapture, CascadeClassifier cascadeFaceClassifier, CascadeClassifier cascadeEyeClassifier) {
		
		
		//Code Snippet for Face Detection
		MatOfRect faces = new MatOfRect();
		cascadeFaceClassifier.detectMultiScale(frameCapture, faces);								
		Rect rectCrop=null;
		for (Rect rect : faces.toArray()) {
			Imgproc.putText(frameCapture, "Face", new Point(rect.x,rect.y), 1, 2, new Scalar(0,0,255));								
			Imgproc.rectangle(frameCapture, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height),
					new Scalar(0, 100, 0),3);
			
			rectCrop = new Rect(rect.x, rect.y, rect.width, rect.height);
			Mat faceImage= new Mat(frameCapture,rectCrop);
		
			pupilSegmentation(faceImage,cascadeEyeClassifier);
		
		
		
		}

		System.out.println(String.format("%s Number of FACES", faces.toArray().length));
		
		
		
	}

	
	private static void pupilSegmentation(Mat frameCapture, CascadeClassifier cascadeEyeClassifier) {

		MatOfRect eyes = new MatOfRect();
		cascadeEyeClassifier.detectMultiScale(frameCapture, eyes);		
		Rect rectCrop=null;
		for (Rect rect : eyes.toArray()) {
			
			Imgproc.putText(frameCapture, "Eye", new Point(rect.x,rect.y), 1, 2, new Scalar(0,0,255));				
			
			Imgproc.rectangle(frameCapture, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height),
					new Scalar(200, 200, 100),2);
		
			rectCrop = new Rect(rect.x, rect.y, rect.width, rect.height);
		
			
			//Code Snippet for Pupil Tracking
			//cropping the eye region first
			Mat im1= new Mat(frameCapture,rectCrop);		
			Mat gray1 = new Mat(im1.rows(), im1.cols(), CvType.CV_8SC1);	
			Imgproc.cvtColor(im1, gray1, Imgproc.COLOR_RGB2GRAY); // RGB to Gray Color Conversion 	
			Imgproc.Canny(gray1, gray1, 80, 100);	//Canny Edge Detection Process									
			Mat MatOut= new Mat();
			MatOfKeyPoint keyPoints = new MatOfKeyPoint(); // Keypoint Containers
			FeatureDetector fd = FeatureDetector.create(FeatureDetector.SIMPLEBLOB); // SimpleBlob Operation for detecting features
			// now edit params as you like, and read it back in:
			fd.read("C://Users/HOME/Desktop/params.xml"); 
			fd.detect(im1, keyPoints);// detection process and keypoints location found
			org.opencv.core.Scalar cores = new org.opencv.core.Scalar(0,0,255);
			org.opencv.features2d.Features2d.drawKeypoints(im1,keyPoints,MatOut,cores,2); //drawing keypoints
			Point pt = new Point();
			System.out.println("keypoints: " + keyPoints.toList());

			 try { 
				 // Logging the coordinates of eye here
			FileWriter writer = new FileWriter("C://Users/HOME/Desktop/MyFile.txt", true);
		            
			List<KeyPoint> list = keyPoints.toList(); // List of keypoints here
			for (int i=0; i < list.size(); i++){
				pt.x = (int)list.get(i).pt.x + rect.x; // Finding Pupil's x-coordinates
			    pt.y = (int)list.get(i).pt.y + rect.y; // Finding Pupil's y-coordinates
			    double s=  list.get(i).size;
			    int r = (int)(Math.floor(s/8)); // Finding radius of pupil here
			    Imgproc.circle(frameCapture, pt, r, new Scalar(0, 0, 255), -3); //drawing red circle here        
			   
		            writer.write("Eye Cordinates are:"+"("+pt.x +","+ pt.y+ ")" ); // writing coordinates in text files
		            writer.write("\r\n");   // write new line
			}
			   writer.close();
		       
			 } catch (IOException e) {
		            e.printStackTrace();
		        }
			    						
		}
		
		System.out.println(String.format("%s Number of EYES  detected.", eyes.toArray().length));
	}
	
	
	private static BufferedImage ConvertMat2Image(Mat CameraData) {
	
		//Image Format Conversion takes place here. From Mat OpenCV format to Image Bytes format
		MatOfByte byteMatData = new MatOfByte();
		
		//Image codes in the format given to the memory
		Imgcodecs.imencode(".jpg", CameraData, byteMatData);
		
		//The toArray () method of the Mat object converts elements to bytes
		byte[] byteArray = byteMatData.toArray();
		BufferedImage image = null;
		try {
			InputStream in = new ByteArrayInputStream(byteArray);
			image = ImageIO.read(in);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return image;
	}

	public static void WindowCreate() {
		//Display Panel is being created here
		frame = new JFrame();
		frame.setLayout(new FlowLayout());
		//Resolution is set here
		frame.setSize(700, 600);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	//Creates a label to show the picture
	public static void PushImage(Image img2) {
		//window created
		if (frame == null)
			WindowCreate();
		//Previously uploaded an image? while the new one added
		if (lbl != null)
			frame.remove(lbl);
		icon = new ImageIcon(img2);
		lbl = new JLabel();
		lbl.setIcon(icon);
		frame.add(lbl);
		//Refreshes Frame object
		frame.revalidate();
	}
	
}