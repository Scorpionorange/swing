import javax.swing.*;
import java.awt.*;
import java.util.Random;

/**
 * Created by ScorpionOrange on 2016/11/20.
 * This program demonstrates that a thread that runs in parallel with the event dispatch
 * thread can cause errors in Swing components.
 */
public class SwingThreadTest {
    public static void main(String[] args){
        EventQueue.invokeLater(() -> {
            JFrame frame = new SwingThreadFrame();
            frame.setTitle("SwingThreadTest");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);
        });
    }
}

/**
 * This frame has two buttons to fill a combo box from a separate thread.
 * The "Good" button uses the event queue, the "Bad" button modifies the combo box directly.
 */
class SwingThreadFrame extends JFrame{
    public SwingThreadFrame(){
        final JComboBox<Integer> comboBox = new JComboBox<>();
        comboBox.insertItemAt(Integer.MAX_VALUE, 0);
        comboBox.setPrototypeDisplayValue(comboBox.getItemAt(0));
        comboBox.setSelectedIndex(0);

        JPanel panel = new JPanel();

        JButton goodButton = new JButton("Good");
        goodButton.addActionListener(event -> new Thread(new GoodWorkerRunnable(comboBox)).start());
        panel.add(goodButton);

        JButton badButton = new JButton("Bad");
        badButton.addActionListener(event -> new Thread(new BadWorkerRunnable(comboBox)).start());
        panel.add(badButton);

        panel.add(comboBox);
        add(panel);
        pack();
    }
}

/**
 * This runnable modifies a combo box by randomly adding and removing number.
 * In order to ensure that the combo box is not corrupted, the editing operations are forward
 * to the event dispatch thread.
 */
class GoodWorkerRunnable implements Runnable{
    private JComboBox<Integer> comboBox;
    private Random generator;

    public GoodWorkerRunnable(JComboBox<Integer> aCombo){
        comboBox = aCombo;
        generator = new Random();
    }

    public void run(){
        try{
            while (true){
                EventQueue.invokeLater(() -> {
                    int i = Math.abs(generator.nextInt());
                    if(i % 2 == 0) {
                        comboBox.insertItemAt(i, 0);
                    }
                    else if(comboBox.getItemCount() > 0){
                        comboBox.removeItemAt(i % comboBox.getItemCount());
                    }
                });
                Thread.sleep(1);
            }
        }
        catch (InterruptedException e){}
    }
}

/**
 * This runnable modifies a combo box by randomly adding and removing numbers.
 * This can result in errors because the combo box methods are not synchronized and both
 * the worker thread and the event dispatch thread access the combo box.
 */
class BadWorkerRunnable implements Runnable{
    private JComboBox<Integer> comboBox;
    private Random generator;

    public BadWorkerRunnable(JComboBox<Integer> aCombo){
        comboBox = aCombo;
        generator = new Random();
    }

    public void run(){
        try{
            while (true){
                int i = Math.abs(generator.nextInt());
                if(i % 2 == 0) {
                    comboBox.insertItemAt(i, 0);
                }
                else if (comboBox.getItemCount() > 0) {
                    comboBox.removeItemAt(i % comboBox.getItemCount());
                }
                Thread.sleep(1);
            }
        }
        catch (InterruptedException e){}
    }
}
