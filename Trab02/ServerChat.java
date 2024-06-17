import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

public class ServerChat extends UnicastRemoteObject implements IServerChat {
    private ArrayList<String> roomList;
    private Registry registry;

    protected ServerChat() throws RemoteException {
        super();
        roomList = new ArrayList<>();
    }

    @Override
    public ArrayList<String> getRooms() throws RemoteException {
        return roomList;
    }

    @Override
    public void createRoom(String roomName) throws RemoteException {
        if (!roomList.contains(roomName)) {
            roomList.add(roomName);
            RoomChat room = new RoomChat(roomName);
            registry.rebind(roomName, room);
            updateRoomList();
        }
    }

    private void closeRoom(String roomName) throws RemoteException {
        if (roomList.contains(roomName)) {
            try {
                IRoomChat room = (IRoomChat) registry.lookup(roomName);
                room.closeRoom();
                roomList.remove(roomName);
                registry.unbind(roomName);
                updateRoomList();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void updateRoomList() {
        roomListArea.setText("");
        for (String room : roomList) {
            roomListArea.append(room + "\n");
        }
    }

    private JTextArea roomListArea;

    public static void main(String[] args) {
        try {
            ServerChat server = new ServerChat();
            try {
                server.registry = LocateRegistry.createRegistry(2020); // Cria um novo registry
            } catch (RemoteException e) {
                server.registry = LocateRegistry.getRegistry(2020); // Tenta obter o registry existente se já estiver criado
            }
            server.registry.rebind("Servidor", server);
            System.out.println("Servidor está pronto.");

            // GUI do Servidor
            JFrame frame = new JFrame("Chat do Servidor");
            JTextArea roomListArea = new JTextArea(20, 40);
            roomListArea.setEditable(false);
            server.roomListArea = roomListArea;
            JTextField roomField = new JTextField(20);
            JButton createButton = new JButton("Criar Sala");
            JButton closeButton = new JButton("Fechar Sala");

            frame.setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));
            frame.add(new JScrollPane(roomListArea));
            frame.add(roomField);
            frame.add(createButton);
            frame.add(closeButton);

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

            closeButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String roomName = roomField.getText();
                    try {
                        server.closeRoom(roomName);
                        roomField.setText("");
                    } catch (RemoteException ex) {
                        ex.printStackTrace();
                    }
                }
            });

            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.pack();
            frame.setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}