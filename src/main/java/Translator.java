import javax.swing.JFrame;
import javax.swing.JLabel;

import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.MouseInfo;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.awt.Rectangle;
import java.io.File;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

import javax.imageio.ImageIO;

import org.bytedeco.javacpp.*;
import static org.bytedeco.javacpp.lept.*;
import static org.bytedeco.javacpp.tesseract.*;

public class Translator {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Hello!!");

        // Set's the window to be "always on top"
        frame.setAlwaysOnTop( true );
        
        frame.setTitle("Translator");
        frame.setLocationByPlatform( true );
        frame.add( new JLabel("Isn't this annoying?") );
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //frame.setUndecorated(true);
        frame.pack();
        frame.setVisible( true );
        try {
            while(true) {
                //captureImage();
                String result = translate();
                System.out.println(result);
                Thread.sleep(5000); 	
        }
        } catch(Exception e) {
    		System.err.println(e);
    	}
    }

    public static void captureImage() {
    	Graphics2D imageGraphics = null;
		try {
			Robot robot = new Robot();
			//GraphicsDevice currentDevice = MouseInfo.getPointerInfo().getDevice();
            Rectangle rect = new Rectangle(450, 908, 420, 110);
			BufferedImage exportImage = robot.createScreenCapture(rect);

			imageGraphics = (Graphics2D) exportImage.getGraphics();
			File screenshotFile = new File("./screen.png");
			ImageIO.write(exportImage, "png",screenshotFile);
			System.out.println("Screenshot successfully captured to screen.png!");
		} catch (Exception exp) {
			exp.printStackTrace();
		}
		finally {
			imageGraphics.dispose();
		}
    }

    public static String translate() throws UnsupportedEncodingException {
        BytePointer outText;

        TessBaseAPI api = new TessBaseAPI();
        // Initialize tesseract-ocr with English, without specifying tessdata path
        if (api.Init(null, "jpn1") != 0) {
            System.err.println("Could not initialize tesseract.");
            System.exit(1);
        }

        // Open input image with leptonica library
        PIX image = pixRead("screen.png");
        api.SetImage(image);
        // Get OCR result
        outText = api.GetUTF8Text();
        String result = outText.getString("UTF-8");

        // Destroy used object and release memory
        api.End();
        outText.deallocate();
        pixDestroy(image);

        return result;
    }
}