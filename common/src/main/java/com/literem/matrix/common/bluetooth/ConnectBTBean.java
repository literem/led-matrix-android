package com.literem.matrix.common.bluetooth;

class ConnectBTBean {
    private String deviceName;
    private String deviceMac;
    private boolean isConnect;

    String getDeviceName() {
        return deviceName;
    }

    void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    String getDeviceMac() {
        return deviceMac;
    }

    void setDeviceMac(String deviceMac) {
        this.deviceMac = deviceMac;
    }

    boolean isConnect() {
        return isConnect;
    }

    void setConnect(boolean connect) {
        isConnect = connect;
    }
}
