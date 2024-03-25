package com.myplex.myplex.concurrentExecution;

import com.myplex.model.CarouselInfoData;

import java.util.List;

public interface Callback {
   void onComplete(List<CarouselInfoData> mListCarouselInfoData);

   void onFailed();
}
