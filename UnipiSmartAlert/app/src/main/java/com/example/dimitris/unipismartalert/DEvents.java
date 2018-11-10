package com.example.dimitris.unipismartalert;

//Κλαση για γεγονοτα
public class DEvents {
    private String DEvent,timestamp,Lat,Lon;

    public DEvents(String dEvent,String timestamp,String Lat,String Lon) {
        this.DEvent = dEvent; //Τυπος
        this.timestamp=timestamp; //Timestamp
        this.Lat=Lat;   //Latitude
        this.Lon=Lon;   //Longitude
    }

    public String getLat() {
        return Lat;
    }

    public String getLon() {
        return Lon;
    }

    public String getDEvent() {
        return this.DEvent;
    }

    public String getTimestamp() {
        return this.timestamp;
    }

    public String getString() {
        return "Time: "+this.timestamp+"\t|\tType: "+
                this.DEvent+"\t|\tLat: "+
                this.Lat+"\t|\tLon: "+
                this.Lon;
    }

}