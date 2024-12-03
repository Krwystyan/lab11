package it.unibo.oop.reactivegui03;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.TimeUnit;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * Third experiment with reactive gui.
 */
@SuppressWarnings("PMD.AvoidPrintStackTrace")
public final class AnotherConcurrentGUI extends JFrame {

    private static final long serialVersionUID = 1L;
    private static final double WIDTH_PERC = 0.2;
    private static final double HEIGHT_PERC = 0.1;
    private final JLabel display = new JLabel();
    private final JButton up = new JButton("up");
    private final JButton stop = new JButton("stop");
    private final JButton down = new JButton("down");

    /**
     * Builds a new CGUI.
     */
    public AnotherConcurrentGUI() {
        super();
        final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        this.setSize((int) (screenSize.getWidth() * WIDTH_PERC), (int) (screenSize.getHeight() * HEIGHT_PERC));
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        final JPanel panel = new JPanel();
        panel.add(display);
        panel.add(up);
        panel.add(down);
        panel.add(stop);
        this.getContentPane().add(panel);
        this.setVisible(true);
        /*
         * Create the counter agent and start it. This is actually not so good:
         * thread management should be left to
         * java.util.concurrent.ExecutorService
         */
        Counter c = new Counter();
        final Agent agent = new Agent(c);
        new Thread(agent).start();
        final AgentChecker chek = new AgentChecker(agent);
        new Thread(chek).start();
        
        /*
         * Register a listener that stops it
         */
        up.addActionListener((e) -> agent.increasedTimer());
        down.addActionListener((e) -> agent.decreasedTimer());
        stop.addActionListener((e) -> agent.stopCounting());
    }

    /*
     * The counter agent is implemented as a nested class. This makes it
     * invisible outside and encapsulated.
     */

    private class Counter {
        private static int UP_TIMER = 1;
        private static int DOWN_TIMER = -1;
        private int verseOfTimer = UP_TIMER;
        private int counter=0;

        public synchronized int getCount(){
            return this.counter;
        }
        public void setCount(){
            this.counter+=verseOfTimer;
        }

        public void increasedCount(){
            this.verseOfTimer=UP_TIMER;
        }

        public void decreasedCount(){
            this.verseOfTimer=DOWN_TIMER;
        }

    }

    private class Agent implements Runnable {
        /*
         * Stop is volatile to ensure visibility. Look at:
         * 
         * http://archive.is/9PU5N - Sections 17.3 and 17.4
         * 
         * For more details on how to use volatile:
         * 
         * http://archive.is/4lsKW
         * 
         */
        private volatile boolean stop;
        private Counter counter;

        public Agent(Counter c){
            this.counter = c;
        }

        @Override
        public void run() {
            while (!this.stop) {
                try {
                    // The EDT doesn't access `counter` anymore, it doesn't need to be volatile 
                    final var nextText = Integer.toString(counter.getCount());
                    SwingUtilities.invokeAndWait(() -> AnotherConcurrentGUI.this.display.setText(nextText));
                    counter.setCount();
                    Thread.sleep(100);
                } catch (InvocationTargetException | InterruptedException ex) {
                    /*
                     * This is just a stack trace print, in a real program there
                     * should be some logging and decent error reporting
                     */
                    ex.printStackTrace();
                }
            }
            AnotherConcurrentGUI.this.up.setEnabled(false);
            AnotherConcurrentGUI.this.down.setEnabled(false);
            AnotherConcurrentGUI.this.stop.setEnabled(false);
        }

        /**
         * External command to stop counting.
         */
        public synchronized void stopCounting() {
            this.stop = true;
        }

        public void increasedTimer() {
            counter.increasedCount();;
        }

        public void decreasedTimer() {
            counter.decreasedCount();
        }

    }

    private class AgentChecker implements Runnable {

        private Agent stopAgent;

        public AgentChecker(Agent aj){
            this.stopAgent = aj;
        }

        @Override
        public void run(){
            try {
                Thread.sleep(1000*10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            stopAgent.stopCounting();
        }
    }
}
