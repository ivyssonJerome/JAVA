import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

class User {
    private String name;
    private String id;
    private List<String> borrowedBicycles;

    public User(String name, String id) {
        this.name = name;
        this.id = id;
        this.borrowedBicycles = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public List<String> getBorrowedBicycles() {
        return borrowedBicycles;
    }
}

public class BicycleManagementSystem {
    private static String[][] bicycles = new String[0][4];
    private static List<User> users = new ArrayList<>();

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("\nMenu:");
            System.out.println("1. Add Bicycle");
            System.out.println("2. View All Bicycles");
            System.out.println("3. Borrow Bicycle");
            System.out.println("4. Return Bicycle");
            System.out.println("5. Display Borrowed Bicycles");
            System.out.println("6. Search Bicycle");
            System.out.println("7. Exit");
            System.out.print("Enter your choice: ");

            int choice;
            try {
                choice = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Invalid choice. Please enter a number.");
                continue;
            }

            switch (choice) {
                case 1:
                    addBicycle(scanner);
                    break;
                case 2:
                    viewBicycles();
                    break;
                case 3:
                    borrowBicycle(scanner);
                    break;
                case 4:
                    returnBicycle(scanner);
                    break;
                case 5:
                    displayBorrowed(scanner);
                    break;
                case 6:
                    searchBicycle(scanner);
                    break;
                case 7:
                    System.out.println("Exiting...");
                    scanner.close();
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private static void addBicycle(Scanner scanner) {
        System.out.println("Enter bicycle name:");
        String name = scanner.nextLine().trim();
        System.out.println("Enter bicycle make:");
        String make = scanner.nextLine().trim();
        System.out.println("Enter bicycle type:");
        String type = scanner.nextLine().trim();

        String[][] newBicycles = new String[bicycles.length + 1][4];
        System.arraycopy(bicycles, 0, newBicycles, 0, bicycles.length);
        newBicycles[newBicycles.length - 1] = new String[]{name, make, type, "Available"};
        bicycles = newBicycles;
        System.out.println("Bicycle added successfully.");
    }

    private static void viewBicycles() {
        if (bicycles.length == 0) {
            System.out.println("No bicycles available.");
            return;
        }
        System.out.println("Bicycles:");
        for (String[] bike : bicycles) {
            System.out.println("Name: " + bike[0] + ", Make: " + bike[1] + ", Type: " + bike[2] + ", Availability: " + bike[3]);
        }
    }

    private static User findUserById(String userId) {
        for (User user : users) {
            if (user.getId().equals(userId)) {
                return user;
            }
        }
        return null;
    }

    private static void borrowBicycle(Scanner scanner) {
        System.out.println("Enter your user ID:");
        String userId = scanner.nextLine().trim();
        System.out.println("Enter your name:");
        String userName = scanner.nextLine().trim();

        User user = findUserById(userId);
        if (user != null) {
            if (!user.getName().equals(userName)) {
                System.out.println("User ID already exists with a different name. Cannot proceed.");
                return;
            }
        } else {
            user = new User(userName, userId);
            users.add(user);
            System.out.println("New user created.");
        }

        if (user.getBorrowedBicycles().size() >= 2) {
            System.out.println("You have already borrowed the maximum of 2 bicycles.");
            return;
        }

        List<String> availableBikes = new ArrayList<>();
        for (String[] bike : bicycles) {
            if ("Available".equals(bike[3])) {
                availableBikes.add(bike[0]);
            }
        }

        if (availableBikes.isEmpty()) {
            System.out.println("No bicycles available to borrow.");
            return;
        }

        System.out.println("Available bicycles: " + String.join(", ", availableBikes));
        System.out.println("Enter the name of the bicycle you want to borrow:");
        String bikeName = scanner.nextLine().trim();

        boolean found = false;
        for (String[] bike : bicycles) {
            if (bike[0].equals(bikeName) && "Available".equals(bike[3])) {
                bike[3] = "Unavailable";
                user.getBorrowedBicycles().add(bikeName);
                System.out.println("Bicycle borrowed successfully.");
                found = true;
                break;
            }
        }

        if (!found) {
            System.out.println("Bicycle not found or not available.");
        }
    }

    private static void returnBicycle(Scanner scanner) {
        System.out.println("Enter your user ID:");
        String userId = scanner.nextLine().trim();

        User user = findUserById(userId);
        if (user == null) {
            System.out.println("User not found.");
            return;
        }

        List<String> borrowed = user.getBorrowedBicycles();
        if (borrowed.isEmpty()) {
            System.out.println("You have no bicycles to return.");
            return;
        }

        System.out.println("Your borrowed bicycles: " + String.join(", ", borrowed));
        System.out.println("Enter the name of the bicycle to return:");
        String bikeName = scanner.nextLine().trim();

        if (!borrowed.contains(bikeName)) {
            System.out.println("Bicycle not found in your borrowed list.");
            return;
        }

        boolean found = false;
        for (String[] bike : bicycles) {
            if (bike[0].equals(bikeName) && "Unavailable".equals(bike[3])) {
                bike[3] = "Available";
                found = true;
                break;
            }
        }

        if (found) {
            borrowed.remove(bikeName);
            System.out.println("Bicycle returned successfully.");
        } else {
            System.out.println("Bicycle not found or already available.");
        }
    }

    private static void displayBorrowed(Scanner scanner) {
        System.out.println("Enter your user ID:");
        String userId = scanner.nextLine().trim();

        User user = findUserById(userId);
        if (user == null) {
            System.out.println("User not found.");
            return;
        }

        List<String> borrowed = user.getBorrowedBicycles();
        if (borrowed.isEmpty()) {
            System.out.println("You have no borrowed bicycles.");
        } else {
            System.out.println("Borrowed bicycles:");
            for (String bikeName : borrowed) {
                System.out.println(bikeName);
            }
        }
    }

    private static void searchBicycle(Scanner scanner) {
        System.out.println("Enter search term:");
        String term = scanner.nextLine().trim().toLowerCase();

        List<String[]> results = new ArrayList<>();
        for (String[] bike : bicycles) {
            if (bike[0].toLowerCase().contains(term) ||
                bike[1].toLowerCase().contains(term) ||
                bike[2].toLowerCase().contains(term)) {
                results.add(bike);
            }
        }

        if (results.isEmpty()) {
            System.out.println("No bicycles found.");
        } else {
            System.out.println("Search results:");
            for (String[] bike : results) {
                System.out.println("Name: " + bike[0] + ", Make: " + bike[1] + ", Type: " + bike[2] + ", Availability: " + bike[3]);
            }
        }
    }
}
