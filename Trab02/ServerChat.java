import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ServerChat extends UnicastRemoteObject implements IServerChat {
    private ArrayList<String> roomList;
    private Map<String, IRoomChat> rooms;

    public ServerChat() throws RemoteException {
        roomList = new ArrayList<>();
        rooms = new HashMap<>();
    }

    @Override
    public synchronized ArrayList<String> getRooms() throws RemoteException {
        return roomList;
    }

    @Override
    public synchronized void createRoom(String roomName) throws RemoteException {
        if (!rooms.containsKey(roomName)) {
            RoomChat newRoom = new RoomChat(roomName);
            rooms.put(roomName, newRoom);
            roomList.add(roomName);
        }
    }

    public static void main(String[] args) {
        try {
            IServerChat server = new ServerChat();
            java.rmi.registry.LocateRegistry.createRegistry(2020).rebind("Servidor", server);
            System.out.println("Server is ready.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}