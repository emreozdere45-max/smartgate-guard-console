package com.smartgate.network;

import java.util.List;

public class ComPackageModel {
    private int ope_type;
    private boolean isNeedResponse;
    private int dataInt;
    private String dataString;
    private SecurityInfo guvenlik;
    private List<DaireInfo> daires;
    private List<Object> guvenliks;

    public static class DaireInfo {
        public double id;
        public double daireNo;
        public double blok;
        public String ip;
        public String isim;
        public String soyisim;
        public String katNo;

        public String getIp() { return ip; }
        public int getDaireNo() { return (int) daireNo; }
        public int getBlok() { return (int) blok; }
        public String getKatNo() { return katNo; }
    }

    public static class SecurityInfo {
        private int guvenlikNo;
        private String ip;

        public SecurityInfo(int guvenlikNo, String ip) {
            this.guvenlikNo = guvenlikNo;
            this.ip = ip;
        }

        public int getGuvenlikNo() { return guvenlikNo; }
        public String getIp() { return ip; }
    }

    public int getOpe_type() { return ope_type; }
    public void setOpe_type(int ope_type) { this.ope_type = ope_type; }

    public boolean isNeedResponse() { return isNeedResponse; }
    public void setNeedResponse(boolean needResponse) { isNeedResponse = needResponse; }

    public int getDataInt() { return dataInt; }
    public void setDataInt(int dataInt) { this.dataInt = dataInt; }

    public String getDataString() { return dataString; }
    public void setDataString(String dataString) { this.dataString = dataString; }

    public SecurityInfo getGuvenlik() { return guvenlik; }
    public void setGuvenlik(SecurityInfo guvenlik) { this.guvenlik = guvenlik; }

    public List<DaireInfo> getDaires() { return daires; }
    public void setDaires(List<DaireInfo> daires) { this.daires = daires; }

    public List<Object> getGuvenliks() { return guvenliks; }
    public void setGuvenliks(List<Object> guvenliks) { this.guvenliks = guvenliks; }

    public static class ZilPanelInfo {
        public String ip;
        public double blok;
        public double kapiNo;
        public String deviceName;

        public String getIp() { return ip; }
        public int getBlok() { return (int) blok; }
        public int getKapiNo() { return (int) kapiNo; }
        public String getDeviceName() { return deviceName; }
    }

    private ZilPanelInfo zilPanel;
    private java.util.List<ZilPanelInfo> zilPanels;

    public ZilPanelInfo getZilPanel() { return zilPanel; }
    public void setZilPanel(ZilPanelInfo zilPanel) { this.zilPanel = zilPanel; }
    public java.util.List<ZilPanelInfo> getZilPanels() { return zilPanels; }
}