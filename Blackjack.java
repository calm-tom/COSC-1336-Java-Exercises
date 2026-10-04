import java.util.Scanner;
import java.text.NumberFormat;

public class Blackjack {

    public static void main(String[] args) {
        System.out.println("BLACKJACK! \nBlackjack payout is 3:2");
        System.out.println();

        NumberFormat whole = NumberFormat.getInstance();

        double money = 100;
        System.out.println("Starting money: " + whole.format(money));
        System.out.println();

        Scanner sc = new Scanner(System.in);
        String choice = "y";

        while (choice.equalsIgnoreCase("y")) {
            double bet = playerBet(money, sc);

            String result = outcome();

            switch (result) {
                case "BLACKJACK":
                    money += bet * 1.5;
                    System.out.println("You got a Blackjack!");
                    break;
                case "WIN":
                    money += bet;
                    System.out.println("You won.");
                    break;
                case "PUSH":
                    System.out.println("Push. No money won or lost.");
                    break;
                case "LOSS":
                    money -= bet;
                    System.out.println("You lost.");
                    break;
            }

            System.out.println("Total money: " + formatCurrency(money));
            System.out.println();

            if (!houseRules(money)) {
                break;
            }
            choice = getPlayerChoice(sc, "Play again? (y/n): ");
            System.out.println();
        }

        System.out.println("Bye!");
    }

    private static String getPlayerChoice(Scanner sc, String prompt) {
        while (true) {
            System.out.print(prompt);
            String choice = sc.nextLine();

            if (choice.equalsIgnoreCase("y") || choice.equalsIgnoreCase("n")) {
                return choice;
            }

            System.out.println("Please enter Y or N.");
        }
    }

    private static String formatCurrency(double money) {
        return NumberFormat.getCurrencyInstance().format(money);
    }
    
    private static String outcome() {
        double r = Math.random();
        if (r < 0.05)          return "BLACKJACK";
        else if (r < 0.42)     return "WIN";
        else if (r < 0.51)     return "PUSH";
        else                  return "LOSS";
    }
    
        private static double playerBet(double money, Scanner sc) {
        final double MIN_BET = 5.0;
        final double MAX_BET = 1_000.0;

        while (true) {
            System.out.print("Bet amount: ");
            String line = sc.nextLine().trim();

            try {
                double bet = Double.parseDouble(line);

                if (bet < MIN_BET) {
                    System.out.println("Bet must be at least $" + (int) MIN_BET + ".");
                } else if (bet > MAX_BET) {
                    System.out.println("Bet cannot exceed $" + (int) MAX_BET + ".");
                } else if (bet > money) {
                    System.out.println("You don't have enough money for that bet.");
                } else {
                    return bet;
                }
            } catch (NumberFormatException e) {
                System.out.println("Please enter a numeric value.");
            }
        }
    }
    
    private static boolean houseRules(double money) {
        final double MIN_MONEY = 5.0;
        final double MAX_MONEY = 10_000.0;

        if (money <= MIN_MONEY) {
            System.out.println("You're out of cash, so get out of the Casino.");
            return false; 
        }
        if (money >= MAX_MONEY) {
            System.out.println("The Casino's owner doesn't like you taking all our cash! Game Over!");
            return false;
        }
        return true;
    }
        
}