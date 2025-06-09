package GUI;

import javax.swing.*;

public class BlackjackGUI {
    public JTextArea displayLog;
    public JButton startButton;
    public JButton wyjdzButton;
    public JButton hitButton;
    public JButton standButton;
    public JTextField CroupierCards;
    public JTextField CroupierPoints;
    private JTextField PlayerCards;
    private JTextField PlayerPoints;
    private JPanel Main;
    private JPanel GameLog;
    private JPanel InfoCard;
    private JPanel Buttons;
    private JPanel CroupierCard;
    private JPanel PlayerCard;
    private JPanel CroupierInfo;
    private JPanel PlayerInfo;
    private JLabel CroupierLabel;
    private JLabel PlayerLabel;

    public JPanel getMainPanel() {
        return Main;
    }

    public JButton getStartButton() {
        return startButton;
    }


}


