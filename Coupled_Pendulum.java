import java.awt.*;
import java.applet.*;
import java.awt.event.*;
import java.lang.Math.*;

//NOTE TO SELF: do not change the control panel, the action listeners, or CONSTANTS

public class Coupled_Pendulum extends Applet implements CONSTANTS2 {
    private Graphics page;
    private Controller controller = new Controller (page);
    //private Swinging_Ball ball1 = new Swinging_Ball (SIZE/2,10,SIZE/2,SIZE-SIZE/5,30,Color.red); 
    //private Swinging_Ball ball2 = new Swinging_Ball (SIZE/2,10,SIZE/2,SIZE-SIZE/5,20,Color.blue); 
    //Swinging_Ball(initial_x,initial_y,final_x,length,size,color)
    
    private Control_Panel controls;
    
    public void init() {
        setVisible (true);
        setSize (SIZE,SIZE);
        page = getGraphics();
        setBackground (Color.white);
    }//end init
    
    public void start() {
        controls = new Control_Panel(Thread.currentThread());
        controls.start();
        controller.pause();
        page.drawString("D=" + SWAY, 10, 20);
        controller.swing(page);
        while (controller.moving()) {
            controller.swing(page);
        }//end while
        //applet terminates at this point
    }//end start
}//end Coupled_Pendulum

class Control_Panel extends Thread implements CONSTANTS2 {
    private Button suspend = new Button("Pause");
    private Button resume = new Button("Resume");
    private Button stop = new Button("Stop");
    private Frame frame = new Frame("Bouncing Ball Control Panel");
    private Thread applet_thread;
    
    Control_Panel(Thread applet_thread){
        this.applet_thread = applet_thread;
    }//end constructor
    
    public void run(){
        Resume_Action resume_action = new Resume_Action(applet_thread);
        Suspend_Action suspend_action = new Suspend_Action(applet_thread);
        Stop_Action stop_action = new Stop_Action(applet_thread);
        stop.addActionListener(stop_action);
        suspend.addActionListener(suspend_action);
        resume.addActionListener(resume_action);
        frame.setLayout(new FlowLayout());
        frame.add(stop);
        frame.add(suspend);
        frame.add(resume);
        frame.pack();
        frame.setLocation(SIZE + 50, SIZE/2);
        frame.setVisible(true);
    }//method run
}//class Control_Panel

class Suspend_Action implements ActionListener {
    Thread applet_thread;
    Suspend_Action (Thread applet_thread) {
        this.applet_thread = applet_thread;
    }//constructor
    
    public void actionPerformed (ActionEvent action) {
        applet_thread.suspend();
    }//method actionPerformed
}//class Suspend_Action

class Stop_Action implements ActionListener {
    Thread applet_thread;
    Stop_Action (Thread applet_thread) {
        this.applet_thread = applet_thread;
    }//constructor
    
    public void actionPerformed (ActionEvent action) {
        applet_thread.stop();
    }//method actionPerformed
}//class Stop_Action

class Resume_Action implements ActionListener {
    Thread applet_thread;
    Resume_Action (Thread applet_thread) {
        this.applet_thread = applet_thread;
    }//constructor
    
    public void actionPerformed (ActionEvent action) {
        applet_thread.resume();
    }//method actionPerformed
}//class Resume_Action

class Swinging_Ball2 extends Shared implements CONSTANTS2 {
    private Color on_color;
    private int x_o, y_o, end_x, length, end_y, ball_size = 10;
    
    public Swinging_Ball2 (int new_x, int new_y, int new_end_x, int new_length, int bs, Color c) {
        x_o = new_x;
        y_o = new_y;
        end_x = new_end_x;
        length = new_length;
        end_y = yCalc(end_x);
        ball_size = bs;
        on_color = c;
    }//end constructor
    
    public int yCalc (int dx) {
        int dy = (int) Math.sqrt(length*length - (x_o-dx)*(x_o-dx));
        return (y_o + dy);
    }//end yCalc
    
    public void draw_ball (Graphics page) {
        page.setColor(on_color);
        page.fillOval(end_x - ball_size/2, end_y - ball_size/2, ball_size, ball_size);
        page.drawLine(x_o, y_o, end_x, end_y);
    }//end draw_ball
    
    public void erase_ball (Graphics page) {
        page.setColor(Color.white);
        page.fillOval(end_x - ball_size/2, end_y - ball_size/2, ball_size, ball_size);
        page.drawLine(x_o, y_o, end_x, end_y);
    }//end draw_ball
    
