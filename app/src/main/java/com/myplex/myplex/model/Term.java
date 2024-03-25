
package com.myplex.myplex.model;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class Term {

    @SerializedName("term")
    @Expose
    private String term;
    @SerializedName("image")
    @Expose
    private String image;
    @SerializedName("humanReadable")
    @Expose
    private String humanReadable;
    @SerializedName("translatedText")
    @Expose
    private String translatedText;
    @SerializedName("selectedImage")
    @Expose
    private String selectedImage;

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getHumanReadable() {
        return humanReadable;
    }

    public void setHumanReadable(String humanReadable) {
        this.humanReadable = humanReadable;
    }

    public String getTranslatedText() {
        return translatedText;
    }

    public void setTranslatedText(String translatedText) {
        this.translatedText = translatedText;
    }

    public String getSelectedImage() {
        return selectedImage;
    }

    public void setSelectedImage(String selectedImage) {
        this.selectedImage = selectedImage;
    }

}
