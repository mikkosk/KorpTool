/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author mikko
 */

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import static com.google.gson.JsonParser.parseString;
import com.google.gson.JsonPrimitive;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

public class Maaseutu {

    public static void main(String[] args) throws MalformedURLException, IOException {
        HashMap<String, Double[][]> result = new HashMap<>();
        String yksikko = "https://korp.csc.fi/cgi-bin/korp.cgi?command=count&groupby=text_publ_title&corpus=klk_fi_18(50.51.52.53.54.55.56.57.58.59.60.61.62.63.64.65.66.67.68.69.70.71.72.73.74.75.76.77.78.79.80.81.82.83.84.85.86.87.88.89.90.91.92.93.94.95.96.97.98.99)&cqp=%5Bmsd+%3D+%22.*NUM_Sg.*%22+%26+lemma+%3D+%22maaseutu%22%5D&indent=2";
        String monikko = "https://korp.csc.fi/cgi-bin/korp.cgi?command=count&groupby=text_publ_title&corpus=klk_fi_18(50.51.52.53.54.55.56.57.58.59.60.61.62.63.64.65.66.67.68.69.70.71.72.73.74.75.76.77.78.79.80.81.82.83.84.85.86.87.88.89.90.91.92.93.94.95.96.97.98.99)&cqp=%5Bmsd+%3D+%22.*NUM_Pl.*%22+%26+lemma+%3D+%22maaseutu%22%5D&indent=2";
        String genetiivi = "https://korp.csc.fi/cgi-bin/korp.cgi?command=count&groupby=text_publ_title&corpus=klk_fi_18(50.51.52.53.54.55.56.57.58.59.60.61.62.63.64.65.66.67.68.69.70.71.72.73.74.75.76.77.78.79.80.81.82.83.84.85.86.87.88.89.90.91.92.93.94.95.96.97.98.99)&cqp=%5Bdeprel%20%3D%20\"poss\"%5D%20%5Blemma%20%3D%20\"maaseutu\"%5D&indent=2";
                for(int v = 0; v < 3; v++) {
                    
                    URL url = null;
                    switch (v) {
                        case 0:
                            url = new URL(yksikko);
                            break;
                        case 1:
                            url = new URL(monikko);
                            break;
                        case 2:
                            url = new URL(genetiivi);
                            break;
                    }

                    InputStream is = url.openStream();
                    InputStreamReader isReader = new InputStreamReader(is);
                    BufferedReader reader = new BufferedReader(isReader);
                    StringBuilder sb = new StringBuilder();
                    String str;
                    while((str = reader.readLine())!= null){
                       sb.append(str);
                    }
                    is.close();
                    
                    JsonElement parser = parse(sb.toString());
                    JsonObject obj = parser.getAsJsonObject();
                    JsonObject corp = obj.getAsJsonObject("corpora");
                    
                    for(int i = 1850; i <= 1899; i++) {
                        
                        JsonObject year = corp.getAsJsonObject("KLK_FI_" + String.valueOf(i));
                        JsonObject relative = year.getAsJsonObject("relative");
                        Set<String> entries = relative.keySet();
                        Object[] entriesArr = entries.toArray();
                   
                        for(int r = 0; r  < entriesArr.length; r++) {
                            String entryName = entriesArr[r].toString();
                            if(entryName.endsWith(entryName.substring(0, Math.floorDiv(entryName.length(), 2)))) {
                                entryName = entryName.substring(0, Math.floorDiv(entryName.length(), 2));
                                System.out.println(entryName);
                            }
                            entryName = entryName.replace(',', ' ');
                            Double[][] updatedArray = result.getOrDefault(entryName, null);
                            if(updatedArray == null) {
                                Double[][] empty = new Double[50][3];
                                for (int n = 0; n < 50; n++) {
                                    empty[n][0] = 0.0;
                                    empty[n][1] = 0.0;
                                    empty[n][2] = 0.0;
                                }
                                updatedArray = empty;
                            }
                            updatedArray[i-1850][v] += relative.getAsJsonPrimitive(entriesArr[r].toString()).getAsDouble();
                            result.put(entryName, updatedArray);
                        }
                        
                    }
                }

        Object[] resArr = result.keySet().toArray();
        for(int i = 0; i < resArr.length; i++) {
            Double[][] tulos = result.get(resArr[i].toString());
            int counter = 0;
            for(int j = 0; j < 50; j++) {
                if(tulos[j][0] == 0.0 || tulos[j][1] == 0.0 || tulos[j][2] == 0.0) {
                    counter++;
                } 
            }
            
            if(counter > 30) {
                result.remove(resArr[i].toString());
                System.out.println("remove");
            }
        }
        try (PrintWriter writer = new PrintWriter(new File("maaseutu.csv"))) {
            StringBuilder sb = new StringBuilder();
            sb.append("Vuosi");
            sb.append(',');
            resArr = result.keySet().toArray();
            for(int j = 0; j < resArr.length; j++) {
                sb.append(resArr[j].toString()).append("Yks");
                sb.append(',');
            }
            for(int j = 0; j < resArr.length; j++) {
                sb.append(resArr[j].toString()).append("Mon");
                sb.append(',');
            }
            for(int j = 0; j < resArr.length; j++) {
                sb.append(resArr[j].toString()).append("Gen");
                sb.append(',');
            }
            sb.append('\n');
            for(int i = 0; i < 50; i++) {
                sb.append(String.valueOf(1850+i));
                sb.append(',');
                for(int j = 0; j < resArr.length; j++) {
                    Double[][] valueArr = result.get(resArr[j].toString());
                    sb.append(valueArr[i][0].toString());
                    sb.append(',');
                }
                for(int j = 0; j < resArr.length; j++) {
                    Double[][] valueArr = result.get(resArr[j].toString());
                    sb.append(valueArr[i][1].toString());
                    sb.append(',');
                }
                for(int j = 0; j < resArr.length; j++) {
                    Double[][] valueArr = result.get(resArr[j].toString());
                    sb.append(valueArr[i][2].toString());
                    sb.append(',');
                }
                sb.append('\n');
            }
            writer.write(sb.toString());
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
        }
        
    }
    
    public static JsonElement parse(String json) {
        return parseString(json);
    }
}

