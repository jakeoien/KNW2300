
package temperature.sensor;
import rxtxrobot.*;
import java.util.Scanner;

public class RobotTester 
{
    final private static int PING_PIN = 9;
    final private static int BOOM_SERVO_PIN = 7;
    final private static int LEFT_BUMP_PIN = 2;
    final private static int RIGHT_BUMP_PIN = 3;
    final private static int CONDUCTIVITY_SERVO_PIN = 8;
    final private static int COVERED_TEMP_PIN = 7;
    final private static int UNCOVERED_TEMP_PIN = 0;
    final private static int TICKS_PER_METER = 312;
    final private static int TICKS_PER_YARD = (int) Math.floor(TICKS_PER_METER*1.09361);
    //MOTOR1 is right motor2 is left
    
    static char faceDirection = 'N';
    final static int DANGER_ZONE = 30; //TODO decide

    public static void testPing()
    {
        while(true)
            { 
                //Read the ping sensor value, which is connected to pin 12 
                System.out.println("Response: " + SensorBot.getPing(PING_PIN) + " cm"); 
                SensorBot.sleep(300); 
            } 
    }

    public static double getTemperature(int pin) 
    {
        int sum = 0;
        int readingCount = 20;
        SensorBot.refreshAnalogPins();

//
 //Read the analog pin values ten times, adding to sum each time
        for (int i = 0; i < readingCount; i++) 
        {
        //Refresh the analog pins so we get new readings
            int reading = SensorBot.getAnalogPin(pin).getValue();
            System.out.println("Run " + i + " ADC Code: " + reading);
            
            sum += reading;
            SensorBot.refreshAnalogPins();
        } //Return the average reading
        sum /= readingCount;
        System.out.println("Average ADC Code: " + sum);
        
        if(pin == UNCOVERED_TEMP_PIN)
            return ((sum-916.02880921895)/-9.38348271446863);
        else
            return ((sum-995.923354373309)/-11.4882777276826);
    }
   
    public static void raiseArmAndGetTemp(int pin){
        robot.moveServo(RXTXRobot.SERVO2, 180);
        System.out.println("Temperature: " + getTemperature(pin));
        robot.moveServo(RXTXRobot.SERVO2, 90);
    }

    public static double getConductivityReading() 
    {
        robot.moveServo(RXTXRobot.SERVO1, 0);
        int sum = 0, numReads = 3;
        for(int i = 0; i < numReads; ++i)
        {
            int reading = SensorBot.getConductivity();
            System.out.println(reading + "conductivity read");
            sum += reading;
        }
        sum /= numReads;
        return ((sum-990.156255152698)/-13.6926312685339); //TODO fix
        
    }

    public static void moveRobot()
    {
        //motor1 always neg for forward, motor2 always pos
        System.out.println("I tried");
        robot.runEncodedMotor(RXTXRobot.MOTOR1, -300, 270, RXTXRobot.MOTOR2, 300, 270);         
    }
    
