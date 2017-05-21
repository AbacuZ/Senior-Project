package udpserver;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.bluetooth.*;

public class MainProgram {
    public static JFrame mazeFrame;
    
    public static void main(String [] args) throws IOException {
        
        int width  = 693;
        int height = 545;
        
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        double screenWidth = screenSize.getWidth();
        double ScreenHeight = screenSize.getHeight();
        int x = ((int)screenWidth-width)/2;
        int y = ((int)ScreenHeight-height)/2;
        
        mazeFrame = new JFrame("Dijkstra's");
        mazeFrame.setContentPane(new MazePanel(width,height));
        mazeFrame.pack();
        mazeFrame.setResizable(false);
        mazeFrame.setLocation(x,y);
        mazeFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mazeFrame.setVisible(true);

        UDPServer server = new UDPServer();
        server.bind(8000);
        server.start();
        System.out.println(server.getDistance(-65));
        /*System.out.println(server.getRange(-66));*/
        //server.position(1.67, 1.15, 1.77);
        
        /*SerialData serial = new SerialData();1
        serial.start();*/
        
        /*SerialData serialData = new SerialData();
        serialData.bind();
        serialData.start();*/
        /*serialData.setMessage(server.getMessage());*/
        
        /*while(true) {
            if (server.getMessage() == null)
                break;
            
            System.out.println(server.getMessage());
            serialData.setMessage(server.getMessage());
            sendTo(address, inCome);      
        }*/
        //server.Stop();
    }
    
    public static class MazePanel extends JPanel {
        JButton resetButton, mazeButton, clearButton, realTimeButton, stepButton, animationButton;
        boolean realTime;
        
        private MazePanel(int width, int height) {
            setLayout(null);
            setPreferredSize(new Dimension(width,height));
            
            resetButton = new JButton("New grid");
            resetButton.addActionListener(new ActionHandler());
            resetButton.setBackground(Color.lightGray);
            resetButton.addActionListener(this::resetButtonActionPerformed);
            
            add(resetButton);
            resetButton.setBounds(520, 10, 170, 25);  
        }
        
        private void resetButtonActionPerformed(java.awt.event.ActionEvent evt) {                                           
            realTime = false;
            realTimeButton.setEnabled(true);
            stepButton.setEnabled(true);
            animationButton.setEnabled(true);
            //initializeGrid(false);
        }

        private class ActionHandler implements ActionListener {
            @Override
                public void actionPerformed(ActionEvent evt) {
                    String cmd = evt.getActionCommand();

                }
        }
    }
}
