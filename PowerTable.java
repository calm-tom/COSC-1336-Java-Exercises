import java.util.Scanner;

public class PowerTable {

    public static void main(String[] args) {
        System.out.println("Welcome to the Squares and Cubes table\n");
        Scanner sc = new Scanner(System.in);
        
        String choice = "y";
        while (choice.equalsIgnoreCase("y")) {
            System.out.print("Enter an integer: ");
            int limit = Integer.parseInt(sc.nextLine());
            System.out.println();
            
            System.out.printf("%-8s%-8s%-8s%n", "Number", "Squared", "Cubed");
            System.out.printf("%-8s%-8s%-8s%n", "======", "=======", "=====");
            for (int i = 1; i <= limit; i++) {
            System.out.printf("%-8d%-8d%-8d%n", i, i * i, i * i * i);
            }
        System.out.print("Countinue? (y/n): ");
        choice = sc.nextLine();
        System.out.println();
        }
    }
}
