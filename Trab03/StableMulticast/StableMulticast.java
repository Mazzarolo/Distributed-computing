package StableMulticast;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class StableMulticast {
    
    public IStableMulticast client;
    private String ip;
    private Integer port;
    private String ipMult;
    private Integer portMult;

    private DatagramSocket uni;
    private MulticastSocket mult;
    private Set<String> members = ConcurrentHashMap.newKeySet(); // Membros do grupo multicast

    public StableMulticast(String ip, Integer port, IStableMulticast client) throws IOException {
        this.ip = ip;
        this.port = port;
        this.ipMult = "230.0.0.0";
        this.portMult = 4446;
        this.client = client;

        // Configuração do socket unicast
        uni = new DatagramSocket(port);

        // Configuração do socket multicast
        mult = new MulticastSocket(portMult);
        InetAddress group = InetAddress.getByName(ipMult);
        mult.joinGroup(group);

        // Threads para recebimento de mensagens
        new Thread(this::receiveUni).start();
        new Thread(this::receiveMult).start();

        // Descoberta e gerenciamento de membros
        discoverMembers();
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
            for (String member : members) {
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
            byte[] buffer = msg.getBytes();
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, InetAddress.getByName(ipMult), portMult);
            mult.send(packet);
            System.out.println("Mensagem enviada para o grupo multicast.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendUnicast(String msg, String address) {
        try {
            DatagramSocket socket = new DatagramSocket();
            byte[] buffer = msg.getBytes();
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, InetAddress.getByName(address), port);
            socket.send(packet);
            System.out.println("Mensagem enviada para " + address);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void receiveUni() {
        try (DatagramSocket socket = new DatagramSocket(port)) {
            byte[] buffer = new byte[1024];
            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                String msg = new String(packet.getData(), 0, packet.getLength());
                String sender = packet.getAddress().getHostAddress();
                System.out.println("Recebido de " + sender + ": " + msg);
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
                System.out.println("Recebido do multicast: " + msg);
                client.deliver(msg);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void discoverMembers() {
        // Implementar descoberta dinâmica dos membros (opcional)
        // Você pode usar um serviço separado para gerenciar e atualizar a lista de membros
        System.out.println("Descoberta de membros não implementada.");
    }
}