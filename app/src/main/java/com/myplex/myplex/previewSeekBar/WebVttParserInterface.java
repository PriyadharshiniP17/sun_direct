package com.myplex.myplex.previewSeekBar;

import java.util.HashSet;

public interface WebVttParserInterface {
    void webVttParserComplete(HashSet<String> thumbnailsUrl);
    void webVttParserFailed();
}
