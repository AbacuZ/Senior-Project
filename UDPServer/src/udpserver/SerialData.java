package udpserver;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import jssc.*;

public class SerialData extends Thread {
    private SerialPort serialPort;
    private String msg;
    
    /*public void bind() {
        serialPort = new jssc.SerialPort("COM3");
        try {
            serialPort.openPort();
            serialPort.setParams(SerialPort.BAUDRATE_115200,
                                 SerialPort.DATABITS_8,
                                 SerialPort.STOPBITS_1,
                                 SerialPort.PARITY_NONE);
            System.out.println("Serial Port COM3 opened");
        } catch (SerialPortException ex) {
            ex.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/
    
    public void start() {
        Thread thread = new Thread(this);
        thread.start();
    }
    
    public void setMessage(String msg) {
        this.msg = msg;
    }
    
    public String getMessage() {
        return msg;
    }
    
    public void run() {
        try {
            /*System.out.println("Data to Serial Port:" + getMessage());
            serialPort.writeString(getMessage());*/
            /*String inCome = serialPort.readString();
            System.out.println("Data from Serial Port:" + inCome);*/
        } catch (Exception ex) {
        }       
    }
}
