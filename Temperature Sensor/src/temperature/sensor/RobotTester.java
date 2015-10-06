
package temperature.sensor;
import rxtxrobot.*;
import java.util.Scanner;

public class RobotTester 
{
    final private static int PING_PIN = 12;
    
    public static void getPing()
    {
        for (int x=0; x < 10; ++x) 
            { 
		//Read the ping sensor value, which is connected to pin 12 
                System.out.println("Response: " + robot.getPing(PING_PIN) + " cm"); 
		robot.sleep(300); 
            } 
    }
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
        //motor1 always positive for forward, motor2 always negative
        System.out.println("I tried");
        robot.runEncodedMotor(RXTXRobot.MOTOR1, 300, 200, RXTXRobot.MOTOR2, -300, 200);  
//        robot.runMotor(RXTXRobot.MOTOR1, 250, 2500);
//        robot.runMotor(RXTXRobot.MOTOR2, 250, 2500);
    }
    
    public static void testBumpSensor() //0 off ~1023 on
    {
        while(true)
        {
            robot.refreshAnalogPins();
            System.out.println(robot.getAnalogPin(1).getValue()); //TODO change this to whatever bump sensor is pinned to 
        }
        
    }
    
    public static void runUntilBumper()
    {
        robot.runMotor(RXTXRobot.MOTOR1, 250, RXTXRobot.MOTOR2, -250, 0);
        robot.refreshAnalogPins();
        while(robot.getAnalogPin(1).getValue() == 0)
        {
            robot.refreshAnalogPins();
        }
        robot.runMotor(RXTXRobot.MOTOR1, 0, RXTXRobot.MOTOR2, 0, 0);
 }  
public static RXTXRobot robot;
//Your main method, where your program starts
public static void main(String[] args) {

    //Connect to the arduino
    robot = new ArduinoNano();
    robot.setPort("/dev/tty.wch ch341 USB=>RS232 1450");
    robot.connect();
    moveRobot3Meters();
    //testBumpSensor();
    //runUntilBumper();
   // getPing();
    robot.close();
//    //Get the average thermistor reading
//    int thermistorReading = getThermistorReading();
//
//    //Print the results
//    System.out.println("The probe read the value: " + thermistorReading);
//    System.out.println("In volts: " + (thermistorReading * (5.0/1023.0)));
 }
    
}
