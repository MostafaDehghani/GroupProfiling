/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.uva.gp;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Map;
import nl.uva.lucenefacility.IndexInfo;
import nl.uva.lucenefacility.Indexer;
import static nl.uva.settings.Config.configFile;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.FSDirectory;

/**
 *
 * @author Mostafa Dehghani
 */
public class RequestIndexer extends Indexer {

    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(RequestIndexer.class.getName());

    String docIndexPathString;
    Path docIpath;
    IndexReader docIreader;
    IndexInfo docInInfo;

    public RequestIndexer() throws Exception, Throwable {
        super(configFile.getProperty("RINDEX_PATH"));
    }

    @Override
    protected void docIndexer() throws Exception {
        docIndexPathString = configFile.getProperty("INDEX_PATH");
        docIpath = FileSystems.getDefault().getPath(docIndexPathString);
        docIreader = DirectoryReader.open(FSDirectory.open(docIpath));
        docInInfo = new IndexInfo(docIreader);
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
        StringBuilder canSB = new StringBuilder();
        for (RatedDoc c : r.ratedCandidates) {
            Integer iID = docInInfo.getIndexId(c.docID);
            Integer coef = 0;
            if (c.rate == 4D) {
                coef = 2;
            }
            if (c.rate == 3D) {
                coef = 1;
            } else {
                continue;
            }
            for (int i = 0; i < coef; i++) {
                canSB.append(docIreader.document(iID).get("TEXT"));
                canSB.append("\n");
            }
        }
        doc.add(new Field("TEXT", canSB.toString(), Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.WITH_POSITIONS));

        StringBuilder prefSB = new StringBuilder();
        for (RatedDoc p : r.ratedPrefrences) {
            Integer iID = docInInfo.getIndexId(p.docID);
            Integer coef = 0;
            if (p.rate == 4D) {
                coef = 2;
            }
            if (p.rate == 3D) {
                coef = 1;
            } else {
                continue;
            }
            for (int i = 0; i < coef; i++) {
                prefSB.append(docIreader.document(iID).get("TEXT"));
                prefSB.append("\n");
            }
        }
        doc.add(new Field("PTEXT", prefSB.toString(), Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.WITH_POSITIONS));

        try {
            writer.addDocument(doc);
        } catch (IOException ex) {
            log.error(ex);
        }
        log.info("Douc number: " + docCount + " - Document " + r.reqId + " has been indexed successfully...");
        docCount++;
//        if(docCount%10 ==0)
//            log.info(docCount);

    }

    public static void main(String[] args) throws Exception, Throwable {
        RequestIndexer ri = new RequestIndexer();
    }
}
