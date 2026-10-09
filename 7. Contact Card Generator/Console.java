import java.util.Scanner;
import java.util.regex.Pattern; //Regex is an easy way to validate input for this application. Used it once before.

class Console {
    private static Scanner sc = new Scanner(System.in);

    public static String getString(String prompt) {
        System.out.print(prompt);
        return sc.nextLine();
    }

    // Regex ensures name contains only letters and is not numeric
    public static String getValidatedName(String prompt) {
        while (true) {
            String input = getString(prompt);
            if (input.matches("[a-zA-Z\\s]+")) {
                return input;
            }
            System.out.println("Error! Names must contain only alphabetic characters.\n");
        }
    }

    // Regex requires @ and specific suffixes. This is perhaps too restrictive, oh well for AI startups I guess.
    public static String getValidatedEmail(String prompt) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)\\.(com|net|edu|gov|org)$";
        while (true) {
            String input = getString(prompt);
            if (Pattern.compile(emailRegex).matcher(input).matches()) {
                return input;
            }
            System.out.println("Error! Please enter a valid email with a supported suffix (.com, .net, .edu, .gov, .org).\n");
        }
    }

    // I also wanted to gently force a valid phone number using a variety of formats. 
    // This works, but I don't love it. Gemma's idea. Is there a better tool than Regex for this purpose specifically?
    // Limited business to explicit "US phone number" because Regex is too complex/rigid for this purpose...
    public static String getValidUSPhone(String prompt) {
        // Regex allows: 1234567890, 123.456.7890, (123) 456-7890, 123-456-7890
        String phoneRegex = "^(\\d{10}|\\d{3}\\.\\d{3}\\.\\d{4}|\\(\\d{3}\\) \\d{3}-\\d{4}|\\d{3}-\\d{3}-\\d{4})$";
        while (true) {
            String input = getString(prompt);
            if (Pattern.compile(phoneRegex).matcher(input).matches()) {
                return input;
            }
            System.out.println("Error! Invalid phone format. Please use a valid US phone number.\n");
        }
    }
}