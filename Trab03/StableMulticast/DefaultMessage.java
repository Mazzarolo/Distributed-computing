package StableMulticast;

import java.net.InetSocketAddress;

public class DefaultMessage {
    private String prefix;
    private String content;
    private InetSocketAddress address;
    private int[] vc;


    public DefaultMessage(String prefix, String content, InetSocketAddress address, int[] vc) {
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

    public int[] getVc() {
        return vc;
    }

    public String getPrefix() {
        return prefix;
    }

    public String vcToString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < vc.length; i++) {
            sb.append(vc[i]);
            if (i < vc.length - 1) {
                sb.append(",");
            }
        }
        return sb.toString();
    }

    public String generateStringMessage() {
        return prefix + ":" + content + ":" + address.getAddress().getHostAddress() + ":" + address.getPort() + ":" + vcToString();
    }

    public static DefaultMessage parseStringMessage(String msg) {
        String[] parts = msg.split(":");
        String prefix = parts[0];
        String content = parts[1];
        InetSocketAddress address = new InetSocketAddress(parts[2], Integer.parseInt(parts[3]));
        String[] vcParts = parts[4].split(",");
        int[] vc = new int[vcParts.length];
        for (int i = 0; i < vcParts.length; i++) {
            vc[i] = Integer.parseInt(vcParts[i]);
        }
        return new DefaultMessage(prefix, content, address, vc);
    }

    public String getMessageText() {
        return prefix + ": " + content + " - " + address.getAddress().getHostAddress() + ":" + address.getPort() + " - [" + vcToString() + "]";
    }
}