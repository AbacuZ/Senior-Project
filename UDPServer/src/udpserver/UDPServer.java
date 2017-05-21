package udpserver;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import jssc.*;

public class UDPServer extends Thread {
    private DatagramSocket serverSocket, sendSocket;
    private DatagramPacket inPacket, outPacket;
    private SerialPort serialPort;
    private String messageIn;
    private String port;
    private String dataFromSerial;
    private int serverPort, rssiBeacon1, rssiBeacon2, rssiBeacon3;
    private boolean isRunning = true;
    private int destinationPort = 8020;
    private InetAddress destinationIP;
    
    public void bind (int serverPort) throws SocketException {
        this.serverPort = serverPort;
        serverSocket = new DatagramSocket(serverPort);
    }
    
    public void start() {
        Thread thread = new Thread(this);
        thread.start();
    }
    
    public void Stop() {
        serverSocket.close();
        isRunning = false;
    }
    
    public void setMessage(String messageIn) {
        this.messageIn = messageIn;
    }
    
    public String getMessage() {
        return messageIn;
    }
    
    public String getPort() { 
        String[] portNames = SerialPortList.getPortNames();
        for (int i = 0; i < portNames.length; i++){
            port = portNames[i];
        }
        return port;
    }
    
    public void setMessageOut(String dataFromSerial) {
        this.dataFromSerial = dataFromSerial;
    }
    
    public void setAddress(InetAddress destinationIP) {
        this.destinationIP = destinationIP;
    }
    
    @Override
    public void run() {
        System.out.println("Connect to port " + serverPort);
        serialPort = new SerialPort(getPort());
        try {
            serialPort.openPort();
            serialPort.setParams(SerialPort.BAUDRATE_115200,
                                 SerialPort.DATABITS_8,
                                 SerialPort.STOPBITS_1,
                                 SerialPort.PARITY_NONE);
            System.out.println("Serial Port " + port + " opened");
            serialPort.addEventListener(new PortReader(), SerialPort.MASK_RXCHAR);
        } catch (SerialPortException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        byte[] buffer = new byte[256];
        inPacket = new DatagramPacket(buffer, buffer.length);
        while(isRunning) {           
            try {
                serverSocket.receive(inPacket);
                String msg = new String(buffer, 0, inPacket.getLength());
                System.out.println("Data from client : " + msg);
                setMessage(msg);
                setAddress(inPacket.getAddress());
                if (msg.equals("ip")) {
                    getBroadCastIPAddress();
                } else if (msg.equals("connect")) {
                    String sendBack = "connect";
                    accept(sendBack, inPacket.getAddress(), inPacket.getPort());
                }
                Thread thread = new Thread() {
                    public void run() {
                        try {
                            serialPort.writeString(getMessage() + "\r");
                            System.out.println("Data to serial port : " + getMessage());
                            Thread.sleep(100);
                        } catch (Exception ex) {}
                    }
                };
                thread.start();
            } catch (SocketException e) {
                e.printStackTrace();
            } catch (NullPointerException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } 
        }
    }
    
    public void send(String inCome) {
        try {
            System.out.print("Data to client : " + inCome);
            sendSocket = new DatagramSocket();
            outPacket = new DatagramPacket(inCome.getBytes(), inCome.length(), destinationIP, destinationPort);
            sendSocket.send(outPacket);
        } catch (SocketException se) {
            se.getStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void accept(String message, InetAddress IP, int Port) {
        try {
            DatagramSocket sendSocket = new DatagramSocket();
            DatagramPacket outPacket = new DatagramPacket(message.getBytes(), message.length(), IP, Port);
            sendSocket.send(outPacket);
            sendSocket.close();
        } catch (SocketException se) {
            se.getStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void getBroadCastIPAddress() throws SocketException {
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface networkInterface = interfaces.nextElement();
            if (networkInterface.isLoopback())
                continue;
            for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
                InetAddress broadcast = interfaceAddress.getBroadcast();
                if (broadcast != null){
                    System.out.println("found broadcast: " + broadcast);
                }
            }
        }
    }
    
    public double calculateAccuracy(int rssi) {
        //0xc5 (default:. 0xc5 potenza tx = -59)
        int txPower = -59;
        if (rssi == 0) {
          return -1.0;
        }
        double ratio = (-rssi * 1.0)/ txPower;
        if (ratio < 1.0) {
          return Math.pow(ratio,10);
        } else {
          //double distance = (0.42093) * Math.pow(ratio, 6.9476) + 0.54992;
          double accuracy = (0.89976) * Math.pow(ratio, 7.7095) + 0.111;  
          return accuracy;
        }
    }
    
    public double getDistance(int rssi) {
        /*
         * RSSI = TxPower - 10 * n * lg(d)
         * n = 2 (in free space)
         * 
         * d = 10 ^ ((TxPower - RSSI) / (10 * n))
         */
        return Math.pow(10d, ((double) (-59) - (rssi)) / (10 * 2));
    }
    
    public double getRange(int rssi) {
        double ratio_db = (-59) - (-rssi);
        double ratio_linear = Math.pow(10, ratio_db / 10);

        double r = Math.sqrt(ratio_linear);
        return r;
    }
    
    public void viewData(String str) {
        String[] data = str.split("\\|");
        if (data.length > 0) {
            for (int i = 0; i < data.length ; i++) {
                if (data[i].equals("884AEA6C3835")) {
                    rssiBeacon1 = Integer.parseInt(data[i+1]);
                    System.out.print(calculateAccuracy(rssiBeacon1) + "\n");
                }
                if (data[i].equals("04A3160A6D83")) {
                    rssiBeacon2 = Integer.parseInt(data[i+1]);
                    System.out.print(calculateAccuracy(rssiBeacon2) + "\n");
                }
                if (data[i].equals("508CB16B0175")) {
                    rssiBeacon3 = Integer.parseInt(data[i+1]);
                    System.out.print(calculateAccuracy(rssiBeacon3) + "\n");
                }
                if (data[i].equals("Front") || data[i].equals("Left") || data[i].equals("Right")) {
                    send(str);
                }
            }
        }
    }
    
    public void position(double r1, double r2, double r3) {
        double x1 = 0.0, y1 = 0.0, x2 = 3.0, y2 = 0.0, x3 = 0.0, y3 = 3.0;
        double delta, A, B, x0, y0;
        delta = 4*((x1-x2)*(y1-y3)-(x1-x3)*(y1-y2));
        A = Math.pow(r2, 2) - Math.pow(r1, 2) - Math.pow(x2, 2) + Math.pow(x1, 2) - Math.pow(y2, 2) + Math.pow(y1, 2);
        B = Math.pow(r3, 2) - Math.pow(r1, 2) - Math.pow(x3, 2) + Math.pow(x1, 2) - Math.pow(y3, 2) + Math.pow(y1, 2);
        x0 = (1/delta) * (((2*A)*(y1-y3))-((2*B)*(y1-y2)));
        y0 = (1/delta) * (((2*B)*(x1-x2))-((2*A)*(x1-x3)));
        System.out.println(x0 + "AND" + y0);
    }
    
    public class PortReader implements SerialPortEventListener {
        public void serialEvent(SerialPortEvent event) {
            if (event.isRXCHAR() && event.getEventValue() > 0) {
                try {
                    String receivedData = serialPort.readString();
                    viewData(receivedData);
                    if (destinationIP != null) {
                        System.out.print(receivedData);
                        //send(receivedData);
                    }
                } catch (SerialPortException ex) {
                    ex.printStackTrace();
                } catch (NullPointerException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
}
