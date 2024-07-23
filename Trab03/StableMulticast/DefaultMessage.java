package StableMulticast;

import java.net.InetSocketAddress;
import java.util.Hashtable;



public class DefaultMessage {
    private String prefix;
    private String content;
    private InetSocketAddress address;
    private Hashtable<InetSocketAddress, Integer> vc = new Hashtable<>();
    
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";

    public DefaultMessage(String prefix, String content, InetSocketAddress address, Hashtable<InetSocketAddress, Integer> vc) {
        this.prefix = prefix;
        this.content = content;
        this.address = address;
        this.vc = vc;
    }

    public String getContent() {
        return content;
    }

    public InetSocketAddress getAddress() {
        return address;
    }

    public String getIP() {
        return address.getAddress().getHostAddress();
    }

    public int getPort() {
        return address.getPort();
    }

    public Hashtable<InetSocketAddress, Integer> getVc() {
        return vc;
    }

    public void setVc(Hashtable<InetSocketAddress, Integer> vc) {
        this.vc = vc;
    }

    public String getPrefix() {
        return prefix;
    }

    public String vcToString() {
        String vcString = "";
        for (InetSocketAddress key : vc.keySet()) {
            vcString += key.getAddress().getHostAddress() + "->" + key.getPort() + "=" + vc.get(key) + ",";
        }
        return vcString;
    }

    public String generateStringMessage() {
        return prefix + ":" + content + ":" + address.getAddress().getHostAddress() + ":" + address.getPort() + ":" + vcToString();
    }

    public static DefaultMessage parseStringMessage(String msg) {
        String[] parts = msg.split(":");
        String prefix = parts[0];
        String content = parts[1];
        InetSocketAddress address = new InetSocketAddress(parts[2], Integer.parseInt(parts[3]));

        Hashtable<InetSocketAddress, Integer> vc = new Hashtable<>();
        String[] vcParts = parts[4].split(",");
        for (String vcPart : vcParts) {
            String[] vcPair = vcPart.split("=");
            InetSocketAddress vcAddress = new InetSocketAddress(vcPair[0].split("->")[0], Integer.parseInt(vcPair[0].split("->")[1]));
            vc.put(vcAddress, Integer.parseInt(vcPair[1]));
        }
        return new DefaultMessage(prefix, content, address, vc);
    }

    public String getMessageText() {
        String ip = address.getAddress().getHostAddress();
        String content = this.content;
        String port = String.valueOf(address.getPort());
        
        int terminalWidth = 80; // Largura do terminal, ajuste conforme necessário
        int contentWidth = terminalWidth - ip.length() - port.length() - 2; // 2 espaços para separadores
        
        if (content.length() > contentWidth) {
            content = content.substring(0, contentWidth); // Trunca o conteúdo se for maior que o espaço disponível
        }
        
        String formattedContent = String.format("%-" + contentWidth + "s", content); // Formata o conteúdo para ocupar exatamente o espaço necessário
        if(prefix.equals("joined") | prefix.equals("already joined")) {
            return ANSI_GREEN + ip + ":" + port + "  " + prefix + ANSI_RESET;
        }
        return "\n" + ANSI_BLUE + ip + ANSI_RESET + String.format("%40s", " ") + ANSI_GREEN + formattedContent + ANSI_RESET + " " + ANSI_RED + port + ANSI_RESET;
    }
}