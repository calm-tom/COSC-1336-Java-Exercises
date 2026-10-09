public class ContactList {
    public static void main(String[] args) {
        System.out.println("Welcome to the Contact List application");

        boolean continueApp = true;
        while (continueApp) {
            Contact contact = new Contact();

            contact.setFirstName(Console.getValidatedName("\nEnter first name: "));
            contact.setLastName(Console.getValidatedName("Enter last name: "));
            contact.setEmail(Console.getValidatedEmail("Enter email: "));
            contact.setPhone(Console.getValidUSPhone("Enter phone: "));

            System.out.println("\n" + contact.displayContact());

            //Gemma had an interesting way to validate the core loop, and I like keeping this class readable.
            //Plus, it's nice for a change of pace after doing the same loop in every assignment.
            String response = "";
            while (true) {
                response = Console.getString("\nContinue? (y/n): ");
                if (response.equalsIgnoreCase("y") || response.equalsIgnoreCase("n")) {
                    break; 
                }
                System.out.println("Error! Please enter 'y' for yes or 'n' for no.");
            }

            if (response.equalsIgnoreCase("n")) {
                continueApp = false;
            }
        }
    }
}