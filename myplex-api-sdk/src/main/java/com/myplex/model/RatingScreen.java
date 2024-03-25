package com.myplex.model;

import java.io.Serializable;
import java.util.List;

public class RatingScreen implements Serializable {
    public String rating_popup_enabled;
    public String rating_reset_enabled;
    public String intervalas;
    public int recurring_interval;
    public int min_rating_for_store_page;
    public List<String> feedback_options;
    public String rate_pstv_lbl;
    public String rate_ngtv_lbl;
    public String fedback_pstv_lbl;
    public String feedback_ngtv_lbl;
    public int text_char_limit;
    public String rate_not_submitted;
    public String feedback_not_submitted;
    public String rating_scrn_text1;
    public String rating_scrn_text2;
    public String feedback_txt_box_text;
    public String feedback_screen_text1;
    public String feedback_screen_text2;
    public String feedback_submitted;
}
