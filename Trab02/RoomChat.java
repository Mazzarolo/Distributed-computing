import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;

public class RoomChat extends UnicastRemoteObject implements IRoomChat {
    private String roomName;
    private Map<String, IUserChat> userList;

    protected RoomChat(String roomName) throws RemoteException {
        super();
        this.roomName = roomName;
        userList = new HashMap<>();
    }

    @Override
    public void sendMsg(String usrName, String msg) throws RemoteException {
        for (IUserChat user : userList.values()) {
            user.deliverMsg(usrName, msg);
        }
    }

    @Override
    public void joinRoom(String usrName, IUserChat user) throws RemoteException {
        userList.put(usrName, user);
        sendMsg("Sistema", usrName + " entrou na sala.");
    }

    @Override
    public void leaveRoom(String usrName) throws RemoteException {
        userList.remove(usrName);
        sendMsg("Sistema", usrName + " saiu da sala.");
    }

    @Override
    public void closeRoom() throws RemoteException {
        sendMsg("Sistema", "Sala fechada pelo servidor.");
        userList.clear();
    }

    @Override
    public String getRoomName() throws RemoteException {
        return roomName;
    }
}