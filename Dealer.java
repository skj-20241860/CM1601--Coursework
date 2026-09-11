public class Dealer {
    private final String dealerId;
    private final String dealerName;
    private final String phoneNumber;
    private final String location;

    public Dealer(String dealerId, String dealerName, String phoneNumber, String location) {
        this.dealerId = dealerId;
        this.dealerName = dealerName;
        this.phoneNumber = phoneNumber;
        this.location = location;
    }

    public String getDealerId() { return dealerId; }
    public String getDealerName() { return dealerName; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getLocation() { return location; }
}
