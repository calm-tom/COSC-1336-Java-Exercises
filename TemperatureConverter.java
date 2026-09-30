package ch03_proj_2.temperatureconverter;

import java.util.Scanner;

public class TemperatureConverter {

    public static void main(String[] args) {
        System.out.println("Welcome to the Temperature Converter");
        System.out.println();  // print a blank line

        Scanner sc = new Scanner(System.in);

        String loopLogic = "y";
        while (loopLogic.equalsIgnoreCase("y")) {
            System.out.print("Enter degrees in Farenheit:   ");
            String input = sc.nextLine();
            double farenheit = Double.parseDouble(input);

            double celsius = (farenheit - 32) * (5.0/9.0);

            String msg = "Degrees in Celsius: " + celsius + "\n";       
            System.out.println(msg);

            System.out.print("Continue? (y/n): ");
            loopLogic = sc.nextLine();
            System.out.println();
        }
    }
}
