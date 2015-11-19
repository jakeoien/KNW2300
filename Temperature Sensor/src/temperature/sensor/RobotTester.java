
package temperature.sensor;
import rxtxrobot.*;
import java.util.Scanner;

public class RobotTester 
{
    final private static int RIGHT_PING_PIN = 7;
    final private static int LEFT_PING_PIN = 9;
    final private static int FRONT_PING_PIN = 8;
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

    public static void testPing(int pin)
    {
        while(true)
            { 
                //Read the ping sensor value, which is connected to pin 12 
                System.out.println("Response: " + SensorBot.getPing(pin) + " cm"); 
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
        robot.sleep(1500);
        robot.moveServo(RXTXRobot.SERVO1, 90);
        robot.sleep(1500);
        robot.moveServo(RXTXRobot.SERVO1, 0);
        robot.sleep(1500);
        robot.moveServo(RXTXRobot.SERVO1, 90);
        robot.sleep(1500);
        robot.moveServo(RXTXRobot.SERVO1, 0);
        int sum = 0, numReads = 3;
        for(int i = 0; i < numReads; ++i)
        {
            int reading = SensorBot.getConductivity();
            System.out.println(reading + "conductivity read");
            sum += reading;
        }
        sum /= numReads;
        robot.moveServo(RXTXRobot.SERVO1, 90);
        return ((sum-990.156255152698)/-13.6926312685339);
        
        
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
        if(SensorBot.getPing(FRONT_PING_PIN) > DANGER_ZONE) //TODO decide danger zone distance
        {
            for(int i = 0; i<5; i++){
                robot.runEncodedMotor(RXTXRobot.MOTOR1, -194, TICKS_PER_METER/5, RXTXRobot.MOTOR2, 200, TICKS_PER_METER/5);
                if(SensorBot.getPing(FRONT_PING_PIN) > DANGER_ZONE){
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

    public static void runUntilPing(int speedRight, int speedLeft, int distanceToStop)
    {
        stopMotors();
        robot.runMotor(RXTXRobot.MOTOR1, 0-speedRight, RXTXRobot.MOTOR2, speedLeft, 0);
        int ping = SensorBot.getPing(FRONT_PING_PIN);
        while(ping > distanceToStop || ping < 7)
        {
            ping = SensorBot.getPing(FRONT_PING_PIN);
            SensorBot.sleep(50);
            System.out.println(ping);
        }
        stopMotors();
    }
    
    public static void runUntilBumper(int speedRight, int speedLeft)
    {
        robot.runMotor(RXTXRobot.MOTOR1, 0-speedRight, RXTXRobot.MOTOR2, speedLeft, 0);
        SensorBot.refreshAnalogPins();
        int left = SensorBot.getAnalogPin(LEFT_BUMP_PIN).getValue(), right = SensorBot.getAnalogPin(RIGHT_BUMP_PIN).getValue();
        while(left < 500 && right < 500)
        {
            SensorBot.refreshAnalogPins();
            left = SensorBot.getAnalogPin(LEFT_BUMP_PIN).getValue();
            right = SensorBot.getAnalogPin(RIGHT_BUMP_PIN).getValue();
        }
        stopMotors();
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
            robot.setPort("/dev/tty.wch ch341 USB=>RS232 1410"); ///dev/tty.wch ch341 USB=>RS232 1410       COMM3
            robot.connect();
            robot.attachServo(RXTXRobot.SERVO1, CONDUCTIVITY_SERVO_PIN);
            robot.attachServo(RXTXRobot.SERVO2, BOOM_SERVO_PIN);
            robot.moveServo(RXTXRobot.SERVO2, 75);
        }
        
        if(setupSensorBot)
        {
            SensorBot = new ArduinoNano();
            SensorBot.setPort("/dev/tty.wch ch341 USB=>RS232 1410");   
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
    
    public static void startInQuad1() //rock
    {
        runUntilPing(120,120,25);
        turnRight();
        robot.runEncodedMotor(RXTXRobot.MOTOR1, -150, (int)(TICKS_PER_YARD*2)+260, RXTXRobot.MOTOR2, 150, (int)(TICKS_PER_YARD*2)+260);
        turnLeft();
        robot.runEncodedMotor(RXTXRobot.MOTOR1, -150, TICKS_PER_YARD, RXTXRobot.MOTOR2, 150, TICKS_PER_YARD); //move past tip of penis fix vals
        
        //go
        turnLeft();
        runUntilPing(120,120,25);
        turnRight();
        
        robot.runMotor(RXTXRobot.MOTOR1, -96, RXTXRobot.MOTOR2, 100, 0); //run to end of wall, and a bit more
        int leftPing = SensorBot.getPing(LEFT_PING_PIN);
        while(leftPing < 25)
        {
            leftPing = SensorBot.getPing(LEFT_PING_PIN);
            SensorBot.sleep(50);
        }
        stopMotors();
        robot.runMotor(RXTXRobot.MOTOR1, -96, RXTXRobot.MOTOR2, 100, 1000);
        
        turnLeft();
        robot.runMotor(RXTXRobot.MOTOR1, -96, RXTXRobot.MOTOR2, 100, 1000);
        turnLeft();
        
        runUntilPing(126,130,40);
        turnRight();
        startInQuad4();
    }
    
    public static void startInQuad2() //ramp
    {
        robot.runEncodedMotor(RXTXRobot.MOTOR1, -150, (int)(TICKS_PER_YARD*2)+210, RXTXRobot.MOTOR2, 150, (int)(TICKS_PER_YARD*2)+210);
        stopMotors();
        turnRight();
        robot.runEncodedMotor(RXTXRobot.MOTOR1, -150, TICKS_PER_YARD, RXTXRobot.MOTOR2, 150, TICKS_PER_YARD);
        goUpRampAndGetTemp();
        doSandboxTask();
       
    }
    
    public static void startInQuad3()//sand
    {
        runUntilPing(120,130,25); //run into wall
        
        robot.runMotor(RXTXRobot.MOTOR1, 200, RXTXRobot.MOTOR2, 200, 0); //turn right next to wall
        int leftPing = SensorBot.getPing(LEFT_PING_PIN);
        while(leftPing > 16)
        {
            leftPing = SensorBot.getPing(LEFT_PING_PIN);
            System.out.println(leftPing + " left");
            SensorBot.sleep(50);
        }
        SensorBot.sleep(300);
        stopMotors();
        
        robot.runMotor(RXTXRobot.MOTOR1, -125, RXTXRobot.MOTOR2, 130, 0); //run to end of wall, and a bit more
        leftPing = SensorBot.getPing(LEFT_PING_PIN);
        while(leftPing < 30)
        {
            leftPing = SensorBot.getPing(LEFT_PING_PIN);
            SensorBot.sleep(50);
        }
        stopMotors();
        robot.runMotor(RXTXRobot.MOTOR1, -125, RXTXRobot.MOTOR2, 130, 1000);
        
        robot.runMotor(RXTXRobot.MOTOR1, 200, RXTXRobot.MOTOR2, 200, 0); //turn right
        int frontPing = SensorBot.getPing(FRONT_PING_PIN); 
        while(frontPing > 178)
        {
            frontPing = SensorBot.getPing(FRONT_PING_PIN);
            SensorBot.sleep(50);
        }
        stopMotors();
        
        doSandboxTask();
        stopMotors();
        //robot.runEncodedMotor(RXTXRobot.MOTOR1, 120, 50, RXTXRobot.MOTOR2, -120, 50); //back up slightly
        robot.runEncodedMotor(RXTXRobot.MOTOR1, 120, 1775, RXTXRobot.MOTOR2, -120, 1775);
        //TODO BACK THE FUCK UP
        /*
        robot.runMotor(RXTXRobot.MOTOR1, -200, RXTXRobot.MOTOR2, -200, 0); //turn around
        SensorBot.sleep(800);
        frontPing = SensorBot.getPing(FRONT_PING_PIN);
        while(frontPing > 300)
        {
            frontPing = SensorBot.getPing(FRONT_PING_PIN);
            SensorBot.sleep(50);
        }
        SensorBot.sleep(500);
        stopMotors();
        
        runUntilPing(126, 130, 60); //approach ramp
        */
        goUpRampAndGetTempBackwards(); //obv, goes down north side CHANGE THIS TO WORK WITH BACKING UP
        
        leftPing = SensorBot.getPing(RIGHT_PING_PIN); //go until even with charging
        robot.runMotor(RXTXRobot.MOTOR1, -96, RXTXRobot.MOTOR2, 100, 0);
        while(leftPing > 200 || leftPing < 8)
        {
            leftPing = SensorBot.getPing(RIGHT_PING_PIN);
            SensorBot.sleep(50);
        }
        stopMotors();
        
        robot.runMotor(RXTXRobot.MOTOR1, -150, RXTXRobot.MOTOR2, -150, 0); //turn left
        frontPing = SensorBot.getPing(FRONT_PING_PIN);
        while(frontPing > 150 || leftPing < 8)
        {
            leftPing = SensorBot.getPing(LEFT_PING_PIN);
            SensorBot.sleep(50);
        }
        SensorBot.sleep(500);
        stopMotors();
        
        runUntilPing(126,130,40); //go to charging station
        
              
    }
    
    public static void startInQuad4() //ball
    {
        runUntilPing(120,120,25);//go to wall
        
        robot.runMotor(RXTXRobot.MOTOR1, 100, RXTXRobot.MOTOR2, 100, 0); //turn right next to wall
        int leftPing = SensorBot.getPing(LEFT_PING_PIN);
        while(leftPing > 20 && leftPing < 17)
        {
            leftPing = SensorBot.getPing(LEFT_PING_PIN);
            SensorBot.sleep(50);
        }
        stopMotors();
        
        robot.runMotor(RXTXRobot.MOTOR1, -96, RXTXRobot.MOTOR2, 100, 0); //run to end of wall, and a bit more
        leftPing = SensorBot.getPing(LEFT_PING_PIN);
        while(leftPing < 25)
        {
            leftPing = SensorBot.getPing(LEFT_PING_PIN);
            SensorBot.sleep(50);
        }
        stopMotors();
        robot.runMotor(RXTXRobot.MOTOR1, -96, RXTXRobot.MOTOR2, 100, 500);
        
        
        turnLeft();
        robot.runEncodedMotor(RXTXRobot.MOTOR1, -150, 150+75, RXTXRobot.MOTOR2, 150, 150+75);
        turnLeft();
        runUntilPing(120,120,25);
        
        
        robot.runMotor(RXTXRobot.MOTOR1, 100, RXTXRobot.MOTOR2, 100, 0); //turn right
        int rightPing = SensorBot.getPing(RIGHT_PING_PIN);
        while(rightPing > 20 || rightPing < 17 )
        {
            rightPing = SensorBot.getPing(FRONT_PING_PIN);
            SensorBot.sleep(50);
        }
        stopMotors();
        
        runUntilPing(120,120,40);
        turnRight();
        startInQuad3();
    }
    
    public static void goUpRampAndGetTemp()
    {
        robot.runMotor(RXTXRobot.MOTOR1, -200, RXTXRobot.MOTOR2, 200, 3500);
        stopMotors();
        raiseArmAndGetTemp(COVERED_TEMP_PIN);
        robot.runMotor(RXTXRobot.MOTOR1, -200, RXTXRobot.MOTOR2, 200, 3000); 
    }
    
    public static void goUpRampAndGetTempBackwards()
    {
        robot.runEncodedMotor(RXTXRobot.MOTOR1, 200, TICKS_PER_YARD, RXTXRobot.MOTOR2, -200, TICKS_PER_YARD);
        stopMotors();
        raiseArmAndGetTemp(COVERED_TEMP_PIN);
        robot.runMotor(RXTXRobot.MOTOR1, -200, RXTXRobot.MOTOR2, 200, 300); 
    }
        
public static RXTXRobot robot;
public static RXTXRobot SensorBot;
//Your main method, where your program starts
public static void main(String[] args) {
    setup(true, true);
    testPing(FRONT_PING_PIN);
    //startInQuad2();
    //doSandboxTask();
    //runUntilPing(25);
    //goUpRampAndGetTemp();
    //runUntilBumper(222, 243);
    //goInCircle();
    //turnLeft();
    //robot.runEncodedMotor(RXTXRobot.MOTOR1, -150, (int)(TICKS_PER_YARD*2)+300, RXTXRobot.MOTOR2, 150, (int)(TICKS_PER_YARD*2)+300);
    //goUpRampAndGetTemp();
    //System.out.println(getConductivityReading());
    startInQuad3();
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


