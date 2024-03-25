package com.myplex.myplex.utils;

import com.myplex.model.CarouselInfoData;

import java.util.List;

public interface OTPCallback {
   void onSuccess(String otp);

   void onFailed();
}
