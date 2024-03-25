package com.myplex.myplex.analytics.partners.chromecast.model;



public class HooqChromecastEvent  {
    public DeviceSegParam proxyDeviceSegment;

    public HooqChromecastEvent() {

        proxyDeviceSegment = new DeviceSegParam();
    }

    public void setProxiedDeviceID(String proxyDeviceID) {
        proxyDeviceSegment.setProxiedDeviceID(proxyDeviceID);
    }

    /**
     * Device segment.
     */
    private static class DeviceSegParam {
        private String deviceName = "chromeCast";
        private String deviceType = "proxyClient";
        private String uniqueDeviceID;

        void setProxiedDeviceID(String deviceID) {
            this.uniqueDeviceID = deviceID;
        }
    }
}