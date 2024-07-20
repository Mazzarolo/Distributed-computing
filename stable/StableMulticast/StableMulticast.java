package StableMulticast;
import java.net.*;
import java.util.*;
import java.io.*;

public class StableMulticast {
    private String ip;
    private int port;
    private IStableMulticast client;
    private DatagramSocket socket;
    private List<String> buffer;
    private Map<String, int[]> vectorClocks;
    private int[] localClock;

    public StableMulticast(String ip, int port, IStableMulticast client) throws IOException {
        this.ip = ip;
        this.port = port;
        this.client = client;
        this.socket = new DatagramSocket(port);
        this.buffer = new ArrayList<>();
        this.vectorClocks = new HashMap<>();
        this.localClock = new int[1]; // Assuming only one process for simplicity

        new Thread(this::receiveMessages).start();
    }

    public void msend(String msg, IStableMulticast client) throws IOException {
        localClock[0]++;
        String timestampedMsg = msg + "@" + Arrays.toString(localClock);
        byte[] buf = timestampedMsg.getBytes();
        InetAddress group = InetAddress.getByName(ip);
        DatagramPacket packet = new DatagramPacket(buf, buf.length, group, port);
        socket.send(packet);
        buffer.add(timestampedMsg);
    }

    private void receiveMessages() {
        byte[] buf = new byte[256];
        while (true) {
            try {
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);
                String received = new String(packet.getData(), 0, packet.getLength());
                buffer.add(received);
                updateVectorClock(received);
                client.deliver(received.split("@")[0]);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void updateVectorClock(String msg) {
        String[] parts = msg.split("@");
        String[] clockParts = parts[1].replace("[", "").replace("]", "").split(", ");
        int[] clock = Arrays.stream(clockParts).mapToInt(Integer::parseInt).toArray();
        vectorClocks.put(parts[0], clock);
    }
}
