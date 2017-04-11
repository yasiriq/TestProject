package com.speedautosystems.firebaseproject;

/**
 * Created by Yasir on 2/13/2017.
 */
public class User {

    public String name;
    public String email;
    public String loc;
    public double lat;
    public double lon;

    public User(){}

    public User(String name, String email, String loc) {
        this.name = name;
        this.email = email;
        this.loc = loc;
    }

    public User(String name, String email, double lat,double lon) {
        this.name = name;
        this.email = email;
        this.lat=lat;
        this.lon=lon;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getLoc() {
        return loc;
    }

    public void setLoc(String loc) {
        this.loc = loc;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }
}
