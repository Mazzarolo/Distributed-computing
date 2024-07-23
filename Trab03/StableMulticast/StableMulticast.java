package StableMulticast;

import java.io.*;
import java.net.*;
import java.util.*;



public class StableMulticast {
    static final int N = 3;
    public IStableMulticast client;
    private String ip;
    private Integer port;
    private String ipMult;
    private Integer portMult;
    private InetSocketAddress address;
    private int[][] mc;

    private DatagramSocket uni;
    private MulticastSocket mult;
    private Set<InetSocketAddress> members;
    private HashMap<InetSocketAddress, Integer> memberIds;
    private List<DefaultMessage> messages;

    public StableMulticast(String ip, Integer port, IStableMulticast client) throws IOException {
        this.ip = ip;
        this.port = port;
        this.ipMult = "230.0.0.0";
        this.portMult = 4446;
        this.client = client;
        
        address = new InetSocketAddress(ip, port);
        mc = new int[N][N];
        initiateMC();

        // Configuração do socket unicast
        uni = new DatagramSocket(port);
        members = new HashSet<>();
        messages = new ArrayList<>();
        memberIds = new HashMap<>();

        members.add(address);
        memberIds.put(address, 0);
        // Configuração do socket multicast
        mult = new MulticastSocket(portMult);
        InetAddress group = InetAddress.getByName(ipMult);
        mult.joinGroup(group);

        Hashtable<InetSocketAddress, Integer> vc = new Hashtable<>();
        vc.put(new InetSocketAddress(ip, port), 0);
        for(int i = 1; i < N; i++) {
            vc.put(new InetSocketAddress("0.0.0.0", 0), -1);
        }

        DefaultMessage message = new DefaultMessage("joined", "", address, vc);
        
        sendMulticast(message);
        // Threads para recebimento de mensagens
        new Thread(this::receiveUni).start();
        new Thread(this::receiveMult).start();
    }

    private void initiateMC() {
        for (int i = 0; i < mc.length; i++) {
            for (int j = 0; j < mc[i].length; j++) {
                mc[i][j] = -1;
            }
        }

        mc[0][0] = 0;
    }

    public void printBuffer(){
        System.out.println("Buffer de mensagens:");
        for(DefaultMessage m : messages){
            System.out.println(m.getIP() + ":" + m.getPort() + " - " + m.getContent());
        }
    }

    public void printMC() {
        // Cria um mapa de ID de membro para IPs e portas
        HashMap<Integer, InetSocketAddress> idToMember = new HashMap<>();
        for (Map.Entry<InetSocketAddress, Integer> entry : memberIds.entrySet()) {
            idToMember.put(entry.getValue(), entry.getKey());
        }
        System.out.print("              ");
        // Imprime os cabeçalhos das colunas
        for (int i = 0; i < N; i++) {
            if (idToMember.containsKey(i)) {
                InetSocketAddress member = idToMember.get(i);
                System.out.print(member.getAddress().getHostAddress() + ":" + member.getPort() + "  ");
            }
            
        }
        System.out.println();
        // Imprime as linhas da matriz com os IPs e portas corretos
        for (int i = 0; i < N; i++) {
            if (idToMember.containsKey(i)) {
                InetSocketAddress member = idToMember.get(i);
                System.out.print(member.getAddress().getHostAddress() + ":" + member.getPort() + "  ");
            }
            for (int j = 0; j < N; j++) {
                System.out.printf("%4d ", mc[i][j]);
                System.out.print(String.format("%14s", " "));
            }
            System.out.println();
        }

        printBuffer();
    }
    

