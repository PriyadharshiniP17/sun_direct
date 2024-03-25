
package com.myplex.myplex.model;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class Language {

    @SerializedName("terms")
    @Expose
    private List<Term> terms = null;

    public List<Term> getTerms() {
        return terms;
    }

    public void setTerms(List<Term> terms) {
        this.terms = terms;
    }

}
