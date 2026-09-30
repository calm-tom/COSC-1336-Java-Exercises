import java.util.Scanner;

public class DiceRoller {

    public static void main(String[] args) {
        System.out.println("Dice Roller");
        System.out.println();

        Scanner sc = new Scanner(System.in);
        String choice;

        while (true) {
            try {
                System.out.print("Roll the dice? (y/n): ");
                choice = sc.nextLine();

                if (!choice.equalsIgnoreCase("y") && !choice.equalsIgnoreCase("n")) {
                    throw new IllegalArgumentException();
                }

                break;
            } catch (IllegalArgumentException e) {
                System.out.println("Please enter Y or N.");
            }
    }

        while (choice.equalsIgnoreCase("y")) {
            int[] dice = rollDice();
            int total = dice[0] + dice[1];

            System.out.println("Die 1: " + dice[0]);
            System.out.println("Die 2: " + dice[1]);
            System.out.println("Total: " + total);

            bonusMessages(total);
            System.out.println();

            choice = getChoice(sc);
        }

        System.out.println("Goodbye!");
    }

    public static int[] rollDice() {
        int die1 = (int) (Math.random() * 6) + 1;
        int die2 = (int) (Math.random() * 6) + 1;
        return new int[] { die1, die2 };
    }

    public static void bonusMessages(int total) {
        if (total == 2) {
            System.out.println("Snake eyes!");
        } else if (total == 12) {
            System.out.println("Boxcars!");
        }
    }

    public static String getChoice(Scanner sc) {
        while (true) {
            System.out.print("Roll again? (y/n): ");
            String choice = sc.nextLine();

            if (choice.equalsIgnoreCase("y") || choice.equalsIgnoreCase("n")) {
                return choice;
            }

            System.out.println("Please enter Y or N.");
        }
    }
}
