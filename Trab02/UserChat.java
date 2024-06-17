import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

public class UserChat extends UnicastRemoteObject implements IUserChat {
    private String usrName;
    private IServerChat server;
    private IRoomChat currentRoom;
    private String serverIp;

    private JFrame frame;
    private JTextArea chatArea;
    private JTextField messageField;
    private JTextField roomField;
    private JButton sendButton;
    private JButton joinButton;
    private JButton leaveButton;
    private JButton createButton;
    private JButton refreshButton;
    private JList<String> roomJList;
    private DefaultListModel<String> listModel;

    protected UserChat(String usrName, IServerChat server, String serverIp) throws RemoteException {
        super();
        this.usrName = usrName;
        this.server = server;
        this.serverIp = serverIp;
        initGUI();
    }

    private void initGUI() {
        frame = new JFrame("Chat de Usuario: " + usrName);
        chatArea = new JTextArea(20, 40);
        chatArea.setEditable(false);
        messageField = new JTextField(30);
        roomField = new JTextField(20);
        sendButton = new JButton("Enviar");
        joinButton = new JButton("Entrar na Sala");
        leaveButton = new JButton("Sair da Sala");
        createButton = new JButton("Criar Sala");
        refreshButton = new JButton("Recarregar Salas");
        listModel = new DefaultListModel<>();
        roomJList = new JList<>(listModel);

        frame.setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));
        frame.add(new JScrollPane(chatArea));
        frame.add(new JScrollPane(roomJList));
        frame.add(roomField);
        frame.add(createButton);
        frame.add(joinButton);
        frame.add(leaveButton);
        frame.add(refreshButton);
        frame.add(messageField);
        frame.add(sendButton);

        createButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String roomName = roomField.getText();
                try {
                    server.createRoom(roomName);
                    roomField.setText("");
                } catch (RemoteException ex) {
                    ex.printStackTrace();
                }
            }
        });

        joinButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String roomName = roomJList.getSelectedValue();
                if (roomName != null) {
                    try {
                        currentRoom = (IRoomChat) LocateRegistry.getRegistry(serverIp, 2020).lookup(roomName);
                        currentRoom.joinRoom(usrName, UserChat.this);
                        chatArea.append("Entrou na Sala: " + roomName + "\n");
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });

        leaveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (currentRoom != null) {
                    try {
                        currentRoom.leaveRoom(usrName);
                        chatArea.append("Saiu da Sala\n");
                        currentRoom = null;
                    } catch (RemoteException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });

        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (currentRoom != null) {
                    String msg = messageField.getText();
                    try {
                        currentRoom.sendMsg(usrName, msg);
                        messageField.setText("");
                    } catch (RemoteException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });

        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateRoomList();
            }
        });

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);

        updateRoomList();
    }

    private void updateRoomList() {
        try {
            ArrayList<String> rooms = server.getRooms();
            listModel.clear();
            for (String room : rooms) {
                listModel.addElement(room);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Inserir o ip <server-ip>");
            System.exit(1);
        }

        String serverIp = args[0];

        try {
            Registry registry = LocateRegistry.getRegistry(serverIp, 2020);
            IServerChat server = (IServerChat) registry.lookup("Servidor");

            String usrName = JOptionPane.showInputDialog("Digite seu nome de usuario:");
            if (usrName != null && !usrName.trim().isEmpty()) {
                new UserChat(usrName, server, serverIp);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deliverMsg(String senderName, String msg) throws RemoteException {
        chatArea.append(senderName + ": " + msg + "\n");
    }
}