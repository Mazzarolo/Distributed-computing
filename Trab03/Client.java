import StableMulticast.*;
import java.io.IOException;
import java.util.Scanner;

public class Client implements IStableMulticast {
    
    private StableMulticast stableMulticast;

    public Client(String ip, Integer port) throws IOException {
        // Inicializando o middleware
        stableMulticast = new StableMulticast(ip, port, this);
    }

    @Override
    public void deliver(String msg) {
        // Lógica para tratar a entrega da mensagem recebida
        System.out.println(msg);
    }
    
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Uso: java Client <IP> <Port>");
            return;
        }

        String ip = args[0];
        int port = Integer.parseInt(args[1]);

        try {
            Client client = new Client(ip, port);

            Scanner scanner = new Scanner(System.in);
            while (true) {
                // System.out.print("Digite uma mensagem para enviar: ");
                String msg = scanner.nextLine();
                client.stableMulticast.msend(msg, client);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}