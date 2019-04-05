package com.example.technician.mulikamwizi.model;

import java.io.Serializable;



public class LocationAlertPojo implements Serializable {
    String fullAddress;
    String longitude;
    String latitude;
    String rootcause;

    public String getFullAddress() {
        return fullAddress;
    }

    public void setFullAddress(String fullAddress) {
        this.fullAddress = fullAddress;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getRootcause() {
        return rootcause;
    }

    public void setRootcause(String rootcause) {
        this.rootcause = rootcause;
    }
}
