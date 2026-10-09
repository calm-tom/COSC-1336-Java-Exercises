package stureg;

import java.util.Scanner;

public class studentRegistration {

    public static void main(String[] args) {
        System.out.println("Student Registration Form");
        System.out.println();
        
        Scanner sc = new Scanner(System.in);
        
        System.out.print("Enter first name:   ");
        String fninput = sc.nextLine();
        
        System.out.print("Enter last name:   ");
        String lninput = sc.nextLine();
        
        System.out.print("Enter year of birth:   ");
        int yinput = Integer.parseInt(sc.nextLine());
        
        System.out.println();
        
        String fullNmMsg = "\n"
            + "Welcome " + fninput + " " + lninput + "\n";
        System.out.println(fullNmMsg);
        System.out.println("Your registration is now complete.");
        
        String tempPassMsg = "\n"
            + "Your temporary password is: " + fninput + "*" + yinput;
        System.out.println(tempPassMsg);
        
        sc.close();
    }
}