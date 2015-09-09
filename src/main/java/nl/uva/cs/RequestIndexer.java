/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.uva.cs;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import nl.uva.lucenefacility.Indexer;
import static nl.uva.settings.Config.configFile;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

/**
 *
 * @author Mostafa Dehghani
 */
public class RequestIndexer extends Indexer {

    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(RequestIndexer.class.getName());
    public RequestIndexer() throws Exception, Throwable {
        super(configFile.getProperty("RINDEX_PATH"));
    }

    @Override
    protected void docIndexer() throws Exception {
        try {
            
        String inputProfiles = configFile.getProperty("REQS");
        String line = null;
        BufferedReader br = new BufferedReader(new FileReader(inputProfiles));
        RequestInfo reqInfo = new RequestInfo();
        while ((line = br.readLine()) != null) {
            reqInfo.setJson(line);
            Request r = reqInfo.GetRequest();
            this.IndexDoc(r);
        }
        } catch (Exception ex) {
            log.error(ex);
            throw ex;
        }
    }
    
    @Override
    protected void analyzerMapInitializer(Map<String, Analyzer> analyzerMap) {
//        analyzerMap.put("ID", new KeywordAnalyzer());//StandardAnalyzer(Version.LUCENE_CURRENT));
        analyzerMap.put("ID", new KeywordAnalyzer());//StandardAnalyzer(Version.LUCENE_CURRENT));
        analyzerMap.put("GROUP", new KeywordAnalyzer());//StandardAnalyzer(Version.LUCENE_CURRENT));
        analyzerMap.put("LOCATION", new KeywordAnalyzer());//StandardAnalyzer(Version.LUCENE_CURRENT));
        analyzerMap.put("TRIPTYPE", new KeywordAnalyzer());//StandardAnalyzer(Version.LUCENE_CURRENT));
        analyzerMap.put("SEASON", new KeywordAnalyzer());//StandardAnalyzer(Version.LUCENE_CURRENT));
        analyzerMap.put("DURATION", new KeywordAnalyzer());//StandardAnalyzer(Version.LUCENE_CURRENT));
        analyzerMap.put("PROFILEID", new KeywordAnalyzer());//StandardAnalyzer(Version.LUCENE_CURRENT));
    }

    @Override
    protected void IndexDoc(Object obj) throws Exception {
        Request r = (Request) obj;
        Document doc = new Document();
        doc.add(new Field("ID", r.reqId, Field.Store.YES, Field.Index.ANALYZED_NO_NORMS, Field.TermVector.YES));
        doc.add(new Field("GROUP", r.group, Field.Store.YES, Field.Index.ANALYZED_NO_NORMS, Field.TermVector.YES));
        doc.add(new Field("LOCATION", r.locationId, Field.Store.YES, Field.Index.ANALYZED_NO_NORMS, Field.TermVector.YES));
        doc.add(new Field("TRIPTYPE", r.tripType, Field.Store.YES, Field.Index.ANALYZED_NO_NORMS, Field.TermVector.YES));
        doc.add(new Field("SEASON", r.season, Field.Store.YES, Field.Index.ANALYZED_NO_NORMS, Field.TermVector.YES));
        doc.add(new Field("DURATION", r.duration, Field.Store.YES, Field.Index.ANALYZED_NO_NORMS, Field.TermVector.YES));
        doc.add(new Field("PROFILEID", r.pId, Field.Store.YES, Field.Index.ANALYZED_NO_NORMS, Field.TermVector.YES));
        try {
            writer.addDocument(doc);
        } catch (IOException ex) {
            log.error(ex);
        }
//        log.info("Douc number: " + docCount + " - Document " + r.reqId + " has been indexed successfully...");
//        docCount++;
//        if(docCount%10 ==0)
//            log.info(docCount);
            
    }
    public static void main(String[] args) throws Exception, Throwable {
        RequestIndexer ri = new RequestIndexer();
    }
}



