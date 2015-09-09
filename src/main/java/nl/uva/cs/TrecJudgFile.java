/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package nl.uva.cs;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import static nl.uva.settings.Config.configFile;

/**
 *
 * @author Mostafa Dehghani
 */
public class TrecJudgFile {
    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TrecJudgFile.class.getName());
    private static HashMap<Map.Entry<String,String>,Double> rates;

    private static void loadRates() throws FileNotFoundException, IOException{
        if(rates != null)
            return;
        rates = new HashMap<>();
        String inputProfiles = configFile.getProperty("JUDG");
        BufferedReader br = new BufferedReader(new FileReader(inputProfiles));
        String line = br.readLine();
        while ((line = br.readLine()) != null) {
            String[] lineArr = line.split(",");
            Map.Entry<String,String> e1 = new AbstractMap.SimpleEntry<>(lineArr[0],lineArr[1]);
//            if(rates.containsKey(e1))
//                System.out.println(e1.getKey() + "," + e1.getValue());
            rates.put(e1, Double.parseDouble(lineArr[2]));
        }
    }
    

    
    public static void main(String[] args) throws IOException {
        loadRates();
        PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("judg.txt")));
        for(Map.Entry<String,String> e: rates.keySet()) {
            String line = null;
            if(rates.get(e) > 2)
                line =  e.getKey() + " 0 " + e.getValue()  + " 1";
            else // if(rates.get(e) < 2)
                line =  e.getKey() + " 0 " + e.getValue()  + " 0";
            out.print(line + "\n");
        }
        out.close(); 
    }
}
