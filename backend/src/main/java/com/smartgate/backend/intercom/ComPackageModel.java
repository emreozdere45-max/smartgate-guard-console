package com.smartgate.backend.intercom;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ComPackageModel {
    @JsonProperty("ope_type")
    private int ope_type;

    @JsonProperty("isNeedResponse")
    private boolean isNeedResponse;

    @JsonProperty("dataInt")
    private int dataInt;

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
}
