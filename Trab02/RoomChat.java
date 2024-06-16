import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;

public class RoomChat extends UnicastRemoteObject implements IRoomChat {
    private String roomName;
    private Map<String, IUserChat> userList;

    public RoomChat(String roomName) throws RemoteException {
        this.roomName = roomName;
        this.userList = new HashMap<>();
    }

    @Override
    public synchronized void sendMsg(String usrName, String msg) throws RemoteException {
        for (IUserChat user : userList.values()) {
            user.deliverMsg(usrName, msg);
        }
    }

    @Override
    public synchronized void joinRoom(String usrName, IUserChat user) throws RemoteException {
        userList.put(usrName, user);
        sendMsg("Server", usrName + " joined the room.");
    }

    @Override
    public synchronized void leaveRoom(String usrName) throws RemoteException {
        userList.remove(usrName);
        sendMsg("Server", usrName + " left the room.");
    }

    @Override
    public synchronized void closeRoom() throws RemoteException {
        sendMsg("Server", "Sala fechada pelo servidor.");
        userList.clear();
    }

    @Override
    public String getRoomName() throws RemoteException {
        return roomName;
    }
}