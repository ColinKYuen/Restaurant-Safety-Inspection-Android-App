package com.example.projectiteration1.model;

import java.util.ArrayList;

/**
 * Restaurant class models the information about a
 * single restaurant. Data includes tracking number,
 * restaurant name, address, city, facility type,
 * GPS coordinates, and all inspection reports for a
 * restaurant.
 */
public class Restaurant {
    // Read from a CSV
    private String trackingNumber;
    private String resName;
    private String address;
    private String city;
    private String facType;
    private String latitude;
    private String longitude;
    private int imgID;
    private boolean isFav;

    // The all reports corresponding to a particular restaurant
    private ArrayList<InspectionReport> inspectionReports = new ArrayList<>();

    public String getTrackingNumber() {
        return trackingNumber;
    }

    public void setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }

    public String getResName() {
        return resName;
    }

    public void setResName(String resName) {
        this.resName = resName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String adress) {
        this.address = adress;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getFacType() {
        return facType;
    }

    public void setFacType(String facType) {
        this.facType = facType;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public ArrayList<InspectionReport> getInspectionReports() {
        return inspectionReports;
    }

    public void setInspectionReports(ArrayList<InspectionReport> inspectionReports) {
        this.inspectionReports = inspectionReports;
    }

    public int getImg(){
        return imgID;
    }

    public void setImg(int imgID){
        this.imgID = imgID;
    }

    public boolean getFav(){
        return isFav;
    }

    public void setFav(boolean newVal){
        isFav = newVal;
    }


    public Restaurant clone(){
        Restaurant ret = new Restaurant();

        ret.trackingNumber = this.trackingNumber;
        ret.resName = this.resName;
        ret.address = this.address;
        ret.city = this.city;
        ret.facType = this.facType;
        ret.latitude = this.latitude;
        ret.longitude = this.longitude;
        ret.imgID = this.imgID;
        ret.inspectionReports = this.inspectionReports;
        ret.isFav = this.isFav;

        return ret;
    }

    // For debugging purposes
    @Override
    public String toString() {
        return "Restaurant{" +
                "trackingNumber='" + trackingNumber + '\'' +
                ", isFav=" + isFav +
                ", resName='" + resName + '\'' +
                ", address='" + address + '\'' +
                ", city='" + city + '\'' +
                ", facType='" + facType + '\'' +
                ", latitude='" + latitude + '\'' +
                ", longitude='" + longitude + '\'' +
                ", inspectionReports=" + inspectionReports +
                '}';
    }
}
