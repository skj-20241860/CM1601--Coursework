import java.util.ArrayList;
import java.util.Random;

public class DealerManager {
    private final ArrayList<Dealer> dealers;

    public DealerManager(ArrayList<Dealer> dealers) {
        this.dealers = dealers == null ? new ArrayList<>() : dealers;
    }

    public void viewAllDealers() {
        if (dealers.isEmpty()) {
            System.out.println("No dealer records found.");
            return;
        }

        for (Dealer dealer : dealers) {
            printDealer(dealer);
        }
    }

    public void selectRandomFourDealers() {
        if (dealers.size() < 4) {
            System.out.println("At least four unique dealers are required.");
            return;
        }

        ArrayList<Dealer> selected = new ArrayList<>();
        Random random = new Random();

        while (selected.size() < 4) {
            Dealer candidate = dealers.get(random.nextInt(dealers.size()));
            boolean duplicate = false;

            for (Dealer dealer : selected) {
                if (dealer.getDealerId().equalsIgnoreCase(candidate.getDealerId())) {
                    duplicate = true;
                    break;
                }
            }

            if (!duplicate) selected.add(candidate);
        }

        sortByLocation(selected);

        System.out.println("\n===== RANDOM FOUR DEALERS =====");
        for (Dealer dealer : selected) {
            printDealer(dealer);
        }
    }

    private void sortByLocation(ArrayList<Dealer> list) {
        for (int i = 0; i < list.size() - 1; i++) {
            for (int j = 0; j < list.size() - i - 1; j++) {
                Dealer first = list.get(j);
                Dealer second = list.get(j + 1);

                if (first.getLocation().compareToIgnoreCase(second.getLocation()) > 0) {
                    list.set(j, second);
                    list.set(j + 1, first);
                }
            }
        }
    }

    private void printDealer(Dealer dealer) {
        System.out.println("----------------------------------------");
        System.out.println("Dealer ID   : " + dealer.getDealerId());
        System.out.println("Dealer Name : " + dealer.getDealerName());
        System.out.println("Phone       : " + dealer.getPhoneNumber());
        System.out.println("Location    : " + dealer.getLocation());
    }
}
