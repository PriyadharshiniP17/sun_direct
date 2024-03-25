package com.myplex.model;

public class BitrateCapForPlayer {
    public HD_BITRATE hd_bitrate;
    public SD_BITRATE sd_bitrate;



    public class HD_BITRATE{
        public Auto auto;
        public Hd hd;
        public Medium medium;
        public Low low;
    }

    public class SD_BITRATE{
        public Auto auto;
        public Hd hd;
        public Medium medium;
        public Low low;
    }



    public  class  Auto {
        public int max;
        public  int min;
    }

    public  class  Hd {
        public int max;
        public  int min;
    }
    public  class  Medium {
        public int max;
        public  int min;
    }
    public  class  Low {
        public int max;
        public  int min;
    }
}