    public void set_x (int new_x) { //sets new value of x (displacement)
        end_x = x_o + new_x;
        end_y = yCalc(end_x);
    }//end set_x
    
    public int get_x () {
        return (end_x - x_o);
    }//end get_x
    
}//end Swinging_Ball2



class Controller extends Shared {
    private Graphics page;
    private int dampening = 10 * SWAY, DX = MOVE, dis1, dis2; //dis1 = displacement ball1, dis2 = displacement ball2
    private final float S = .1f, M = 5, Wo_SQR = 0.25f; //coupling constant, mass, frequency squared
    private final float A2 = .5f, A1 = (1-A2), P1 = 1/(2*A2); //Runge-Kutta Constants
    private float k1 = 0, k2 = 0, z1 = -1, z2 = 0; //parts for Runge-Kutta
    private float X = 0, Y = 0; //X = front (ball1), Y = back (ball2)
    private int i = 0;
    
    private Swinging_Ball2 ball1;
    private Swinging_Ball2 ball2;
    //Swinging_Ball(initial_x,initial_y,final_x,length,size,color)
    
    public Controller (Graphics p) {
        page = p;
        ball1 = new Swinging_Ball2 (SIZE/2,SIZE/10,SIZE/2,SIZE - SIZE/5,44,Color.blue); //front ball
        ball2 = new Swinging_Ball2 (SIZE/2,SIZE/10,SIZE/2,SIZE - SIZE/5,50,Color.red); //back ball
        ball1.set_x(SWAY);
        X = ball1.get_x() + ball2.get_x();
        Y = ball1.get_x() - ball2.get_x();
        //ball2.draw_ball(page);
        //ball1.draw_ball(page);
    }//end constructor
    
    public boolean moving () {
        dampening = (int)(TIMER * dampening);
        return (dampening != 0);
    }//end moving
    
    public void swing (Graphics page) {
        //draw ball2 then ball1, pause, erase the balls, move them (call the integrator)
        ball2.draw_ball(page);
        ball1.draw_ball(page);
        pause();
        ball1.erase_ball(page);
        ball2.erase_ball(page);
        move_balls();
    }//end swiing
    
    private void move_balls () {
        /*
        Methods next_z1 and next_z2 calculate how much to change the z components.
        The new z1 and z2 are then used to calculate the next X and Y. X and Y are used 
        to calculate the displacement of each ball. The Runge-Kutta method is recoded
        in each method.
        */
        //call the Runge-Katta method(s) here <see below comment>
        z1 += next_z1();
        z2 += next_z2();
        X += next_X();
        Y += next_Y();
        //calc the new displacements
        dis1 = (int)((X + Y)/2);
        dis2 = (int)((X - Y)/2);
        //put the new values in their spots
        ball1.set_x(dis1);
        ball2.set_x(dis2);
    }//end move balls
    
    private float next_z1 () {
        k1 = -Wo_SQR * X;
        k2 = -Wo_SQR * (X + P1*DX);
        return (A1*k1 + A2*k2)*DX;
    }//end next_z
    
    private float next_z2 () {
        k1 = -(Wo_SQR + 2*S/M) * Y;
        k2 = -(Wo_SQR + 2*S/M) * (Y + P1*DX);
        return (A1*k1 + A2*k2)*DX;
    }//end next_z
    
    private float next_X () {
        k1 = z1;
        k2 = z1 + P1*DX;
        return (A1*k1 + A2*k2)*DX;
    }//end next_z
    
    private float next_Y () {
        k1 = z2;
        k2 = z2 + P1*DX;
        return (A1*k1 + A2*k2)*DX;
    }//end next_z
}//end Controller

//more an example of an abstract class than for function
abstract class Shared implements CONSTANTS2 {
    public void pause() {
        try {
            Thread.currentThread().sleep(PAUSE);
        } catch (InterruptedException exception) {
            System.out.println ("have an expection");
        }
    }//method pause
}//end Shared

interface CONSTANTS2 { //will hold all constants that need to be global
    int SIZE = 300; //window size, and is basis for pedulum sizes
    int PAUSE = 150; //milliseconds
    int SWAY = SIZE/2 - 50; //how much it will sway, initial displacement for the first pendulum
    int MOVE = 1;
    float TIMER = 0.99f; //only used in class Controller for timing purposes
}//end CONSTANTS