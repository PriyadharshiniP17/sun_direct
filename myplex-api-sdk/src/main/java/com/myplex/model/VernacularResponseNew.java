package com.myplex.model;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Apalya on 9/19/2014.
 */
public class VernacularResponseNew extends BaseResponseData {

   public JsonObject language_strings = null;


   public class LanguageString {
      @SerializedName("Currently Playing on other channels")
      public CurrentlyPlayingOnOtherChannels currentlyPlayingOnOtherChannels;
      @SerializedName("Upcoming Programs")
      public UpcomingPrograms upcomingPrograms;
      @SerializedName("People also watched")
      public PeopleAlsoWatched peopleAlsoWatched;
      public Season Season;
      public More More;
   }

   public class More {

      private String hindi;
      private String english;


      public String getHindi() {
         return hindi;
      }

      public void setHindi(String hindi) {
         this.hindi = hindi;
      }

      public String getEnglish() {
         return english;
      }

      public void setEnglish(String english) {
         this.english = english;
      }


   }

   public class CurrentlyPlayingOnOtherChannels {

      private String hindi;
      private String english;

      public String getHindi() {
         return hindi;
      }

      public void setHindi(String hindi) {
         this.hindi = hindi;
      }

      public String getEnglish() {
         return english;
      }

      public void setEnglish(String english) {
         this.english = english;
      }
   }

   public class PeopleAlsoWatched {

      private String hindi;
      private String english;


      public String getHindi() {
         return hindi;
      }

      public void setHindi(String hindi) {
         this.hindi = hindi;
      }

      public String getEnglish() {
         return english;
      }

      public void setEnglish(String english) {
         this.english = english;
      }



   }

   public class Season {

      private String hindi;
      private String english;


      public String getHindi() {
         return hindi;
      }

      public void setHindi(String hindi) {
         this.hindi = hindi;
      }

      public String getEnglish() {
         return english;
      }

      public void setEnglish(String english) {
         this.english = english;
      }


   }

   public class UpcomingPrograms {

      private String hindi;
      private String english;

      public String getHindi() {
         return hindi;
      }

      public void setHindi(String hindi) {
         this.hindi = hindi;
      }

      public String getEnglish() {
         return english;
      }

      public void setEnglish(String english) {
         this.english = english;
      }

   }




   }
