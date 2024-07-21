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
    private InetSocketAddress address;

    private DatagramSocket uni;
    private MulticastSocket mult;
    private Set<InetSocketAddress> members;
    private List<DefaultMessage> messages;

    public StableMulticast(String ip, Integer port, IStableMulticast client) throws IOException {
        this.ip = ip;
        this.port = port;
        this.ipMult = "230.0.0.0";
        this.portMult = 4446;
        this.client = client;
        
        address = new InetSocketAddress(ip, port);

        // Configuração do socket unicast
        uni = new DatagramSocket(port);
        members = new HashSet<>();
        messages = new ArrayList<>();
        // Configuração do socket multicast
        mult = new MulticastSocket(portMult);
        InetAddress group = InetAddress.getByName(ipMult);
        mult.joinGroup(group);
        DefaultMessage message = new DefaultMessage("joined", " ", address, new int[3]);
        sendMulticast(message);
        // Threads para recebimento de mensagens
        new Thread(this::receiveUni).start();
        new Thread(this::receiveMult).start();
    }

    public void msend(String msg, IStableMulticast client) {
        // Solicitando se o envio será para todos ou não
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enviar mensagem para todos? (s/n): ");
        String choice = scanner.nextLine().trim().toLowerCase();
        DefaultMessage message = new DefaultMessage("msg", msg, address, new int[3]);

        if (choice.equals("s")) {
            // Envio multicast
            sendMulticast(message);
        } else {
            System.out.println("Membros disponíveis:" + members);
            // Envio unicast para membros específicos
            for (InetSocketAddress member : members) {
                System.out.print("Enviar mensagem para " + member + "? (Digite qualquer tecla para confirmar): ");
                scanner.nextLine();
                sendUnicast(message, member);
            }
        }
    }

    private void sendMulticast(DefaultMessage message) {
        try {
            String content = message.generateStringMessage();
            byte[] buffer = content.getBytes();
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, InetAddress.getByName(ipMult), portMult);
            mult.send(packet);
            // System.out.println("Mensagem enviada para o grupo multicast.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendUnicast(DefaultMessage message, InetSocketAddress member) {
        try {
            String content = message.generateStringMessage();
            byte[] buffer = content.getBytes();
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, InetAddress.getByName(member.getAddress().getHostAddress()), member.getPort());
            uni.send(packet);
            // System.out.println("Mensagem enviada para " + member.getAddress().getHostAddress() + ":" + member.getPort());
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
                String content = new String(packet.getData(), 0, packet.getLength());
                DefaultMessage msg = DefaultMessage.parseStringMessage(content);
                if(msg.getPrefix() == "already joined") {
                    InetSocketAddress member = new InetSocketAddress(msg.getIP(), msg.getPort());
                    members.add(member);
                }
                else {
                    messages.add(msg);
                }
                client.deliver(msg.getMessageText());
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
                DefaultMessage message = DefaultMessage.parseStringMessage(msg);

                if(message.getIP().equals(ip) && message.getPort() == port) {
                    continue;
                }

                client.deliver(message.getMessageText());
                
                if(message.getPrefix().equals("joined")) {
                    InetSocketAddress member = new InetSocketAddress(message.getIP(), message.getPort());
                    members.add(member);
                    sendUnicast(new DefaultMessage("already joined", " ", address, new int[3]), member);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}