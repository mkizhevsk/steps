package com.mk.steps.data;

import com.google.gson.annotations.SerializedName;

public class Weather {

    @SerializedName("visibility")
    private int visibility;

    @SerializedName("main")
    private Main main;

    public Weather() {
    }

    public int getVisibility() {
        return visibility;
    }

    public void setVisibility(int visibility) {
        this.visibility = visibility;
    }

    public Main getMain() {
        return main;
    }

    public void setMain(Main main) {
        this.main = main;
    }
}
