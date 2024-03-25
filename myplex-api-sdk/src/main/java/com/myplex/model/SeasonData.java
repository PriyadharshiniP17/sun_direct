package com.myplex.model;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Srikanth on 14-Aug-17.
 */

public class SeasonData implements Serializable {
    public SeasonData() {
    }

    public String seasonName;
    public List<CardDownloadData> tvEpisodesList;
}
