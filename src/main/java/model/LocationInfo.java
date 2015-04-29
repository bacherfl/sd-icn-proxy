package model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by florian on 29.04.15.
 */
public class LocationInfo {

    private List<String> locations;

    public LocationInfo() {
        locations = new ArrayList<>();
    }

    public List<String> getLocations() {
        return locations;
    }

    public void setLocations(List<String> locations) {
        this.locations = locations;
    }
}
