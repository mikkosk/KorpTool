
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import static com.google.gson.JsonParser.parseString;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Scanner;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author mikko
 */
public class reader {

    public static void main(String[] args) throws MalformedURLException, IOException {
        String base1 = "https://korp.csc.fi/cgi-bin/korp.cgi?command=count&groupby=word&defaultcontext=1+sentence&corpus=KLK_FI_18(";
        String base2 = ")&%0A&cqp=%5Bword+%3D+\"";
        String base3 = "\"+%25c%5D+%5Bword+%3D+\"";
        String base4 = "\"+%25c%5D&defaultwithin=sentence&within=&loginfo=lang%3Dfi+search%3Dadv=&indent=2";
        String[] names = new String[50];
        Double[][] values = new Double[100][50];
        Double[] yearCases = new Double[100];
        Double[] yearAbsSum = new Double[100];
        Double[] yearRelSum = new Double[100];
        
        for(int i = 0; i < 100; i++) {
            yearCases[i] = 0.0;
            yearAbsSum[i] = 0.0;
            yearRelSum[i] = 0.0;
            for(int j = 0; j < 50; j++) {
                values[i][j] = 0.0;
            }
        }
        
        try (Scanner tiedostonLukija = new Scanner(new File("Senaattorit.txt"))) {
            int n = 0;
            while (tiedostonLukija.hasNextLine()) {
                  
                String rivi = tiedostonLukija.nextLine();
                String[] pilkottu = rivi.split(";");
                String vuodet = "";
                for(int i = Integer.valueOf(pilkottu[3].trim()); i <= Integer.valueOf(pilkottu[4].trim()); i++) {
                    vuodet = vuodet + String.valueOf(i-1800)+".";
                    if (i == Integer.valueOf(pilkottu[4])) {
                        vuodet = vuodet.substring(0, vuodet.length()-1);
                    }
                }
                String valmis = base1 + vuodet + base2 + pilkottu[0] + base3 + pilkottu[1] + base3 + pilkottu[2] + base4;
                String valmis2 = base1 + vuodet + base2 + pilkottu[0].substring(0, 1) + "." + base3 + pilkottu[1].substring(0, 1) + "." + base3 + pilkottu[2] + base4;
                String valmis3 = base1 + vuodet + base2 + "senaattori" + base3 + pilkottu[2] + base4;
                System.out.println(valmis2);
                
                names[n] = pilkottu[2] + "Abs";
                n++;
                names[n] = pilkottu[2] + "Suht";
                n++;
                
                for(int v = 0; v < 3; v++) {
                    
                    URL url = null;
                    switch (v) {
                        case 0:
                            url = new URL(valmis);
                            break;
                        case 1:
                            url = new URL(valmis2);
                            break;
                        case 2:
                            url = new URL(valmis3);
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
                    
                    for(int i = Integer.valueOf(pilkottu[3].trim()); i <= Integer.valueOf(pilkottu[4].trim()); i++) {
                        
                        JsonObject year = corp.getAsJsonObject("KLK_FI_" + String.valueOf(i));
                        JsonObject sums = year.getAsJsonObject("sums");
                        
                        if (sums != null) {
                            
                            yearCases[i-1800] += 1.0;

                            values[i-1800][n-2] += sums.get("absolute").getAsDouble();
                            yearAbsSum[i-1800] += sums.get("absolute").getAsDouble();

                            values[i-1800][n-1] += sums.get("relative").getAsDouble();
                            yearRelSum[i-1800] += sums.get("relative").getAsDouble();

                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Virhe" + e.getMessage());
        }
        
        try (PrintWriter writer = new PrintWriter(new File("newTest.csv"))) {
            StringBuilder sb = new StringBuilder();
            sb.append("Vuosi");
            sb.append(',');
            for(int j = 0; j < 50; j++) {
                sb.append(names[j]);
                sb.append(',');
            }
            sb.append("AbsPerCases");
            sb.append(",");
            sb.append("RelPerCases");
            sb.append(",");
            sb.append('\n');
            for(int i = 0; i < 100; i++) {
                sb.append(String.valueOf(1800+i));
                sb.append(',');
                for(int j = 0; j < 50; j++) {
                    sb.append(values[i][j]);
                    sb.append(',');
                }
                if(yearCases[i] != 0) {
                    sb.append(String.valueOf(yearAbsSum[i]/yearCases[i]));
                    sb.append(',');
                    sb.append(String.valueOf(yearRelSum[i]/yearCases[i]));
                    sb.append(',');
                } else {
                    sb.append("0.0");
                    sb.append(',');
                    sb.append("0.0");
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
