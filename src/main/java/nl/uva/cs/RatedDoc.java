/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package nl.uva.cs;

import java.util.HashSet;
import java.util.Objects;
import nl.uva.lm.LanguageModel;

/**
 *
 * @author Mostafa Dehghani
 */
public class RatedDoc {
    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(RatedDoc.class.getName());
    public String docID;
    public Double rate;
    public HashSet<String> tags;
    public LanguageModel docSLM;

    public RatedDoc(String docID, Double rate) {
        this.docID = docID;
        this.rate = rate;
    }

    public RatedDoc(String docID, Double rate, HashSet<String> tags) {
        this.docID = docID;
        this.rate = rate;
        if(tags != null)
            this.tags = tags;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(this.docID);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final RatedDoc other = (RatedDoc) obj;
        if (!Objects.equals(this.docID, other.docID)) {
            return false;
        }
        return true;
    }
    
    public void setNewRate(Double newRate){
        this.rate = newRate;
    }

    
    
}
