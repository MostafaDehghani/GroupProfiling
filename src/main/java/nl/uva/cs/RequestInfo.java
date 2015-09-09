package nl.uva.cs;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import nl.uva.lucenefacility.IndexInfo;
import static nl.uva.settings.Config.configFile;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.FSDirectory;

public class RequestInfo {

    String profileJson;
    String indexPathString;
    Path ipath;
    IndexReader ireader;
    IndexInfo iInfo;
    private HashMap<Map.Entry<String,String>, Map.Entry<Double,HashSet<String>>> rates;

    public RequestInfo() throws IOException {
        indexPathString = configFile.getProperty("INDEX_PATH");
        ipath = FileSystems.getDefault().getPath(indexPathString);
        ireader = DirectoryReader.open(FSDirectory.open(ipath));
        iInfo = new IndexInfo(ireader);
        this.loadRates();
    }
    
    public void setJson(String profileJson){
             this.profileJson = profileJson;
    }

    private JsonArray getSuggestionCandidates() {
        JsonElement jelement = new JsonParser().parse(profileJson);
        JsonObject jobject = jelement.getAsJsonObject();
        JsonArray jarray = jobject.getAsJsonArray("candidates");
        return jarray;
       
    }

    private JsonArray getPreferences() {
        JsonElement jelement = new JsonParser().parse(profileJson);
        JsonObject jobject = jelement.getAsJsonObject();
        jobject = jobject.getAsJsonObject("body");
        JsonObject jPersonObject = jobject.getAsJsonObject("person");
        JsonArray jarray = jPersonObject.getAsJsonArray("preferences");
        return jarray;
    }

    public String getProfileID() {
        String profileID = "";
        JsonElement jelement = new JsonParser().parse(profileJson);
        JsonObject jobject = jelement.getAsJsonObject();
        jobject = jobject.getAsJsonObject("body");
        JsonObject jPersonObject = jobject.getAsJsonObject("person");
        profileID = jPersonObject.get("id").toString().replaceAll("\"", "").trim();
        return profileID;
    }
    
    private String getLocationID() {
        String locationID = "";
        JsonElement jelement = new JsonParser().parse(profileJson);
        JsonObject jobject = jelement.getAsJsonObject();
        jobject = jobject.getAsJsonObject("body");
        JsonObject jLocationObject = jobject.getAsJsonObject("location");
        locationID = jLocationObject.get("id").toString().replaceAll("\"", "").trim();
        return locationID;
    }
    
    private String getGroup() {
        String group = "";
        JsonElement jelement = new JsonParser().parse(profileJson);
        JsonObject jobject = jelement.getAsJsonObject();
        jobject = jobject.getAsJsonObject("body");
        if(jobject.get("group") == null)
            group = "unknown";
        else
            group = jobject.get("group").toString().replaceAll("\"", "").trim();

        return group;
    }
    
    private String getSeason() {
        String season = "";
        JsonElement jelement = new JsonParser().parse(profileJson);
        JsonObject jobject = jelement.getAsJsonObject();
        jobject = jobject.getAsJsonObject("body");
        
        if(jobject.get("season") == null)
            season = "unknown";
        else
            season = jobject.get("season").toString().replaceAll("\"", "").trim();

        return season;
    }
    
    private String getTripType() {
        String tripType = "";
        JsonElement jelement = new JsonParser().parse(profileJson);
        JsonObject jobject = jelement.getAsJsonObject();
        jobject = jobject.getAsJsonObject("body");
        if(jobject.get("trip_type") == null)
            tripType = "unknown";
        else
            tripType = jobject.get("trip_type").toString().replaceAll("\"", "").trim();

        return tripType;
    }
        
    private String getDuration() {
        String duration = "";
        JsonElement jelement = new JsonParser().parse(profileJson);
        JsonObject jobject = jelement.getAsJsonObject();
        jobject = jobject.getAsJsonObject("body");
        if(jobject.get("duration") == null)
            duration = "unknown";
        else
            duration = jobject.get("duration").toString().replaceAll("\"", "").trim();

        return duration;
    }

    private String getRequestID() {
        String requestID = "";
        JsonElement jelement = new JsonParser().parse(profileJson);
        JsonObject jobject = jelement.getAsJsonObject();
        requestID = jobject.get("id").toString().replaceAll("\"", "").trim();

        return requestID;
    }
    
