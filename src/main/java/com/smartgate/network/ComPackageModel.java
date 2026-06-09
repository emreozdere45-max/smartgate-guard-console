package com.smartgate.network;

public class ComPackageModel {
    private int ope_type;
    private boolean isNeedResponse;
    private int dataInt;
    private String dataString;
    private SecurityInfo guvenlik;

    public int getOpe_type() {
        return ope_type;
    }

    public void setOpe_type(int ope_type) {
        this.ope_type = ope_type;
    }

    public boolean isNeedResponse() {
        return isNeedResponse;
    }

    public void setNeedResponse(boolean needResponse) {
        isNeedResponse = needResponse;
    }

    public int getDataInt() {
        return dataInt;
    }

    public void setDataInt(int dataInt) {
        this.dataInt = dataInt;
    }

    public String getDataString() {
        return dataString;
    }

    public void setDataString(String dataString) {
        this.dataString = dataString;
    }

    public SecurityInfo getGuvenlik() {
        return guvenlik;
    }

    public void setGuvenlik(SecurityInfo guvenlik) {
        this.guvenlik = guvenlik;
    }

    public static class SecurityInfo {
        private int guvenlikNo;
        private String ip;

        public SecurityInfo(int guvenlikNo, String ip) {
            this.guvenlikNo = guvenlikNo;
            this.ip = ip;
        }

        public int getGuvenlikNo() {
            return guvenlikNo;
        }

        public String getIp() {
            return ip;
        }
    }
}

