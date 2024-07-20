import java.io.IOException;
import java.util.Scanner;

import StableMulticast.*;

public class Main {
    public static void main(String[] args) throws IOException {
        IStableMulticast client = new IStableMulticast() {
            @Override
            public void deliver(String msg) {
                System.out.println("Received: " + msg);
            }
        };

        StableMulticast middleware = new StableMulticast("230.0.0.0", 4446, client);

        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("Enter message: ");
            String msg = scanner.nextLine();
            middleware.msend(msg, client);
        }
    }
}
