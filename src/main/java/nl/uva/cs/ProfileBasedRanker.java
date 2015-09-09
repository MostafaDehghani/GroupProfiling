/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.uva.cs;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import nl.uva.lm.CollectionSLM;
import nl.uva.lm.Divergence;
import nl.uva.lm.LanguageModel;
import static nl.uva.lm.LanguageModel.sortByValues;
import nl.uva.lm.SmoothedLM;
import nl.uva.lm.StandardLM;
import nl.uva.lucenefacility.IndexInfo;
import static nl.uva.settings.Config.configFile;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.FSDirectory;

/**
 *
 * @author Mostafa Dehghani
 */
public class ProfileBasedRanker {

    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ProfileBasedRanker.class.getName());
    private String indexPathString;
    private Path ipath;
    private IndexReader ireader;
    private IndexInfo iInfo;
    private HashMap<Entry<String,String>, Entry<Double,HashSet<String>>> rates;

    public ProfileBasedRanker() throws IOException {
        indexPathString = configFile.getProperty("INDEX_PATH");
        ipath = FileSystems.getDefault().getPath(indexPathString);
        ireader = DirectoryReader.open(FSDirectory.open(ipath));
        iInfo = new IndexInfo(ireader);
    }

    public void main() throws FileNotFoundException, IOException {
        File f2 = new File("response-treceval.res");
            f2.delete();
        String field = "TEXT";
        indexPathString = configFile.getProperty("INDEX_PATH");
        ipath = FileSystems.getDefault().getPath(indexPathString);
        ireader = DirectoryReader.open(FSDirectory.open(ipath));
        iInfo = new IndexInfo(ireader);
        CollectionSLM CLM = new CollectionSLM(ireader, field);

        String inputProfiles = configFile.getProperty("REQS");
        String line = null;
        BufferedReader br = new BufferedReader(new FileReader(inputProfiles));
        Integer cnt = 1;
        RequestInfo reqInfo = new RequestInfo();
        while ((line = br.readLine()) != null) {
            reqInfo.setJson(line);
            Request r = reqInfo.GetRequest();
            HashMap<String, Double> scores = new HashMap<>();
            for (RatedDoc candidate : r.ratedCandidates) {
                Integer indexId = iInfo.getIndexId(candidate.docID);
                LanguageModel candidateSLM = new StandardLM(ireader, indexId, field);
                SmoothedLM candidateSLM_smoothed = new SmoothedLM(candidateSLM, CLM);
                SmoothedLM PMLM_smoothed = new SmoothedLM(r.getUserPrefPositiveMixtureLM(), CLM);
                
                Divergence div = new Divergence(candidateSLM_smoothed, PMLM_smoothed);
                Double score = div.getJsdSimScore();
                scores.put(candidate.docID,score);
            }
            List<Map.Entry<String, Double>> sortedCandidates = sortByValues(scores, false);
            System.out.println("Request: " + cnt++);
            this.OutputGenerator_trecEval(r.pId, sortedCandidates);
        }
        
    }
    
    
    private void OutputGenerator_trecEval(String pId, List<Map.Entry<String, Double>> sortedCandidates) throws IOException{
        //query-number    Q0  document-id rank    score   Exp
        PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("ProfileBased.res", true)));
        Integer rank = 1;
        for(Map.Entry<String, Double> e : sortedCandidates){
                String line = pId + " Q0 " + e.getKey() + " " + rank + " " + e.getValue() + " ProfileBased";
                out.println(line);
                rank++;
        }
        out.close();
    }
    
     

    public static void main(String[] args) throws IOException {
        File f = new File("ProfileBased.res");
            f.delete();
        ProfileBasedRanker r = new ProfileBasedRanker();
        r.main();
        System.out.println("Finished...");
    }

}
