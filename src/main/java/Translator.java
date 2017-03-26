import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import java.io.File;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.nio.charset.Charset;
import java.net.URLEncoder;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.Header;
import org.apache.http.util.EntityUtils;
import org.apache.http.protocol.HTTP;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import com.google.gson.Gson;

/*
import javax.imageio.ImageIO;

import org.bytedeco.javacpp.*;
import static org.bytedeco.javacpp.lept.*;
import static org.bytedeco.javacpp.tesseract.*;*/

public class Translator {
    public static void main(String[] args) {
        try{
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch(Exception e) {

        }

        final JFrame frame = new JFrame("Hello!!");

        // Set's the window to be "always on top"
        frame.setAlwaysOnTop( true );
        
        frame.setTitle("Translator");
        frame.setLocationByPlatform(true);
        frame.setLayout(new BorderLayout());
        JTextArea translationArea = new JTextArea(11, 50);
        JScrollPane translationScrollPane = new JScrollPane(translationArea);
        translationScrollPane.setPreferredSize(new Dimension(600, 200));
        JTextArea originalArea = new JTextArea(11, 50);
        final JScrollPane originalScrollPane = new JScrollPane(originalArea);
        originalScrollPane.setPreferredSize(new Dimension(600, 200));
        frame.add(translationScrollPane, BorderLayout.PAGE_START);
        frame.add(originalScrollPane, BorderLayout.PAGE_END);

        JButton b = new JButton("Show/Hide");
        b.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                if(originalScrollPane.isVisible()) {
                    originalScrollPane.setVisible(false);
                } else {
                    originalScrollPane.setVisible(true);
                }
                frame.pack();
            }
        });
        frame.add(b, BorderLayout.LINE_END);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //frame.setUndecorated(true);
        frame.pack();
        frame.setVisible(true);
        File file = getLatestFilefromDir("D:\\ProgramFiles\\BNO\\GundamOnline\\chat");
        
        Gson gson = new Gson();
        try {
            //BufferedWriter bw = new BufferedWriter(new FileWriter("test.txt"));
            String result, chat, originalChat, nameAndText, translated;
            String[] lines;
            while(true) {
                result = tail(file, 20);
                lines = result.split("\\u000A");
                chat = "";
                originalChat = "";
                for(int i = 0; i < lines.length; i++) {
                    int space = lines[i].indexOf(" ");
                    nameAndText = lines[i].substring(space+1, lines[i].length());
                    translated = translate(nameAndText);
                    if(!translated.contains("{") && !translated.contains("}")) {
                        chat += nameAndText+"\n";
                    } else {
                        TranslatedMessage msg = gson.fromJson(translated, TranslatedMessage.class);
                        chat += msg.getTranslationText()+"\n";
                    }
                    originalChat += nameAndText+"\n";
                }
                translationArea.setText(chat);
                translationArea.update(translationArea.getGraphics());
                if(originalScrollPane.isVisible()) {
                    originalArea.setText(originalChat);
                    originalArea.update(originalArea.getGraphics());
                }
                //bw.write(chat); 
                //bw.flush();
                Thread.sleep(5000);
            }

        } catch(Exception e) {
    		System.err.println(e);
    	}
    }

    public static String translate(String s) {
        try {
            String sourceLang = "ja";
            String targetLang = "en";
            //String url = "https://translate.googleapis.com/translate_a/single?client=gtx&sl=" 
            //    + sourceLang + "&tl=" + targetLang + "&dt=t&q=" + URLEncoder.encode(s, "UTF-8");
            String url = "http://www.transltr.org/api/translate?text="+URLEncoder.encode(s, "UTF-8")+"&to="
                +targetLang+"&from="+sourceLang;

            /*HttpClient client = new DefaultHttpClient();
            HttpGet request = new HttpGet(url);        
            //request.setContentType("application/json; charset=UTF-8");
            HttpResponse response = client.execute(request);
            String result = EntityUtils.toString(response.getEntity(), HTTP.UTF_8);
            //BufferedReader rd = new BufferedReader (new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
            //return rd.readLine();
            return result;*/

            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                .url(url)
                .build();

            Response response = client.newCall(request).execute();
            return response.body().string();
        } catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String tail(File file, int lines) {
        java.io.RandomAccessFile fileHandler = null;
        try {
            fileHandler = new java.io.RandomAccessFile( file, "r" );
            long fileLength = fileHandler.length() - 1;
            ArrayList<Byte> byteArraylist = new ArrayList<Byte>();
            int line = 0;

            for(long filePointer = fileLength; filePointer != -1; filePointer--){
                fileHandler.seek( filePointer );
                byte readByte = fileHandler.readByte();

                 if( readByte == 0xA ) {
                    if (filePointer < fileLength) {
                        line = line + 1;
                    }
                } else if( readByte == 0xD ) {
                    if (filePointer < fileLength-1) {
                        line = line + 1;
                    }
                }
                if (line >= lines) {
                    break;
                }
                byteArraylist.add(readByte);
            }

            // convert arraylist to array
            byte[] byteArray = new byte[byteArraylist.size()];
            for(int i = 0; i < byteArraylist.size(); i++) {
                byteArray[i] = (byte)byteArraylist.get(i);
            }

            StringBuilder sb = new StringBuilder();
            sb.append(new String(byteArray, Charset.forName("UTF-16BE")));
            String lastLine = sb.reverse().toString();
            return lastLine;
        } catch( java.io.FileNotFoundException e ) {
            e.printStackTrace();
            return null;
        } catch(IOException e) {
            e.printStackTrace();
            return null;
        }
        finally {
            if (fileHandler != null )
                try {
                    fileHandler.close();
                } catch (IOException e) {
                }
        }
    }

    public static File getLatestFilefromDir(String dirPath){
        File dir = new File(dirPath);
        File[] files = dir.listFiles();
        if (files == null || files.length == 0) {
            return null;
        }

        File lastModifiedFile = files[0];
        for (int i = 1; i < files.length; i++) {
           if (lastModifiedFile.lastModified() < files[i].lastModified()) {
               lastModifiedFile = files[i];
           }
        }
        return lastModifiedFile;
    }

    /*
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
    }*/
}