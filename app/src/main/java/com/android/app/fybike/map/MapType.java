package com.android.app.fybike.map;

/**
 * Created by Thach Nguyen on 11/22/2016.
 */

public enum MapType {
    CAR_REPAIR("car_repair");

    private final String type;

    MapType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return type;
    }

}
