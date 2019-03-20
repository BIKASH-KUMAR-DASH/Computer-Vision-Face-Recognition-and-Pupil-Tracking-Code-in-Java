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



public class EyeTracer{
	
	static JFrame frame;
	static JFrame frame1;
	static JLabel lbl;
	static ImageIcon icon;
	
	
	public static void main(String[] args) {
		
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

		CascadeClassifier cascadeFaceClassifier = new CascadeClassifier(
				"D:/opencv/build/etc/haarcascades/haarcascade_frontalface_default.xml");
		CascadeClassifier cascadeEyeClassifier = new CascadeClassifier(
				"D://opencv/build/etc/haarcascades/haarcascade_eye.xml");
		
		// Webcam capture Starting
		VideoCapture videoDevice = new VideoCapture();
		videoDevice.open(0);
		if (videoDevice.isOpened()) {
			while (true) {		
				Mat frameCapture = new Mat();
				videoDevice.read(frameCapture);
				
				//Code Snippet for Face Detection
				MatOfRect faces = new MatOfRect();
				cascadeFaceClassifier.detectMultiScale(frameCapture, faces);								
				
				for (Rect rect : faces.toArray()) {
					Imgproc.putText(frameCapture, "Face", new Point(rect.x,rect.y-5), 1, 2, new Scalar(0,0,255));								
					Imgproc.rectangle(frameCapture, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height),
							new Scalar(0, 100, 0),3);
				}
		
				
				//Code Snippet for Eyes Detection
				MatOfRect eyes = new MatOfRect();
				cascadeEyeClassifier.detectMultiScale(frameCapture, eyes);		
				Rect rectCrop=null;
				for (Rect rect : eyes.toArray()) {
					
					Imgproc.putText(frameCapture, "Eye", new Point(rect.x,rect.y-5), 1, 2, new Scalar(0,0,255));				
					
					Imgproc.rectangle(frameCapture, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height),
							new Scalar(200, 200, 100),2);
				
					rectCrop = new Rect(rect.x, rect.y, rect.width, rect.height);
				
					
					//Code Snippet for Pupil Tracking
					Mat im1= new Mat(frameCapture,rectCrop);		
						
					Mat gray1 = new Mat(im1.rows(), im1.cols(), CvType.CV_8SC1);	
					Imgproc.cvtColor(im1, gray1, Imgproc.COLOR_RGB2GRAY); 	
					Imgproc.Canny(gray1, gray1, 80, 100);										
					Mat MatOut= new Mat();
					MatOfKeyPoint keyPoints = new MatOfKeyPoint();
					FeatureDetector fd = FeatureDetector.create(FeatureDetector.SIMPLEBLOB);
					// now edit params as you like, and read it back in:
					fd.read("C://Users/HOME/Desktop/params.xml"); // wherever you put it.
					fd.detect(im1, keyPoints);
					org.opencv.core.Scalar cores = new org.opencv.core.Scalar(0,0,255);
					org.opencv.features2d.Features2d.drawKeypoints(im1,keyPoints,MatOut,cores,2);
					Point pt = new Point();
					System.out.println("keypoints: " + keyPoints.toList());

					 try { 
							FileWriter writer = new FileWriter("C://Users/HOME/Desktop/MyFile.txt", true);
				            
					List<KeyPoint> list = keyPoints.toList();
					for (int i=0; i < list.size(); i++){
						pt.x = (int)list.get(i).pt.x + rect.x; 
					    pt.y = (int)list.get(i).pt.y + rect.y; 
					    double s=  list.get(i).size;
					    int r = (int)(Math.floor(s/8));
					    Imgproc.circle(frameCapture, pt, r, new Scalar(0, 0, 255), -3);        
					   
				            writer.write("Eye Cordinates are:"+"("+pt.x +","+ pt.y+ ")" );
				            writer.write("\r\n");   // write new line
					}
					   writer.close();
				       
					 } catch (IOException e) {
				            e.printStackTrace();
				        }
					    
					/* Alternate Method of using Hough Cirles for Pupil Tracking :: (Not Best Result though)   
					Mat im= new Mat(frameCapture,rectCrop);			
					Mat gray = new Mat(im.rows(), im.cols(), CvType.CV_8SC1);	
					Imgproc.cvtColor(im, gray, Imgproc.COLOR_RGB2GRAY); 	
					Imgproc.Canny(gray, gray, 80, 100);										
					Mat circles = new Mat();
					Imgproc.HoughCircles(gray, circles, Imgproc.CV_HOUGH_GRADIENT, 1, 20, 50, 30, 0, 0); 
					Point pt = new Point();
					System.out.println(circles.cols());
					for (int i = 0; i < circles.cols(); i++){
						double data[] = circles.get(0, i);
						pt.x = data[0] + rect.x;
						pt.y = data[1] + rect.y;
						double rho = data[2];
						System.out.println(pt);
						Imgproc.circle(frameCapture, pt, (int)rho, new Scalar(0, 0, 255), 3);
						Imgproc.circle(frameCapture, pt, 80, new Scalar(0, 0, 255), 3);
						}						
					Imgproc.rectangle(frameCapture, new Point(pt.x, pt.y), new Point(pt.x + pt.width, pt.y + pt.height),
									new Scalar(200, 200, 100),2);
					*/	
						
				}
				
				PushImage(ConvertMat2Image(frameCapture));
				System.out.println(String.format("%s yüz(FACES) %s göz(EYE)  detected.", faces.toArray().length,eyes.toArray().length));
		
		
		}
		} else {
			System.out.println("Video can't be opened.");
			return;
		}
		//webcam capture ends
		
	}
	
