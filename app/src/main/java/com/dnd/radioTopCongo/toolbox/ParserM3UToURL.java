package com.dnd.radioTopCongo.toolbox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.util.Log;

public class ParserM3UToURL {

    public static String parse(String M3U_URL) {

        String ligne = null;

        try {
        	
        	Log.i("A", "A");
            URL urlPage = new URL(M3U_URL);
            Log.i("B", "B");
            HttpURLConnection connection = (HttpURLConnection) urlPage.openConnection();
            Log.i("C", "C");
            InputStream inputStream = connection.getInputStream();
            Log.i("D", "D");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            Log.i("E", "E");

            StringBuffer stringBuffer = new StringBuffer();
            
            Log.i("F", "F");
            
            while((ligne = bufferedReader.readLine()) != null) {
            	
                if (ligne.contains("http")) {
                    connection.disconnect();
                    bufferedReader.close();
                    inputStream.close();
                    return ligne;
                }
                stringBuffer.append(ligne);
            }
            
            Log.i("G", "G");

	        connection.disconnect();
	        bufferedReader.close();
	        inputStream.close();
	        
	        Log.i("H", "H");
        
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
