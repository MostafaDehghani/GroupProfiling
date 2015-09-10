/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package nl.uva.gp;

import nl.uva.generalinzedlm.*;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import nl.uva.lucenefacility.IndexInfo;
import static nl.uva.settings.Config.configFile;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.FSDirectory;

/**
 *
 * @author Mostafa Dehghani
 */
public class CalculateGLM {
    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(CalculateGLM.class.getName());
    
    public static void main(String[] args) throws IOException {
        String indexPathString = configFile.getProperty("RINDEX_PATH");
        Path ipath = FileSystems.getDefault().getPath(indexPathString);
        IndexReader ireader = DirectoryReader.open(FSDirectory.open(ipath));
        IndexInfo iInfo = new IndexInfo(ireader);
        ArrayList<Integer> docsList = iInfo.getDocsContainingTerm("SEASON", "Spring");

        DocsGroup dGroup = new DocsGroup(ireader, "TEXT", docsList);
//        System.out.println("CLM" + dGroup.getCollectionLM().getTopK(20));
//        System.out.println("CLM:" + dGroup.getCollectionLM().getSize());
//        System.out.println("STLM" + dGroup.getGroupStandardLM().getTopK(20));
//        System.out.println("STLM:" + dGroup.getGroupStandardLM().getSize());
//        System.out.println("SPLM" + dGroup.getGroupSpecificLM().getTopK(20));
//        System.out.println("SPLM:" + dGroup.getGroupSpecificLM().getSize());
//        System.out.println("PLM" + dGroup.getGroupParsimoniouseLM().getTopK(20));
//        System.out.println("PLM:" + dGroup.getGroupParsimoniouseLM().getSize());
        System.out.println("GLM" + dGroup.getGroupGeneralizedLM().getTopK(20));   
        System.out.println("GLM:" + dGroup.getGroupGeneralizedLM().getSize());   
//        System.out.println("HGLM" + dGroup.getGroupHGeneralizedLM().getTopK(20));   
//        System.out.println("HGLM:" + dGroup.getGroupHGeneralizedLM().getSize());   
    }

}
