import javax.swing.*;
import java.awt.*;

/**
 * Created by Xuhao Chen on 2016/12/18.
 */
public class Frame extends JFrame {
    public Frame(int width, int height,int[] x, int[] y, int[] tour){
        this.getContentPane().add(new Panel(x,y,tour));
        setTitle("ACO TSP Display");
        setSize(width,height);
        setVisible(true);
    }
    private class Panel extends JPanel{
        private int[] x;
        private int[] y;
        private int[] tour;

        public Panel(int[] x, int[] y, int[] tour){
            setBackground(Color.white);
            this.x = x;
            this.y = y;
            this.tour = tour;
        }

        public void paintComponent(Graphics g){
            super.paintComponent(g);
            g.setColor(Color.black);
            int xShift = 30;
            int yShift = -70;
            int tSize = 4;
            //draw cities points on canvas with tags
            for(int i=0;i<x.length;i++){
                int dx = x[i]/tSize+xShift;
                int dy = y[i]/tSize+yShift;
                if(i>=9) g.drawString(Integer.toString(i+1),dx,dy);
                else g.drawString(Integer.toString(i+1),dx,dy);
                drawPoint(dx,dy,g);
            }
            System.out.println("Number of cities draw: " + x.length);
            //draw path
            for(int i=0;i<tour.length-1;i++){
                int dx1 = x[tour[i]]/tSize+xShift;
                int dy1 = y[tour[i]]/tSize+yShift;
                int dx2 = x[tour[i+1]]/tSize+xShift;
                int dy2 = y[tour[i+1]]/tSize+yShift;

                g.drawLine(dx1,dy1,dx2,dy2);
            }

        }
    }
    private void drawPoint(int x, int y, Graphics g){
        g.fillOval(x,y,5,5);
    }
}
