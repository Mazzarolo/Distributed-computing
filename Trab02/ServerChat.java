import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ServerChat extends UnicastRemoteObject implements IServerChat {
    private ArrayList<String> roomList;
    private Map<String, IRoomChat> rooms;

    private JFrame frame;
    private DefaultListModel<String> listModel;
    private JList<String> roomJList;
    private JButton closeButton;
    private JButton createButton;
    private JTextField roomTextField;

    protected ServerChat() throws RemoteException {
        super();
        roomList = new ArrayList<>();
        rooms = new HashMap<>();
        initGUI();
    }

    private void initGUI() {
        frame = new JFrame("Server Chat");
        listModel = new DefaultListModel<>();
        roomJList = new JList<>(listModel);
        closeButton = new JButton("Close Room");
        createButton = new JButton("Create Room");
        roomTextField = new JTextField(20);

        frame.setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));
        frame.add(new JScrollPane(roomJList));
        frame.add(roomTextField);
        frame.add(createButton);
        frame.add(closeButton);

        createButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String roomName = roomTextField.getText();
                try {
                    createRoom(roomName);
                    roomTextField.setText("");
                } catch (RemoteException ex) {
                    ex.printStackTrace();
                }
            }
        });

        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedRoom = roomJList.getSelectedValue();
                if (selectedRoom != null) {
                    try {
                        rooms.get(selectedRoom).closeRoom();
                        roomList.remove(selectedRoom);
                        listModel.removeElement(selectedRoom);
                        rooms.remove(selectedRoom);
                    } catch (RemoteException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        try {
            ServerChat server = new ServerChat();
            Registry registry = LocateRegistry.createRegistry(2020);
            registry.rebind("Servidor", server);
            System.out.println("Servidor de Chat está rodando...");
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public ArrayList<String> getRooms() throws RemoteException {
        return roomList;
    }

    @Override
    public void createRoom(String roomName) throws RemoteException {
        if (!rooms.containsKey(roomName)) {
            RoomChat newRoom = new RoomChat(roomName);
            rooms.put(roomName, newRoom);
            roomList.add(roomName);
            listModel.addElement(roomName);
        }
    }
}
