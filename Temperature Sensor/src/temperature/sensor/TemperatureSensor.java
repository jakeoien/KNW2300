
package temperature.sensor;

import rxtxrobot.*;

public class TemperatureSensor {

    
//This function reads the voltage coming into analog pin 0 ten times
//takes the average, then returns the result.
public static int getThermistorReading() {
    int sum = 0;
    int readingCount = 10;

    //Read the analog pin values ten times, adding to sum each time
    boolean go = true;
    while (go){

    //Refresh the analog pins so we get new readings
        robot.refreshAnalogPins();
        int reading = robot.getAnalogPin(0).getValue();
        System.out.println(reading + " read");
        sum += reading;
    }

 //Return the average reading
    return sum / readingCount;
 }

 public static RXTXRobot robot;

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

    robot.close();
    }
    
}
