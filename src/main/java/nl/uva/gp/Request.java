/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package nl.uva.gp;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.AbstractMap;
import java.util.HashSet;
import java.util.Map;
import nl.uva.lm.LanguageModel;
import nl.uva.lm.MixtureLM;
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
public class Request {
    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Request.class.getName());
    
    public double nuteralRate = 2;
    public String pId;
    public String reqId;
    public String group;
    public String season ;
    public String tripType;
    public String duration;
    public String locationId;
    public HashSet<RatedDoc> ratedPrefrences;
    public HashSet<RatedDoc> ratedCandidates;
    
    public LanguageModel userPrefPositiveMixtureLM;
    public LanguageModel userCandPositiveMixtureLM;
//    public LanguageModel userNegativeMixturePLM;

    public Request(String pId, String reqId, String group, String season, String tripType, String duration, String locationId, HashSet<RatedDoc> ratedPrefrences, HashSet<RatedDoc> ratedCandidates) throws IOException {
        this.pId = pId;
        this.reqId = reqId;
        this.group = group;
        this.season = season;
        this.tripType = tripType;
        this.duration = duration;
        this.locationId = locationId;
        this.ratedPrefrences = ratedPrefrences;
        this.ratedCandidates = ratedCandidates;
    }

    public LanguageModel getUserPrefPositiveMixtureLM() throws IOException {
        if(this.userPrefPositiveMixtureLM == null)
            this.generateUserPrefLM();
        return userPrefPositiveMixtureLM;
    }

    public LanguageModel getUserCandPositiveMixtureLM() throws IOException {
        if(this.userCandPositiveMixtureLM == null)
            this.generateUserCandLM();
        return userCandPositiveMixtureLM;
    }
    
    


    
    private void generateUserPrefLM() throws IOException{
        HashSet<RatedDoc> positivePrefrences = new HashSet<>();
//        HashSet<Prefrence> negativePrefrences = new HashSet<>();
        Double sumPR =0D;
//        Double sumNR =0D;
        for(RatedDoc p : this.ratedPrefrences){
           
            if(p.rate == -1 /*not seen*/ || p.rate == this.nuteralRate)
                continue;
            
            if(p.rate > 2){
                if(p.rate == 4.0)
                    p.setNewRate(2.0);
                if(p.rate == 3.0)
                    p.setNewRate(1.0);
                positivePrefrences.add(p);
                sumPR += p.rate;
            }
            
//            else if(p.rate < 2){
//                if(p.rate == 0.0)
//                    p.setNewRate(2.0);
//                if(p.rate == 1.0)
//                    p.setNewRate(1.0);
//                negativePrefrences.add(p);
//                sumNR += p.rate;
//            }
        }
        this.userPrefPositiveMixtureLM = this.MixtureModel(sumPR, positivePrefrences);
//        this.userNegativeMixturePLM = new ParsimoniousMixtureModel(sumNR, negativePrefrences);
    }
    private void generateUserCandLM() throws IOException{
        HashSet<RatedDoc> positiveCandidates = new HashSet<>();
//        HashSet<Prefrence> negativePrefrences = new HashSet<>();
        Double sumPR =0D;
//        Double sumNR =0D;
        for(RatedDoc c : this.ratedCandidates){
           
            if(c.rate == -1 || c.rate == 2)
                continue;
            
            if(c.rate > 2){
                if(c.rate == 4.0)
                    c.setNewRate(2.0);
                if(c.rate == 3.0)
                    c.setNewRate(1.0);
                positiveCandidates.add(c);
                sumPR += c.rate;
            }
            
//            else if(p.rate < 2){
//                if(p.rate == 0.0)
//                    p.setNewRate(2.0);
//                if(p.rate == 1.0)
//                    p.setNewRate(1.0);
//                negativePrefrences.add(p);
//                sumNR += p.rate;
//            }
//        }
                }
        this.userCandPositiveMixtureLM = this.MixtureModel(sumPR, positiveCandidates);
//        this.userNegativeMixturePLM = new ParsimoniousMixtureModel(sumNR, negativePrefrences);
    }
    
    public LanguageModel MixtureModel(Double rateSum, HashSet<RatedDoc> rd) throws IOException {
        String field = "TEXT";
        String indexPathString = configFile.getProperty("INDEX_PATH");
        Path ipath = FileSystems.getDefault().getPath(indexPathString);
        IndexReader ireader = DirectoryReader.open(FSDirectory.open(ipath));
        IndexInfo iInfo = new IndexInfo(ireader);
        HashSet<Map.Entry<Double,LanguageModel>> prob_pref = new HashSet<>();
        for(RatedDoc p:rd){
            Integer indexId = iInfo.getIndexId(p.docID);
            LanguageModel docSLM = new StandardLM(ireader,indexId,field);
            Double prob = p.rate / rateSum;
            Map.Entry<Double,LanguageModel> e = new AbstractMap.SimpleEntry(prob,docSLM);
            prob_pref.add(e);
        }
        MixtureLM MLM = new MixtureLM(prob_pref);
        return MLM;
    }
}
