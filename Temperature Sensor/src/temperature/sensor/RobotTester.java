
package temperature.sensor;
import rxtxrobot.*;
import java.util.Scanner;
import java.math.*;

public class RobotTester 
{
    final private static int PING_PIN = 12;
    final static int TICKS_PER_METER = 200; //TODO not correct - calibrate
    static char faceDirection = 'N';
    final static int DANGER_ZONE = 40; //TODO decide

    public static void testPing()
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
        robot.attachServo(RXTXRobot.SERVO1, 9); 
        while(robot.getAnalogPin(1).getValue() == 0)
        {
            robot.refreshAnalogPins();
            robot.moveServo(RXTXRobot.SERVO1, 180);
        }
        System.out.println("finished");
    }

    public static int conductivity() 
    {
        int sum = 0;
        int readingCount = 10;

 //Read the analog pin values ten times, adding to sum each time
        for (int i = 0; i < readingCount; i++) 
        {
            int reading = robot.getConductivity();
            sum += reading;
        } //Return the average reading
        return sum / readingCount;
    }

    public static void moveRobot3Meters()
    {
        //motor1 always positive for forward, motor2 always negative
        System.out.println("I tried");
        robot.runEncodedMotor(RXTXRobot.MOTOR1, 300, 1000, RXTXRobot.MOTOR2, -300, 1000);         
    }
    
    public static void turnLeft(){
        //turn left 90 degrees
        //TODO fix these values
        robot.runEncodedMotor(RXTXRobot.MOTOR1, 100, 100, RXTXRobot.MOTOR2, 100, 100);
        switch (faceDirection)
        {
            case 'N':
                faceDirection = 'W';
                break;
            case 'W':
                faceDirection = 'S';
                break;
            case 'S':
                faceDirection = 'E';
                break;
            case 'E':
                faceDirection = 'N';
                break;
            default:
                faceDirection = 'W';
                break;
        }
    }
    
    public static void turnRight(){
        //turn right 90 degrees
        //TODO fix these values
        robot.runEncodedMotor(RXTXRobot.MOTOR1, 100, 100, RXTXRobot.MOTOR2, 100, 100);
        switch (faceDirection)
        {
            case 'N':
                faceDirection = 'E';
                break;
            case 'E':
                faceDirection = 'S';
                break;
            case 'S':
                faceDirection = 'W';
                break;
            case 'W':
                faceDirection = 'N';
                break;
            default:
                faceDirection = 'E';
                break;
        }
    }
    
    public static void evasive(){
        //TODO this is really rough
        turnLeft();
        if(robot.getPing(PING_PIN) > DANGER_ZONE) //TODO decide danger zone distance
        {
            moveRobot3Meters(); //this is unsafe right now, change to check distance
            turnRight();
        }
        else //way is blocked, try the other way
        {
            turnRight();
            turnRight();
            if(robot.getPing(PING_PIN) > DANGER_ZONE) 
            {
                moveRobot3Meters();
                turnLeft();
            }
            else //left and right somehow blocked
            {
                turnRight();
                moveRobot3Meters(); // probably need to check if blocked now but w/e
            }
        }
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
    
    public static double[] gpsToMeters(double[] c){
        double[] startLocation = robot.getGPSCoordinates(); //starting location before moving
        double startLat = (Math.abs(startLocation[0]) + (startLocation[1] / 60.0)); //starting latitude in decimal degrees
        double startLong = (Math.abs(startLocation[2]) + (startLocation[3] / 60.0)); //starting longitude in decimal degrees
        double endLat = (Math.abs(c[0]) + (c[1] / 60.0)); //endling latitude in decimal degrees
        double endLong = (Math.abs(c[2]) + (c[3] / 60.0)); //ending longitude in decimal degrees
        double y = (endLat - startLat) * 110901.46; //vertical distance in meters
        double x = (endLong - startLong) * 110901.46; //horizontal distance in meters
        double[] ans = {x,y}; //array of vertical and horizontal distances displayed in meters
        return ans;
        //if issues, double precision errors
        
    }
    public static void moveToLocation(double[] coordinates){
        //TODO figure out how long 1 meter is
        //TODO north currently is positive latitude & meters to travel, check
        //TODO west currently is positive longitude & meters to travel, check
        double[] distanceToTravel = gpsToMeters(coordinates);
        
        
        boolean motorsRunning = false;
        //x direction
        if(distanceToTravel[0] > 0){
            while(faceDirection != 'W'){
                turnLeft();
            }
        }
        if(distanceToTravel[0] < 0){
            while(faceDirection != 'E'){
                turnRight();
            }
        }
        
        while(gpsToMeters(coordinates)[0] > 2)
        {
            if(robot.getPing(PING_PIN) < DANGER_ZONE)
            {
                stopMotors();
                motorsRunning = false;
                evasive();
            }
            else//if(robot.getPing(PING_PIN) > DANGER_ZONE)
            {   
                if(!motorsRunning)
                {
                    robot.runMotor(RXTXRobot.MOTOR1, 250, RXTXRobot.MOTOR2, 250, 0);
                    motorsRunning = true;
                }
            }
            robot.sleep(250);
        }
        
        //y direction
        
        if(distanceToTravel[1] > 0){
            while(faceDirection != 'N'){
                turnLeft();
            }
        }
        if(distanceToTravel[1] < 0){
            while(faceDirection != 'S'){
                turnRight();
            }
        }
        
        while(gpsToMeters(coordinates)[1] > 2)
        {
            if(robot.getPing(PING_PIN) < DANGER_ZONE)
            {
                stopMotors();
                motorsRunning = false;
                evasive();
            }
            else//if(robot.getPing(PING_PIN) > DANGER_ZONE)
            {   
                if(!motorsRunning)
                {
                    robot.runMotor(RXTXRobot.MOTOR1, 250, RXTXRobot.MOTOR2, 250, 0);
                    motorsRunning = true;
                }
            }
            robot.sleep(250);
        }
    }
    public static void stopMotors()
    {
         robot.runMotor(RXTXRobot.MOTOR1, 0, RXTXRobot.MOTOR2, 0, 0);
    }
public static RXTXRobot robot;
//Your main method, where your program starts
public static void main(String[] args) {

    //Connect to the arduino
    robot = new ArduinoNano();
    robot.setPort("/dev/tty.wch ch341 USB=>RS232 1410"); ///dev/tty.wch ch341 USB=>RS232 1410
    robot.connect();
    //runServoMotor();
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
