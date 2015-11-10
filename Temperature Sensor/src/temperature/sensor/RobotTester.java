
package temperature.sensor;
import rxtxrobot.*;
import java.util.Scanner;
import java.math.*;

public class RobotTester 
{
    final private static int PING_PIN = 8;
    final private static int BOOM_SERVO_PIN = 7;
    final private static int CONDUCTIVITY_SERVO_PIN = 8;
    
    final static int TICKS_PER_METER = 200; //TODO not correct - calibrate
    static char faceDirection = 'N';
    final static int DANGER_ZONE = 30; //TODO decide

    public static void testPing()
    {
        for (int x=0; x < 10; ++x) 
            { 
                //Read the ping sensor value, which is connected to pin 12 
                System.out.println("Response: " + SensorBot.getPing(PING_PIN) + " cm"); 
                SensorBot.sleep(300); 
            } 
    }

    public static double getTemperature() 
    {
        int sum = 0;
        int readingCount = 10;
        SensorBot.refreshAnalogPins();

//
 //Read the analog pin values ten times, adding to sum each time
        for (int i = 0; i < readingCount; i++) 
        {
        //Refresh the analog pins so we get new readings
            int reading = SensorBot.getAnalogPin(0).getValue();
            System.out.println("Run " + i + " ADC Code: " + reading);
            
            sum += reading;
            SensorBot.refreshAnalogPins();
        } //Return the average reading
        sum /= readingCount;
        System.out.println("Average ADC Code: " + sum);
        return ((sum-881.79003021148)/-5.83534743202417);
    }
   
    public static void raiseArmAndGetTemp(){
        robot.moveServo(RXTXRobot.SERVO2, 0);
        System.out.println("Temperature: " + getTemperature());
    }

    public static void getConductivityReading() 
    {
        robot.moveServo(CONDUCTIVITY_SERVO_PIN, 0);
        System.out.println(SensorBot.getConductivity());
    }

    public static void moveRobot()
    {
        //motor1 always neg for forward, motor2 always pos
        System.out.println("I tried");
        robot.runEncodedMotor(RXTXRobot.MOTOR1, -300, 270, RXTXRobot.MOTOR2, 300, 270);         
    }
    
    public static void turnLeft(){
        //turn left 90 degrees
        //TODO fix these values
        robot.runEncodedMotor(RXTXRobot.MOTOR1, -200, 85, RXTXRobot.MOTOR2, -200, 85);
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
        robot.runEncodedMotor(RXTXRobot.MOTOR1, 200, 80, RXTXRobot.MOTOR2, 200, 80);
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
        if(SensorBot.getPing(PING_PIN) > DANGER_ZONE) //TODO decide danger zone distance
        {
            moveRobot(); //this is unsafe right now, change to check distance
            turnRight();
        }
        else //way is blocked, try the other way
        {
            turnRight();
            turnRight();
            if(SensorBot.getPing(PING_PIN) > DANGER_ZONE) 
            {
                moveRobot();
                turnLeft();
            }
            else //left and right somehow blocked
            {
                turnRight();
                moveRobot(); // probably need to check if blocked now but w/e
            }
        }
    }

    public static void testBumpSensor() //0 off ~1023 on, A2 is left sensor, A3 is right
    {
        while(true)
        {
            SensorBot.refreshAnalogPins();
            System.out.println(SensorBot.getAnalogPin(3).getValue()); //TODO change this to whatever bump sensor is pinned to 
        }

    }
    
    public static void testGPS()
    {
        double[] coordinates = SensorBot.getGPSCoordinates();

        System.out.println("Degrees latitude: " + coordinates[0]);
        System.out.println("Minutes latitude: " + coordinates[1]);
        System.out.println("Degrees longitude: " + coordinates[2]);
        System.out.println("Minutes longitude: " + coordinates[3]);
    }

    public static void runUntilBumper()
    {
        robot.runMotor(RXTXRobot.MOTOR1, -222, RXTXRobot.MOTOR2, 250, 0);
        SensorBot.refreshAnalogPins();
        while(SensorBot.getAnalogPin(2).getValue() < 500 && SensorBot.getAnalogPin(3).getValue() < 500)
        {
            SensorBot.refreshAnalogPins();
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
    
    public static void setup()
    {
        robot = new ArduinoNano();
        SensorBot = new ArduinoNano();
        robot.setPort("/dev/tty.wch ch341 USB=>RS232 1410"); ///dev/tty.wch ch341 USB=>RS232 1410
        SensorBot.setPort("/dev/tty.wch ch341 USB=>RS232 1450");
        robot.connect();
        SensorBot.connect();
        
        robot.attachServo(RXTXRobot.SERVO1, CONDUCTIVITY_SERVO_PIN);
        robot.attachServo(RXTXRobot.SERVO2, BOOM_SERVO_PIN);
        
        SensorBot.attachGPS();
        
    }
    
    public static void closeBots()
    {
        robot.close();
        SensorBot.close();
    }
public static RXTXRobot robot;
public static RXTXRobot SensorBot;
//Your main method, where your program starts
public static void main(String[] args) {
    setup();
    robot.moveServo(RXTXRobot.SERVO2, 0);
    robot.sleep(5000);
    //System.out.println("Robot read temperature of " + getTemperature() + " degrees C");
    //testGPS();
    //testPing();
    //raiseArmAndGetTemp();
    //testBumpSensor();
    //testGPS();
    //turnLeft();
    //runServoMotor();
    //turnRight();
    //evasive();
    //System.out.println(robot.getEncodedMotorPosition(RXTXRobot.MOTOR1));
    //robot.runEncodedMotor(RXTXRobot.MOTOR1, -250, 2000, RXTXRobot.MOTOR2, 250, 2000);
    //robot.runEncodedMotor(RXTXRobot.MOTOR1, -125, 300, RXTXRobot.MOTOR2, 125, 300); //move 2 feet 
    //runUntilBumper();
//    robot.runMotor(RXTXRobot.MOTOR1, 200, RXTXRobot.MOTOR2, 200, 0);
//    if(SensorBot.getPing(PING_PIN) < DANGER_ZONE)
//    {
//        robot.runMotor(RXTXRobot.MOTOR1, 0, RXTXRobot.MOTOR2, 0, 0);
//        evasive();
//    }
    //raiseArmAndGetTemp();
    
    
    //getPing();
    //System.out.println(SensorBot.getConductivity() + " conductivity reading");
        //Get the average thermistor reading
//    int thermistorReading = getThermistorReading();
//
//    //Print the results
//    System.out.println("The probe read the value: " + thermistorReading);
//    System.out.println("In volts: " + (thermistorReading * (5.0/1023.0)));
    closeBots();
 }

}
