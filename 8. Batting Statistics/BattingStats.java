public class BattingStats {

    public static void main(String[] args) {
        System.out.println("Welcome to the Batting Average Calculator");
        
        boolean continueApp = true;
        while (continueApp) {
            int numAtBats = BattingConsole.getValidInt("\nEnter number of times at bat: ", 1, 30);
            Calculator calc = new Calculator();
            calc.setAtBats(numAtBats);

            System.out.println("\n0 = out, 1 = single, 2 = double, 3 = triple, 4 = home run");
            for (int i = 0; i < numAtBats; i++) {
                int result = BattingConsole.getValidInt("Result for at-bat " + (i + 1) + ": ", 0, 4);
                calc.setAtBatResult(i, result);
            }

            System.out.printf("\nBatting average: %.3f", calc.calculateAverage());
            System.out.printf("\nSlugging percent: %.3f\n", calc.calculateSlugging());

            String response = ""; 
            while (true) {
                response = BattingConsole.getString("\nAnother Player? (y/n): ");
                if (response.equalsIgnoreCase("y") || response.equalsIgnoreCase("n")) {
                    break; 
                }
                System.out.println("Error! Please enter 'y' for yes or 'n' for no.");
            }

            if (response.equalsIgnoreCase("n")) {
                continueApp = false;
            } // Added missing closing brace for the if-statement
        } // Added missing closing brace for the while(continueApp) loop

        System.out.println("\nBye!");
    }
}
