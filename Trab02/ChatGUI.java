import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ChatGUI extends JFrame {
    private JTextArea chatArea;
    private JTextField inputField;
    private JButton sendButton;
    private JButton joinButton;
    private JButton leaveButton;
    private JButton closeButton;
    private JComboBox<String> roomList;

    public ChatGUI() {
        setTitle("Sala de Chat");
        setSize(400, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        inputField = new JTextField(20);
        sendButton = new JButton("SEND");
        joinButton = new JButton("JOIN");
        leaveButton = new JButton("LEAVE");
        closeButton = new JButton("CLOSE");
        roomList = new JComboBox<>();

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(new JScrollPane(chatArea), BorderLayout.CENTER);

        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new FlowLayout());
        inputPanel.add(inputField);
        inputPanel.add(sendButton);

        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new FlowLayout());
        controlPanel.add(roomList);
        controlPanel.add(joinButton);
        controlPanel.add(leaveButton);
        controlPanel.add(closeButton);

        panel.add(inputPanel, BorderLayout.SOUTH);
        panel.add(controlPanel, BorderLayout.NORTH);

        add(panel);

        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Handle send button action
            }
        });

        joinButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Handle join button action
            }
        });

        leaveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Handle leave button action
            }
        });

        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Handle close button action
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new ChatGUI().setVisible(true);
            }
        });
    }
}