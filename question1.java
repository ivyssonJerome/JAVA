import java.util.Scanner;

public class BicycleManagementSystem {

    // Multidimensional array for storing bicycles: [][0]=Name, [1]=Make, [2]=Type, [3]=Available ("yes"/"no")
    static String[][] bicycles = new String[100][4]; // Maximum 100 bicycles
    static int bicycleCount = 0;

    // Arrays to store user information
    static String[] userNames = new String[100];
    static String[] userIds = new String[100];
    static String[][] userBorrowed = new String[100][2]; // Max 2 borrowed bicycles per user
    static int userCount = 0;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int choice;

        do {
            // Menu
            System.out.println("\n--- E-Hailing Bicycle Management System ---");
            System.out.println("1. Add Bicycle");
            System.out.println("2. View Bicycles");
            System.out.println("3. Borrow Bicycle");
            System.out.println("4. Return Bicycle");
            System.out.println("5. Display Borrowed Bicycles");
            System.out.println("6. Search for a Bicycle");
            System.out.println("0. Exit");
            System.out.print("Enter your choice: ");
            choice = scanner.nextInt();
            scanner.nextLine(); // Clear buffer

            switch (choice) {
                case 1: addBicycle(scanner); break;
                case 2: viewBicycles(); break;
                case 3: borrowBicycle(scanner); break;
                case 4: returnBicycle(scanner); break;
                case 5: displayBorrowed(); break;
                case 6: searchBicycle(scanner); break;
                case 0: System.out.println("Goodbye!"); break;
                default: System.out.println("Invalid choice.");
            }
        } while (choice != 0);

        scanner.close();
    }

    // Function to add a bicycle
    static void addBicycle(Scanner scanner) {
        System.out.print("Enter Bicycle Name: ");
        String name = scanner.nextLine();
        System.out.print("Enter Make: ");
        String make = scanner.nextLine();
        System.out.print("Enter Type (e.g. Mountain, Road): ");
        String type = scanner.nextLine();

        bicycles[bicycleCount][0] = name;
        bicycles[bicycleCount][1] = make;
        bicycles[bicycleCount][2] = type;
        bicycles[bicycleCount][3] = "yes"; // Available by default
        bicycleCount++;

        System.out.println("Bicycle added successfully!");
    }

    // Function to view all bicycles
    static void viewBicycles() {
        System.out.println("\n--- Available Bicycles ---");
        for (int i = 0; i < bicycleCount; i++) {
            System.out.println((i + 1) + ". " + bicycles[i][0] + " | " + bicycles[i][1] + " | " + bicycles[i][2] + " | Available: " + bicycles[i][3]);
        }
    }

    // Function to borrow a bicycle
    static void borrowBicycle(Scanner scanner) {
        System.out.print("Enter User Name: ");
        String name = scanner.nextLine();
        System.out.print("Enter User ID: ");
        String id = scanner.nextLine();

        // Find or create user
        int userIndex = findUser(id);
        if (userIndex == -1) {
            userIndex = userCount;
            userNames[userCount] = name;
            userIds[userCount] = id;
            userCount++;
        }

        // Check if user already borrowed 2 bicycles
        if (userBorrowed[userIndex][0] != null && userBorrowed[userIndex][1] != null) {
            System.out.println("You have already borrowed 2 bicycles.");
            return;
        }

        viewBicycles();
        System.out.print("Enter Bicycle Number to Borrow: ");
        int bikeIndex = scanner.nextInt() - 1;
        scanner.nextLine(); // Clear buffer

        if (bikeIndex < 0 || bikeIndex >= bicycleCount || bicycles[bikeIndex][3].equals("no")) {
            System.out.println("Invalid selection or bicycle not available.");
        } else {
            // Assign bicycle
            if (userBorrowed[userIndex][0] == null)
                userBorrowed[userIndex][0] = bicycles[bikeIndex][0];
            else
                userBorrowed[userIndex][1] = bicycles[bikeIndex][0];

            bicycles[bikeIndex][3] = "no";
            System.out.println("Bicycle borrowed successfully!");
        }
    }

    // Function to return a bicycle
    static void returnBicycle(Scanner scanner) {
        System.out.print("Enter User ID: ");
        String id = scanner.nextLine();
        int userIndex = findUser(id);
        if (userIndex == -1) {
            System.out.println("User not found.");
            return;
        }

        System.out.println("Borrowed Bicycles:");
        for (int i = 0; i < 2; i++) {
            if (userBorrowed[userIndex][i] != null) {
                System.out.println((i + 1) + ". " + userBorrowed[userIndex][i]);
            }
        }

        System.out.print("Enter number of bicycle to return (1 or 2): ");
        int bikeSlot = scanner.nextInt() - 1;
        scanner.nextLine();

        if (bikeSlot >= 0 && bikeSlot < 2 && userBorrowed[userIndex][bikeSlot] != null) {
            String bikeName = userBorrowed[userIndex][bikeSlot];

            // Mark bicycle as available again
            for (int i = 0; i < bicycleCount; i++) {
                if (bicycles[i][0].equals(bikeName)) {
                    bicycles[i][3] = "yes";
                    break;
                }
            }

            userBorrowed[userIndex][bikeSlot] = null;
            System.out.println("Bicycle returned successfully!");
        } else {
            System.out.println("Invalid choice.");
        }
    }

    // Function to display all borrowed bicycles
    static void displayBorrowed() {
        System.out.println("\n--- Borrowed Bicycles ---");
        for (int i = 0; i < userCount; i++) {
            System.out.print(userNames[i] + " (" + userIds[i] + "): ");
            for (int j = 0; j < 2; j++) {
                if (userBorrowed[i][j] != null) {
                    System.out.print(userBorrowed[i][j] + " ");
                }
            }
            System.out.println();
        }
    }

    // Function to search for a bicycle by name
    static void searchBicycle(Scanner scanner) {
        System.out.print("Enter Bicycle Name to Search: ");
        String name = scanner.nextLine();

        boolean found = false;
        for (int i = 0; i < bicycleCount; i++) {
            if (bicycles[i][0].equalsIgnoreCase(name)) {
                System.out.println("Found: " + bicycles[i][0] + " | " + bicycles[i][1] + " | " + bicycles[i][2] + " | Available: " + bicycles[i][3]);
                found = true;
            }
        }

        if (!found) {
            System.out.println("Bicycle not found.");
        }
    }

    // Helper method to find a user by ID
    static int findUser(String id) {
        for (int i = 0; i < userCount; i++) {
            if (userIds[i].equals(id)) {
                return i;
            }
        }
        return -1;
    }
}