    public Request GetRequest() throws FileNotFoundException, IOException {

        String reqId = this.getRequestID();
        String uId = this.getProfileID();
        String group = this.getGroup();
        String tripType = this.getTripType();
        String locationId = this.getLocationID();
        String duration = this.getDuration();
        String season = this.getSeason();
        
        
        JsonArray jarray1 = this.getPreferences();
        HashSet<RatedDoc> Preferences = new HashSet<>();
        /*
        for (int i = 0; i < jarray1.size(); i++) {
            JsonObject jobjectIterator = jarray1.get(i).getAsJsonObject();
            String docID = jobjectIterator.get("documentId").toString().replaceAll("\"", "").trim();
            
            //Filter non-crowled Docs
            if (iInfo.getIndexId(docID) == null){
                System.err.println("ERROR:" + docID);
                continue;
            }
            
            Map.Entry<String,String> e = new AbstractMap.SimpleEntry<>(uId,docID);
            if(!rates.containsKey(e)){
//                System.err.println("No judge for preference: " + uId + "," + docID);
                continue;
            }
            Map.Entry<Double,HashSet<String>> en = rates.get(e);
            RatedDoc rd = new RatedDoc(docID,en.getKey(), en.getValue());
            Preferences.add(rd);
        }
                */
        for (int i = 0; i < jarray1.size(); i++) {
            Double rating = null;
            JsonObject jobjectIterator = jarray1.get(i).getAsJsonObject();
            String docID = jobjectIterator.get("documentId").toString().replaceAll("\"", "").trim();
            
            //Filter non-crowled Docs
            if (iInfo.getIndexId(docID) == null){
                System.out.println(docID);
                continue;
            }
                
            if (jobjectIterator.get("rating") != null) {
                rating = Double.parseDouble(jobjectIterator.get("rating").toString());
            } else {
                System.out.println("ERR: No rating! for docID: " + docID);
            }

            RatedDoc rd = null;
            if (jobjectIterator.getAsJsonArray("tags") == null) {
                rd = new RatedDoc(docID, rating);
            } else {
                JsonArray jarray3 = jobjectIterator.getAsJsonArray("tags");
                HashSet<String> tags = new HashSet<>();
                for (int j = 0; j < jarray3.size(); j++) {
                    tags.add(jarray3.get(j).toString());
                }
                rd = new RatedDoc(docID, rating, tags);
            }
            Preferences.add(rd);
        }
        
        JsonArray jarray2 = this.getSuggestionCandidates();
        HashSet<RatedDoc> Candidates = new HashSet<>();
        for (int i = 0; i < jarray2.size(); i++) {
            String docID =jarray2.get(i).toString().replaceAll("\"", "").trim();
            //Filter non-crowled Docs
            if (iInfo.getIndexId(docID) == null){
                System.err.println("ERROR:" + docID);
                continue;
            }
            Map.Entry<String,String> e = new AbstractMap.SimpleEntry<>(uId,docID);
            if(!rates.containsKey(e)){
//                System.err.println("No judge for candidate: " + uId + "," + docID);
                continue;
            }
            Map.Entry<Double,HashSet<String>> en = rates.get(e);
            RatedDoc rd = new RatedDoc(docID,en.getKey(), en.getValue());
            Candidates.add(rd);
        }
       
        Request r = new Request(uId, reqId, group, season, tripType, duration, locationId, Preferences, Candidates);
        
        return r;
    }
    
    private void loadRates() throws FileNotFoundException, IOException{
        if(rates != null)
            return;
        rates = new HashMap<>();
        String inputProfiles = configFile.getProperty("JUDG");
        BufferedReader br = new BufferedReader(new FileReader(inputProfiles));
        String line = br.readLine();
        while ((line = br.readLine()) != null) {
            String[] lineArr = line.split(",");
            Map.Entry<String,String> e1 = new AbstractMap.SimpleEntry<>(lineArr[0],lineArr[1]);
            HashSet<String> tags = new HashSet<>(); 
            if(lineArr.length > 3){
                for(String s:lineArr[3].split("|")){
                    tags.add(s);
                }
            }
//            if(rates.containsKey(e1))
//                System.out.println(e1.getKey() + "," + e1.getValue());
            Map.Entry<Double,HashSet<String>> e2 = new AbstractMap.SimpleEntry<>(Double.parseDouble(lineArr[2]),tags);
            this.rates.put(e1, e2);
        }
    }

}
