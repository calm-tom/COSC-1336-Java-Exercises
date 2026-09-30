import java.util.Scanner;
import java.util.Random;

public class DiceRoller {

    private static final Random random = new Random();

    public static void main(String[] args) {
        System.out.println("Dice Roller");
        System.out.println();

        Scanner sc = new Scanner(System.in);

        String choice = getUserChoice(sc, "Roll the dice? (y/n): ");

        while (choice.equalsIgnoreCase("y")) {
            int die1 = rollDie();
            int die2 = rollDie();

            printDice(die1, die2);
            printSpecialMessage(die1 + die2);

            choice = getUserChoice(sc, "Roll again? (y/n): ");
        }

        System.out.println("Goodbye!");
    }

    private static int rollDie() {
        return random.nextInt(6) + 1;
    }

    private static void printDice(int die1, int die2) {
        System.out.println("Die 1: " + die1);
        System.out.println("Die 2: " + die2);
        System.out.println("Total: " + (die1 + die2));
    }

    private static void printSpecialMessage(int total) {
        switch (total) {
            case 2 -> System.out.println("Snake eyes!");
            case 12 -> System.out.println("Boxcars!");
            default -> System.out.println();
        }
    }

    private static String getUserChoice(Scanner sc, String prompt) {
        while (true) {
            System.out.print(prompt);
            String choice = sc.nextLine();

            if (choice.equalsIgnoreCase("y") || choice.equalsIgnoreCase("n")) {
                return choice;
            }

            System.out.println("Please enter Y or N.");
        }
    }
}
