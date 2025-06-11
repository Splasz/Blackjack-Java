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
    public JTextField PlayerCards;
    public JTextField PlayerPoints;
    private JPanel MainPanel;
    private JPanel GameLog;
    private JPanel InfoCard;
    private JPanel Buttons;
    private JPanel CroupierCard;
    private JPanel PlayerCard;
    private JPanel CroupierInfo;
    private JPanel PlayerInfo;
    private JLabel CroupierLabel;
    public JLabel PlayerLabel;

    public JPanel getMainPanel() {
        return MainPanel;
    }

    public JButton getStartButton() {
        return startButton;
    }


}


