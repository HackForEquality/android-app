package ie.yesequality.yesequality;

/**
 * Class to represent a single Polling Station
 */
public class PollingStation {
    private float latitude = 0.0F;
    private float longitude = 0.0F;
    private String name = "name";
    private String address = "address";
    private static String openingHours = "Opening Hours"; //are these the same for all Polling Stations?

    public PollingStation(float latitude, float longitude, String name, String address) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.name = name;
        this.address = address;
    }

    public float getLatitude() {
        return latitude;
    }

    public float getLongitude() {
        return longitude;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public static String getOpeningHours() {
        return openingHours;
    }

    /**
     * Gives you lat + long comma separated
     *
     *
     * @param latitude
     * @param longitude
     * @return
     */
    private String buildLatLong(String latitude, String longitude){
        return (latitude +","+ longitude);
    }


    @Override
    public String toString() {
        return "PollingStation{" +
                "latitude=" + latitude +
                ", longitude=" + longitude +
                ", name='" + name + '\'' +
                ", address='" + address + '\'' +
                '}';
    }
}