    public void msend(String msg, IStableMulticast client) {
        // Solicitando se o envio será para todos ou não
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enviar mensagem para todos? (s/n): ");
        String choice = scanner.nextLine().trim().toLowerCase();

        Hashtable<InetSocketAddress, Integer> vc = new Hashtable<>();

        for(InetSocketAddress member : members) {
            vc.put(member, mc[0][memberIds.get(member)]);
        }

        DefaultMessage message = new DefaultMessage("msg", msg, address, vc);
        
        

        if (choice.equals("s")) {
            // Envio multicast
            sendMulticast(message);
        } else {
            // Envio unicast para membros específicos
            for (InetSocketAddress member : members) {
                System.out.print("Enviar mensagem para " + member + "? (Digite qualquer tecla para confirmar): ");
                scanner.nextLine();
                sendUnicast(message, member);
            }
        }
        
        mc[0][0] += 1;

        this.printMC();
        
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
                
                if(msg.getPrefix().equals("msg")){
                    messages.add(msg);

                    Iterator<DefaultMessage> iterator = messages.iterator();
                    while (iterator.hasNext()) {
                        DefaultMessage m = iterator.next();
                        
                        InetSocketAddress messageMember = new InetSocketAddress(m.getIP(), m.getPort());
                        Integer messageMemberIdx = memberIds.get(messageMember);
                        Boolean remove = true;
                        
                        for (int i = 0; i < N; i++) {
                            if (mc[i][messageMemberIdx] < m.getVc().get(messageMember)) {
                                remove = false;
                            }
                        }
                        
                        if (remove) {
                            iterator.remove();
                        }
                    }

                    if(!(msg.getIP().equals(ip) && msg.getPort() == port)) {
                        Integer idx = memberIds.get(new InetSocketAddress(msg.getIP(), msg.getPort()));
                        
                        Enumeration<InetSocketAddress> e = msg.getVc().keys();

                        while(e.hasMoreElements()) {
                            InetSocketAddress key = e.nextElement();
                            mc[idx][memberIds.get(key)] = Math.max(mc[idx][memberIds.get(key)], msg.getVc().get(key));

                        }
                        mc[0][idx] = Math.max(mc[0][idx], msg.getVc().get(new InetSocketAddress(msg.getIP(), msg.getPort())));
                        this.printMC();
                    }
                }

                if(msg.getIP().equals(ip) && msg.getPort() == port) {
                    continue;
                }

                if(msg.getPrefix().equals("already joined")) {
                    InetSocketAddress member = new InetSocketAddress(msg.getIP(), msg.getPort());
                    members.add(member);
                    memberIds.put(member, members.size() - 1);
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

                if(message.getPrefix().equals("msg")) {
                    messages.add(message);

                    Iterator<DefaultMessage> iterator = messages.iterator();
                    while (iterator.hasNext()) {
                        DefaultMessage m = iterator.next();
                        
                        InetSocketAddress messageMember = new InetSocketAddress(m.getIP(), m.getPort());
                        Integer messageMemberIdx = memberIds.get(messageMember);
                        Boolean remove = true;
                        
                        for (int i = 0; i < N; i++) {
                            if (mc[i][messageMemberIdx] < m.getVc().get(messageMember)) {
                                remove = false;
                            }
                        }
                        
                        if (remove) {
                            iterator.remove();
                        }
                    }


                    if(!(message.getIP().equals(ip) && message.getPort() == port)) {
                        Integer idx = memberIds.get(new InetSocketAddress(message.getIP(), message.getPort()));
                        
                        Enumeration<InetSocketAddress> e = message.getVc().keys();

                        while(e.hasMoreElements()) {
                            InetSocketAddress key = e.nextElement();
                            mc[idx][memberIds.get(key)] = Math.max(mc[idx][memberIds.get(key)], message.getVc().get(key));
                        }
                        mc[0][idx] = Math.max(mc[0][idx], message.getVc().get(new InetSocketAddress(message.getIP(), message.getPort())));
                        this.printMC();
                    }
                    
                }

                if(message.getIP().equals(ip) && message.getPort() == port) {
                    continue;
                }
                
                if(message.getPrefix().equals("joined")) {
                    InetSocketAddress member = new InetSocketAddress(message.getIP(), message.getPort());
                    members.add(member);
                    memberIds.put(member, members.size() - 1);

                    Hashtable<InetSocketAddress, Integer> vc = new Hashtable<>();
                    
                    vc.put(new InetSocketAddress(ip, port), 0);
                    for(int i = 1; i < N; i++) {
                        vc.put(new InetSocketAddress("0.0.0.0", 0), -1);
                    }


                    sendUnicast(new DefaultMessage("already joined", "", address, vc), member);
                }

                client.deliver(message.getMessageText());
                
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}