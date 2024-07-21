package StableMulticast;

import java.io.*;
import java.net.*;
import java.util.*;



public class StableMulticast {
    
    public IStableMulticast client;
    private String ip;
    private Integer port;
    private String ipMult;
    private Integer portMult;

    private DatagramSocket uni;
    private MulticastSocket mult;
    private Set<InetSocketAddress> members;
    private List<String> messages;

    public StableMulticast(String ip, Integer port, IStableMulticast client) throws IOException {
        this.ip = ip;
        this.port = port;
        this.ipMult = "230.0.0.0";
        this.portMult = 4446;
        this.client = client;

        // Configuração do socket unicast
        uni = new DatagramSocket(port);
        members = new HashSet<>();
        messages = new ArrayList<>();
        // Configuração do socket multicast
        mult = new MulticastSocket(portMult);
        InetAddress group = InetAddress.getByName(ipMult);
        mult.joinGroup(group);
        sendMulticast("joined");
        // Threads para recebimento de mensagens
        new Thread(this::receiveUni).start();
        new Thread(this::receiveMult).start();

        // Descoberta e gerenciamento de membros
        getNewMembers();
    }

    public void msend(String msg, IStableMulticast client) {
        // Solicitando se o envio será para todos ou não
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enviar mensagem para todos? (s/n): ");
        String choice = scanner.nextLine().trim().toLowerCase();

        if (choice.equals("s")) {
            // Envio multicast
            sendMulticast(msg);
        } else {
            // Envio unicast para membros específicos
            for (InetSocketAddress member : members) {
                System.out.print("Enviar mensagem para " + member + "? (s/n): ");
                String response = scanner.nextLine().trim().toLowerCase();
                if (response.equals("s")) {
                    sendUnicast(msg, member);
                }
            }
        }
    }

    private void sendMulticast(String msg) {
        try {
            msg = msg + ":" + ip + ":" + port;
            messages.add(msg);
            byte[] buffer = msg.getBytes();
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, InetAddress.getByName(ipMult), portMult);
            mult.send(packet);
            System.out.println("Mensagem enviada para o grupo multicast.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendUnicast(String msg, InetSocketAddress member) {
        try {
            msg = msg + ":" + ip + ":" + port;
            byte[] buffer = msg.getBytes();
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, InetAddress.getByName(member.getAddress().getHostAddress()), member.getPort());
            uni.send(packet);
            System.out.println("Mensagem enviada para " + member.getAddress().getHostAddress() + ":" + member.getPort());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void receiveUni() {
        try {
            byte[] buffer = new byte[1024];
            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                uni.receive(packet);
                String msg = new String(packet.getData(), 0, packet.getLength());
                if(msg.startsWith("already joined")) {
                    String[] parts = msg.split(":");
                    InetSocketAddress member = new InetSocketAddress(parts[1], Integer.parseInt(parts[2]));
                    members.add(member);
                }
                String sender = packet.getAddress().getHostAddress();
                client.deliver(msg);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void receiveMult() {
        try {
            byte[] buffer = new byte[1024];
            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                mult.receive(packet);
                
                String msg = new String(packet.getData(), 0, packet.getLength());
                String[] parts = msg.split(":");

                if(parts[1].equals(ip) && Integer.parseInt(parts[2]) == port) {
                    continue;
                }

                client.deliver(msg);

                if(msg.startsWith("joined")) {
                    InetSocketAddress member = new InetSocketAddress(parts[1], Integer.parseInt(parts[2]));
                    members.add(member);
                    sendUnicast("already joined", member);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void getNewMembers() {
        System.out.println("Descoberta de membros não implementada.");
    }
}