    public static void turnLeft(){
        //turn left 90 degrees
        robot.runEncodedMotor(RXTXRobot.MOTOR1, -200, 140, RXTXRobot.MOTOR2, -200, 140);
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
        robot.runEncodedMotor(RXTXRobot.MOTOR1, 200, 120, RXTXRobot.MOTOR2, 200, 120);
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
            for(int i = 0; i<5; i++){
                robot.runEncodedMotor(RXTXRobot.MOTOR1, -194, TICKS_PER_METER/5, RXTXRobot.MOTOR2, 200, TICKS_PER_METER/5);
                if(SensorBot.getPing(PING_PIN) > DANGER_ZONE){
                    evasive();
                }
            }
                //this is unsafe right now, change to check distance
            turnRight();
        }
        /*
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
        */
    }

    public static void testBumpSensor(int pinNum) //0 off ~1023 on, A2 is left sensor, A3 is right
    {
        while(true)
        {
            SensorBot.refreshAnalogPins();
            System.out.println(SensorBot.getAnalogPin(pinNum).getValue()); //TODO change this to whatever bump sensor is pinned to 
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

    public static void runUntilPing(int distanceToStop)
    {
        stopMotors();
        robot.runMotor(RXTXRobot.MOTOR1, -75, RXTXRobot.MOTOR2, 77, 0);
        int ping = SensorBot.getPing(PING_PIN);
        while(ping > distanceToStop || ping < 7)
        {
            ping = SensorBot.getPing(PING_PIN);
        }
        stopMotors();
    }
    
    public static void runUntilBumper(int speedLeft, int speedRight)
    {
        robot.runMotor(RXTXRobot.MOTOR1, 0-speedLeft, RXTXRobot.MOTOR2, speedRight, 0);
        SensorBot.refreshAnalogPins();
        int left = SensorBot.getAnalogPin(2).getValue(), right = SensorBot.getAnalogPin(3).getValue();
        while(left < 500 && right < 500)
        {
            SensorBot.refreshAnalogPins();
            left = SensorBot.getAnalogPin(2).getValue();
            right = SensorBot.getAnalogPin(3).getValue();
        }
        robot.runMotor(RXTXRobot.MOTOR1, 0, RXTXRobot.MOTOR2, 0, 0);
 }  
    public static void stopMotors()
    {
         robot.runMotor(RXTXRobot.MOTOR1, 0, RXTXRobot.MOTOR2, 0, 0);
    }
    
    public static void setup(boolean setupRobot, boolean setupSensorBot)
    {
        if(setupRobot)
        {
            robot = new ArduinoNano();
            robot.setPort("/dev/tty.wch ch341 USB=>RS232 1410"); ///dev/tty.wch ch341 USB=>RS232 1410
            robot.connect();
            robot.attachServo(RXTXRobot.SERVO1, CONDUCTIVITY_SERVO_PIN);
            robot.attachServo(RXTXRobot.SERVO2, BOOM_SERVO_PIN);
            robot.moveServo(RXTXRobot.SERVO2, 75);
        }
        
        if(setupSensorBot)
        {
            SensorBot = new ArduinoNano();
            SensorBot.setPort("/dev/tty.wch ch341 USB=>RS232 1450");
            SensorBot.connect();
            SensorBot.attachGPS();
        }
        
    }
    
    public static void closeBots()
    {
        if(robot != null)
            robot.close();
        if(SensorBot != null)
            SensorBot.close();
    }
    
    public static void goInCircle()
    {
        robot.runEncodedMotor(RXTXRobot.MOTOR1, -186, TICKS_PER_METER, RXTXRobot.MOTOR2, 194, TICKS_PER_METER);
        turnLeft();
        robot.runEncodedMotor(RXTXRobot.MOTOR1, -186, TICKS_PER_METER, RXTXRobot.MOTOR2, 194, TICKS_PER_METER);
        turnLeft();
        robot.runEncodedMotor(RXTXRobot.MOTOR1, -186, TICKS_PER_METER, RXTXRobot.MOTOR2, 194, TICKS_PER_METER);
        turnLeft();
        robot.runEncodedMotor(RXTXRobot.MOTOR1, -186, TICKS_PER_METER, RXTXRobot.MOTOR2, 194, TICKS_PER_METER);
        turnLeft(); 
    }
    
    public static void doSandboxTask()
    {
        runUntilBumper(130, 130);
        System.out.println(getConductivityReading() + "% water");
    }
    
    public static void startInQuad1()
    {
        runUntilPing(25);
        turnLeft();
        robot.runEncodedMotor(RXTXRobot.MOTOR1, -140, (int)(TICKS_PER_YARD*2)+260, RXTXRobot.MOTOR2, 150, (int)(TICKS_PER_YARD*2)+260);
        turnRight();
        robot.runEncodedMotor(RXTXRobot.MOTOR1, -140, TICKS_PER_YARD, RXTXRobot.MOTOR2, 150, TICKS_PER_YARD);
        goUpRampAndGetTemp();
        turnLeft();
        runUntilPing(25);
        turnLeft();
        robot.runEncodedMotor(RXTXRobot.MOTOR1, -140, TICKS_PER_YARD, RXTXRobot.MOTOR2, 150, TICKS_PER_YARD);
        turnRight();
        doSandboxTask();
        robot.runEncodedMotor(RXTXRobot.MOTOR1, 100, 50, RXTXRobot.MOTOR2, -100, 50);
        turnRight();
        robot.runEncodedMotor(RXTXRobot.MOTOR1, -140, (int)(TICKS_PER_YARD*2)+260, RXTXRobot.MOTOR2, 150, (int)(TICKS_PER_YARD*2)+260);
        runUntilPing(25);
    }
    
    public static void startInQuad2()
    {
        robot.runEncodedMotor(RXTXRobot.MOTOR1, -140, (int)(TICKS_PER_YARD*2)+210, RXTXRobot.MOTOR2, 150, (int)(TICKS_PER_YARD*2)+210);
        stopMotors();
        turnRight();
        robot.runEncodedMotor(RXTXRobot.MOTOR1, -140, TICKS_PER_YARD, RXTXRobot.MOTOR2, 150, TICKS_PER_YARD);
        goUpRampAndGetTemp();
        doSandboxTask();
       
    }
    
    public static void startInQuad3()
    {
        robot.runMotor(RXTXRobot.MOTOR1, -100, RXTXRobot.MOTOR2, 100, 0);
        runUntilBumper(130,130);
        turnRight();
        robot.runEncodedMotor(RXTXRobot.MOTOR1, -140, (int)(TICKS_PER_YARD*2)+150, RXTXRobot.MOTOR2, 150, (int)(TICKS_PER_YARD*2)+150);
        turnRight();
        doSandboxTask();
        robot.runEncodedMotor(RXTXRobot.MOTOR1, 50, 50, RXTXRobot.MOTOR2, -50, 50);
        turnLeft();
        runUntilBumper(130,130);
        turnRight();
        runUntilBumper(130,130);
    }
    
    public static void startInQuad4()
    {
        runUntilPing(25);
        turnRight();
        robot.runEncodedMotor(RXTXRobot.MOTOR1, -140, (int)(TICKS_PER_YARD*2)+300, RXTXRobot.MOTOR2, 150, (int)(TICKS_PER_YARD*2)+300);
        turnLeft();
        robot.runEncodedMotor(RXTXRobot.MOTOR1, -142, 150+75, RXTXRobot.MOTOR2, 150, 150+75);
        turnLeft();
        runUntilPing(25);
        turnRight();
        runUntilPing(40);
        turnRight();
        startInQuad3();
    }
    
    public static void goUpRampAndGetTemp()
    {
        robot.runMotor(RXTXRobot.MOTOR1, -200, RXTXRobot.MOTOR2, 200, 3500);
        stopMotors();
        raiseArmAndGetTemp(COVERED_TEMP_PIN);
        robot.runMotor(RXTXRobot.MOTOR1, -190, RXTXRobot.MOTOR2, 210, 3000); 
    }
public static RXTXRobot robot;
public static RXTXRobot SensorBot;
//Your main method, where your program starts
public static void main(String[] args) {
    setup(true, true);
    startInQuad2();
    //testPing();
    //runUntilPing(25);
    //goUpRampAndGetTemp();
    //runUntilBumper(222, 243);
    //goInCircle();
    //turnLeft();
    //robot.runEncodedMotor(RXTXRobot.MOTOR1, -150, (int)(TICKS_PER_YARD*2)+300, RXTXRobot.MOTOR2, 150, (int)(TICKS_PER_YARD*2)+300);
    //goUpRampAndGetTemp();
    //System.out.println(getConductivityReading());
    //startInQuad3();
    //turnRight();
    //raiseArmAndGetTemp(COVERED_TEMP_PIN);
    //System.out.println(getTemperature(UNCOVERED_TEMP_PIN));
    //System.out.println(getTemperature(COVERED_TEMP_PIN));
    //robot.moveServo(RXTXRobot.SERVO2, 0);
    //doSandboxTask();
    //System.out.println("Robot read temperature of " + getTemperature() + " degrees C");
    //startInQuad3();
    //evasive();
    //System.out.println(robot.getEncodedMotorPosition(RXTXRobot.MOTOR1));
    //robot.runEncodedMotor(RXTXRobot.MOTOR1, -250, 2000, RXTXRobot.MOTOR2, 250, 2000);
    //robot.runEncodedMotor(RXTXRobot.MOTOR1, -186, 310, RXTXRobot.MOTOR2, 194, 310); //move 1 meter 
    //runUntilBumper();
//    robot.runMotor(RXTXRobot.MOTOR1, 200, RXTXRobot.MOTOR2, 200, 0);
//    if(SensorBot.getPing(PING_PIN) < DANGER_ZONE)
//    {
//        robot.runMotor(RXTXRobot.MOTOR1, 0, RXTXRobot.MOTOR2, 0, 0);
//        evasive();
//    }
    
    
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


