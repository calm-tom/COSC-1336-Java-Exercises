import java.util.Scanner;

public class BattingConsole {
    private static Scanner sc = new Scanner(System.in);

    public static String getString(String prompt) {
        System.out.print(prompt);
        return sc.nextLine();
    }

    public static int getValidInt(String prompt, int min, int max) {
        int value;
        while (true) {
            System.out.print(prompt);
            try {
                value = Integer.parseInt(sc.nextLine());
                if (value >= min && value <= max) {
                    return value;
                }
                System.out.println("Error: Please enter a number between " + min + " and " + max + ".");
            } catch (NumberFormatException e) {
                System.out.println("Error: Invalid input. Please enter an integer.");
            }
        }
    }
}
