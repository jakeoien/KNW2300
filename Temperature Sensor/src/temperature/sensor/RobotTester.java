
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
        } //Return the average reading
        return sum / readingCount;
    }
    
    public static void runServoMotor()
    {
//        Scanner userInput = new Scanner(System.in);
//        System.out.println("Angle to turn servo to: ");
//        int angle = userInput.nextInt();
//        System.out.println("Moving servo to angle " + angle);
        robot.attachServo(RXTXRobot.SERVO1, 7); 
        robot.moveServo(RXTXRobot.SERVO1, 45);
        System.out.println("finished");
    }
    
    public static void moveRobot3Meters()
    {
        robot.attachMotor(RXTXRobot.MOTOR1, 2);
        robot.attachMotor(RXTXRobot.MOTOR2, 3);
  //      robot.resetEncodedMotorPosition(RXTXRobot.MOTOR1);
  //      robot.resetEncodedMotorPosition(RXTXRobot.MOTOR2);
        robot.runMotor(RXTXRobot.MOTOR1, 10, RXTXRobot.MOTOR2, 10, 10000);
     
    }
    
    public static void runUntilBumper(){
//        robot.attachMotor(RXTXRobot.MOTOR1, 2);
//        robot.attachMotor(RXTXRobot.MOTOR2, 3);
//        robot.resetEncodedMotorPosition(RXTXRobot.MOTOR1);
//        robot.resetEncodedMotorPosition(RXTXRobot.MOTOR2);
//        robot.runEncodedMotor(RXTXRobot.MOTOR1, 10, 0, RXTXRobot.MOTOR2, 10, 0);
//        robot.refreshAnalogPins();
//        while(robot.getAnalogPin(1).getValue() == 0)
//        {
//            
//        }
//        
//        robot.runEncodedMotor(RXTXRobot.MOTOR1, 10, 1, RXTXRobot.MOTOR2, 10, 1);
 }  
public static RXTXRobot robot;
//Your main method, where your program starts
public static void main(String[] args) {

    //Connect to the arduino
    robot = new ArduinoNano();
    robot.setPort("COM3");
    robot.connect();
    moveRobot3Meters();
    robot.close();
//    //Get the average thermistor reading
//    int thermistorReading = getThermistorReading();
//
//    //Print the results
//    System.out.println("The probe read the value: " + thermistorReading);
//    System.out.println("In volts: " + (thermistorReading * (5.0/1023.0)));
 }
    
}
