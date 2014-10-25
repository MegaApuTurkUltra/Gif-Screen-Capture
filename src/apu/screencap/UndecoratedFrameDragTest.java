package apu.screencap;

import javax.swing.*;
import java.awt.event.*;
import java.awt.*;

/**
 * Test
 * @author "MegaApuTurkUltra"
 *
 */
public class UndecoratedFrameDragTest {
    private static Point point = new Point();

    public static void main(String[] args) {
        final JFrame frame = new JFrame();
        frame.setUndecorated(true);
        frame.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                point.x = e.getX();
                point.y = e.getY();
            }
        });
        frame.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                Point p = frame.getLocation();
                frame.setLocation(p.x + e.getX() - point.x,
                        p.y + e.getY() - point.y);
            }
        });

        frame.setSize(300, 300);
        frame.setLocation(200, 200);
        frame.setLayout(new BorderLayout());

        frame.getContentPane().add(new JLabel("Drag to move", JLabel.CENTER),
                BorderLayout.CENTER);

        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("Menu");
        menuBar.add(menu);
        JMenuItem item = new JMenuItem("Exit");
        item.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        menu.add(item);
        frame.setJMenuBar(menuBar);

        frame.setVisible(true);
    }
}