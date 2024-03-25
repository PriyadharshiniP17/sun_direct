package com.myplex.myplex.ui.fragment.epg;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
/*import com.yupptv.ott.epg.domain.EPGChannel;
import com.yupptv.ott.epg.domain.EPGEvent;*/

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Created by Kristoffer on 15-05-24.
 */
public class MockDataService {

    private static Random rand = new Random();
    private static List<Integer> availableEventLength = Lists.newArrayList(
            1000*60*15,  // 15 minutes
            1000*60*30,  // 30 minutes
            1000*60*45,  // 45 minutes
            1000*60*60,  // 60 minutes
            1000*60*120  // 120 minutes
    );

    private static List<String> availableEventTitles = Lists.newArrayList(
            "Avengers",
            "How I Met Your Mother",
            "Silicon Valley",
            "Late Night with Jimmy Fallon",
            "The Big Bang Theory",
            "Leon",
            "Die Hard"
    );

    private static List<String> availableChannelLogos = Lists.newArrayList(
            "https://aastha.akamaized.net/ondemand/vedic/katha/bhagwad_katha/devi_chitralekha_ji/devi_chitrlekha_ji_kandivali_day_03/116996/images/97938_00047.jpg"
    ,"https://aastha.akamaized.net/ondemand/vedic/katha/bhagwad_katha/sanjeev_krishna_thakur_neem_ka_thana/sanjeev_krishan_thakur_ji_neem_ka_thana_rajasthan_day_04/117187/images/98076_00059.jpg"
   ,"https://aastha.akamaized.net/ondemand/vadic/katha/gau_katha/shree_gopal_mani_ji/gau_katha_gopalmani_ji_day_04/105000/images/86559_00063.jpg"
    ,"https://aastha.akamaized.net/ondemand/vadic/katha/gau_katha/shree_gopal_mani_ji/gau_katha_gopalmani_ji_day_02/104869/images/86443_00047.jpg",
            "https://d229kpbsb5jevy.cloudfront.net/aastha/320/280/content/common/channel/logos/vedic-new.png"
    );

   /* public static Map<EPGChannel, List<EPGEvent>> getMockData() {
        HashMap<EPGChannel, List<EPGEvent>> result = Maps.newLinkedHashMap();

        long nowMillis = System.currentTimeMillis();

        for (int i=0 ; i < 60 ; i++) {
            EPGChannel epgChannel = new EPGChannel(availableChannelLogos.get(i % 5),
                    "Channel " + (i+1), Integer.toString(i));

            result.put(epgChannel, createEvents(epgChannel, nowMillis));
        }

        return result;
    }

    private static List<EPGEvent> createEvents(EPGChannel epgChannel, long nowMillis) {
        List<EPGEvent> result = Lists.newArrayList();

        long epgStart = nowMillis - EPGView.DAYS_BACK_MILLIS;
        long epgEnd = nowMillis + EPGView.DAYS_FORWARD_MILLIS;

        long currentTime = epgStart;

        while (currentTime <= epgEnd) {
            long eventEnd = getEventEnd(currentTime);
            EPGEvent epgEvent = new EPGEvent(currentTime, eventEnd, availableEventTitles.get(randomBetween(0, 6)));
            result.add(epgEvent);
            currentTime = eventEnd;
        }

        return result;
    }*/

    private static long getEventEnd(long eventStartMillis) {
        long length = availableEventLength.get(randomBetween(0,4));
        return eventStartMillis + length;
    }

    private static int randomBetween(int start, int end) {
        return start + rand.nextInt((end - start) + 1);
    }
}