private static BufferedImage ConvertMat2Image(Mat CameraData) {
	
		
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
	frame = new JFrame();
	frame.setLayout(new FlowLayout());
	frame.setSize(700, 600);
	frame.setVisible(true);
	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
}

/*
public static Mat[] findROI(Mat src) {
    Mat maskedImage = new Mat(src.size(), src.type());
    Mat binaryImg = new Mat(src.size(), src.type());

    Imgproc.cvtColor(src, binaryImg, Imgproc.COLOR_BGR2GRAY);
    Imgproc.GaussianBlur(binaryImg, binaryImg, new Size(27, 27), 10000);

    int the_size = 2;
    Mat element = Imgproc.getStructuringElement(Imgproc.CV_SHAPE_RECT, new Size(2 * the_size + 1, 2 * the_size + 1), new Point(the_size, the_size));
    for (int v = 0; v < 5; v++) {
        Imgproc.dilate(binaryImg, binaryImg, element);
    }

    Imgproc.threshold(binaryImg, binaryImg, 0, 255, Imgproc.THRESH_BINARY + Imgproc.THRESH_OTSU);

    for (int v = 0; v < 6; v++) {
        Imgproc.erode(binaryImg, binaryImg, element);
    }

    List<MatOfPoint> contours = new ArrayList<>();
    Imgproc.findContours(binaryImg, contours,new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE );

    int idx = -1;
    int tmp = -1;
    for (MatOfPoint c : contours) {
        if(c.rows() > tmp) {
            tmp = c.rows();
            idx = contours.indexOf(c);
        }
    }

    Imgproc.drawContours(src, contours, idx, new Scalar(255,255,255),3  );

    Mat mask = new Mat(src.size(), CvType.CV_8UC1, new Scalar(255,255,255));
    Core.bitwise_not(mask,mask);

    Imgproc.drawContours(mask, contours, idx, new Scalar(255,255,255),3  );
    Imgproc.floodFill(mask, new Mat(), new Point(mask.size().width/2, mask.size().height/2) , new Scalar(255,255,255) );

    src.copyTo(maskedImage,mask); // creates masked Image and copies it to maskedImage
    Mat[] ret = new Mat[2];
    ret[0] = maskedImage;
    ret[1] = mask;
    //return maskedImage;
    return ret;
}
*/


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