/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.uva.gp;

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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import nl.uva.generalinzedlm.DocsGroup;
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
public class GroupBasedRanker {

    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(GroupBasedRanker.class.getName());
    private String indexPathString;
    private Path ipath;
    private IndexReader ireader;
    private IndexInfo iInfo;
    private String RindexPathString;
    private Path Ripath;
    private IndexReader Rireader;
    private IndexInfo RiInfo;
    private HashMap<Entry<String,String>, Entry<Double,HashSet<String>>> rates;
    public String group = "SEASON";

    public GroupBasedRanker() throws IOException {
        indexPathString = configFile.getProperty("INDEX_PATH");
        ipath = FileSystems.getDefault().getPath(indexPathString);
        ireader = DirectoryReader.open(FSDirectory.open(ipath));
        iInfo = new IndexInfo(ireader);
        RindexPathString = configFile.getProperty("RINDEX_PATH");
        Ripath = FileSystems.getDefault().getPath(RindexPathString);
        Rireader = DirectoryReader.open(FSDirectory.open(Ripath));
        RiInfo = new IndexInfo(Rireader);
    }

    public void ranker() throws FileNotFoundException, IOException {
        String field = "TEXT";
        CollectionSLM CLM = new CollectionSLM(ireader, field);
        String inputProfiles = configFile.getProperty("REQS");
        String line = null;
        BufferedReader br = new BufferedReader(new FileReader(inputProfiles));
        Integer cnt = 1;
        RequestInfo reqInfo = new RequestInfo();
        while ((line = br.readLine()) != null) {
            reqInfo.setJson(line);
            Request r = reqInfo.GetRequest();
            Integer RIndexId = RiInfo.getIndexId(r.reqId);
            String rGroup = Rireader.document(RIndexId).get(this.group);
            if(rGroup.equalsIgnoreCase("unknown")){
                System.err.println("Unknown group for:" + this.group + " for request:" + r.reqId);
            }
            ArrayList<Integer> docsList = RiInfo.getDocsContainingTerm(this.group, rGroup);
            System.out.println("Number of doc in group" + this.group + ":" + rGroup + " is:" + docsList.size());
            DocsGroup dGroup = new DocsGroup(Rireader, "TEXT", docsList);
            HashMap<String, Double> scores = new HashMap<>();
            
            for (RatedDoc candidate : r.ratedCandidates) {
                Integer indexId = iInfo.getIndexId(candidate.docID);
                LanguageModel candidateSLM = new StandardLM(ireader, indexId, field);
                SmoothedLM candidateSLM_smoothed = new SmoothedLM(candidateSLM, CLM);
                SmoothedLM groupLM_smoothed = new SmoothedLM(dGroup.getGroupGeneralizedLM(), CLM);
                
                Divergence div = new Divergence(candidateSLM_smoothed, groupLM_smoothed);
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
        PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("GroupBased_" + this.group + ".res", true)));
        Integer rank = 1;
        for(Map.Entry<String, Double> e : sortedCandidates){
                String line = pId + " Q0 " + e.getKey() + " " + rank + " " + e.getValue() + " GruppBased_" + this.group;
                out.println(line);
                rank++;
        }
        out.close();
    }
    
     

    public static void main(String[] args) throws IOException {
       
        GroupBasedRanker r = new GroupBasedRanker();
        File f = new File("GroupBased_" + r.group + ".res");
            f.delete();
        r.ranker();
        System.out.println("Finished...");
    }

}
