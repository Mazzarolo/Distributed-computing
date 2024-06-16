import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

public class UserChat extends UnicastRemoteObject implements IUserChat {
    private String userName;

    public UserChat(String userName) throws RemoteException {
        this.userName = userName;
    }

    @Override
    public void deliverMsg(String senderName, String msg) throws RemoteException {
        System.out.println(senderName + ": " + msg);
    }

    public void sendMsg(IRoomChat room, String msg) throws RemoteException {
        room.sendMsg(userName, msg);
    }

    public static void main(String[] args) {
        try {
            java.rmi.registry.Registry registry = java.rmi.registry.LocateRegistry.getRegistry("localhost", 2020);
            IServerChat server = (IServerChat) registry.lookup("Servidor");

            UserChat user = new UserChat("User1");
            ArrayList<String> rooms = server.getRooms();
            System.out.println("Available rooms: " + rooms);

            String roomName = "Room1";
            if (!rooms.contains(roomName)) {
                server.createRoom(roomName);
            }

            IRoomChat room = (IRoomChat) registry.lookup(roomName);
            room.joinRoom(user.userName, user);
            user.sendMsg(room, "Hello, everyone!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}