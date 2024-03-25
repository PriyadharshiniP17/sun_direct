package com.myplex.myplex.previewSeekBar;

import java.io.File;

public class WebVTTParser {
    public WebVTTModule webVTTModule=new WebVTTModule();
    private File file=null;
    private WebVttParserInterface completeListener;


    public WebVTTParser(File file){
        this.file=file;
    }

    public void init(){
        if(file!=null&&webVTTModule.isEmpty()){
            Download download=new Download(file, webVTTModule,completeListener);
            download.execute((Void)null);
        }
    }

    public boolean isReady(){
        return webVTTModule.isReady();
    }

    public void setOnCompleteListener(WebVttParserInterface completeListener){
        this.completeListener = completeListener;
    }

    public String getText(){
        if(webVTTModule.isReady()){
            return webVTTModule.getWebVTTData().getText();
        }
        return null;
    }
    public WebVTTData getWebVTTData(){
        if(webVTTModule.isReady()){
            return webVTTModule.getWebVTTData();
        }
        return null;
    }

    public String getText(long time){
        if(webVTTModule.isReady()){
            webVTTModule.updateToStartInTime(time);
            return getText();
        }
        return null;
    }
    public WebVTTData getWebVTTData(long time){
        if(webVTTModule.isReady()){
            webVTTModule.updateToStartInTime(time);
            return getWebVTTData();
        }
        return null;
    }
}
