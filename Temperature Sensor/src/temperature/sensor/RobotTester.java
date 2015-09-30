/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package temperature.sensor;
import rxtxrobot.*;
import java.util.Scanner;

public class RobotTester 
{
    public static int getThermistorReading() 
    {
        int sum = 0;
        int readingCount = 10;

 //Read the analog pin values ten times, adding to sum each time
        for (int i = 0; i < readingCount; i++) 
        {
        //Refresh the analog pins so we get new readings
            robot.refreshAnalogPins();
            int reading = robot.getAnalogPin(0).getValue();
            sum += reading;
        }
 //Return the average reading
        return sum / readingCount;
    }
    
    public static void runServoMotor()
    {
        Scanner userInput = new Scanner(System.in);
        System.out.println("Angle to turn servo to: ");
        int angle = userInput.nextInt();
        System.out.println("Moving servo to angle " + angle);
        robot.attachServo(RXTXRobot.SERVO1, 0); //THESE ARE NOT NECESSARILY CORRECT CHECK THIS YOU IDIOT
        robot.moveServo(RXTXRobot.SERVO1, angle);
    }
    
    public static void moveRobot3Meters()
    {
//        System.out.println("Moving robot ~3 meters");
//        int TEST_TIME = 5000;
//        robot.attachMotor(RXTXRobot.MOTOR1, 8); //THESE ARE NOT CORRECT CHECK THIS YOU IDIOT      
    }
    
public static RXTXRobot robot;
//Your main method, where your program starts
public static void main(String[] args) {

    //Connect to the arduino
    robot = new ArduinoNano();
    robot.setPort("COM3");
    robot.connect();

    //Get the average thermistor reading
    int thermistorReading = getThermistorReading();

    //Print the results
    System.out.println("The probe read the value: " + thermistorReading);
    System.out.println("In volts: " + (thermistorReading * (5.0/1023.0)));
 }
    
}
