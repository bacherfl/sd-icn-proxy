package itec.proxy;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * Created by florian on 27.04.15.
 */
public class ProxyServer {

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = null;
        boolean listening = true;

        int port = 10000;	//default
        try {
            port = Integer.parseInt(args[0]);
        } catch (Exception e) {
            //ignore me
        }

        String resolverUrl = "localhost";
        try {
            resolverUrl = args[1];
        } catch (Exception e) {

        }

        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Started on: " + port);
        } catch (IOException e) {
            System.err.println("Could not listen on port: " + args[0]);
            System.exit(-1);
        }

        while (listening) {
            new ProxyThread(serverSocket.accept(), resolverUrl).start();
        }
        serverSocket.close();
    }
}
