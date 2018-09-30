import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

/**
 * @author BorisMirage
 * Time: 2018/09/10 10:05
 * Created with IntelliJ IDEA
 */

class BinaryServerThread implements Runnable {

    private Socket sock;
    private LRUCache store;
    private int protocol = 0;       // Default TCP

    public BinaryServerThread(Socket s, LRUCache cache, int protocol) {
        this.sock = s;
        store = cache;
        this.protocol = protocol;
    }

    /**
     * Server running thread.
     * Client message format:
     * [mode] [length] [key 1] [value 1] ... [key n] [value n]
     */
    public void run() {

        try {
            DataInputStream in = new DataInputStream(sock.getInputStream());
            System.out.println("Got connection from " + sock.getInetAddress());
            byte id = in.readByte();
            int intID = (int) id;

            if (intID == -1) {
                binary(in);
            } else {
                ascii(in);
            }

            // clean things up
            in.close();
            sock.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Deal with message that send from binary client (Client.java).
     *
     * @param in input data stream
     */
    private void binary(DataInputStream in) {

        try {
            DataOutputStream out = new DataOutputStream(sock.getOutputStream());

            /* Read and store incoming key-value pair */
            int length = (int) in.readByte();
            System.out.println(length);
            BinaryCoder receiveDecoder = new BinaryCoder();
            Message receiveMessage;
            byte[] data = new byte[length];
            in.readFully(data, 0, length);
            receiveMessage = receiveDecoder.toMsg(data);
            BinaryCoder outEncoder = new BinaryCoder();

            if (receiveMessage.getOp().equals("stats")) {
                stats(out, outEncoder);
            } else if (receiveMessage.getOp().equals("get")) {
                get(receiveMessage.getKey(), out, outEncoder);
            } else if (receiveMessage.getOp().equals("set")) {
                set(receiveMessage.getKey(), receiveMessage.getVal(), out, outEncoder);
            } else if (receiveMessage.getOp().equals("exit")) {
                System.out.println("Exit operation received, system offline. ");
                exit(out, outEncoder);
                System.exit(0);

            }
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Deal with the message that based on ASCII protocol (telnet).
     *
     * @param in input data stream
     */
    private void ascii(DataInputStream in) {
        try {

            /* ASCII support */
            BufferedReader data = new BufferedReader(new InputStreamReader(in));
            in.readByte();
            PrintWriter outPrintWriter = new PrintWriter(sock.getOutputStream(), true);
            String key;
            String val;

            /* Read operation */
            String input = data.readLine();
            String[] arr = input.split(" ");
            System.out.println(Arrays.toString(arr));
            if (arr[0].equals("set")) {
                for (int i = 1; i < arr.length; i += 2) {
                    try {
                        key = arr[i];
                        val = arr[i + 1];
                        store.put(key, val);
                        outPrintWriter.println(String.format("Server: Put [%s] - [%s] into server! ", key, val));
                    } catch (IndexOutOfBoundsException e) {
                        outPrintWriter.println("Input arguments error! ");
                        System.out.println("Input arguments error! ");
                    }
                }
            }
            if (arr[0].equals("get")) {
                for (int i = 1; i < arr.length; i++) {
                    key = arr[i];
                    if (store.get(key) != null) {
                        outPrintWriter.println(String.format("Server: GET [%s] - [%s] ", key, store.get(key)));
                    } else {
                        outPrintWriter.println(String.format("Server: [%s] is not found in server! ", key));
                    }
                }
            }
            if (arr[0].equals("stats")) {
                outPrintWriter.println("Server: " + store.stats());
                outPrintWriter.flush();
            }

            data.close();
            outPrintWriter.flush();
            outPrintWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Return STATS to server.
     *
     * @param out socket output PrintWriter
     */
    private void stats(DataOutputStream out, BinaryCoder outEncoder) {
        System.out.println("STATS: " + store.stats());

        Message statsResponse = new Message(false, true, "stats");
        statsResponse.setVal("STATS: " + store.stats());
        statsResponse.setKey(" ");
        try {
            byte[] responseBytes = outEncoder.toBinary(statsResponse);
            out.writeInt(responseBytes.length);
            out.write(responseBytes);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Return value according to given key.
     *
     * @param k   key that client requested
     * @param out socket output DataOutputStream
     */
    private void get(String k, DataOutputStream out, BinaryCoder outEncoder) {
        System.out.println(String.format("Request GET [%s] ", k));
        String m;
        String v;
        Message getResponse = new Message(false, true, "get");
        if (store.get(k) != null) {

            /* If key found in server */
            v = store.get(k);
            m = String.format("Request Key-Value pair [%s] - [%s]", k, v);
            System.out.println(String.format("Request Key-Value pair [%s] - [%s]", k, v));

        } else {

            /* If key was not found in server */
            m = String.format("Key [%s] does not exist in server! ", k);
            System.out.println(String.format("Key [%s] does not exist in server! ", k));
        }
        getResponse.setKey(" ");
        getResponse.setVal(m);

        try {
            byte[] responseBytes = outEncoder.toBinary(getResponse);
            out.writeInt(responseBytes.length);
            out.write(responseBytes);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Store key-value pair according to given key-value pair.
     * If the given key has been in server, server will rewrite this represented value.
     *
     * @param k          ArrayList that temporary store key-value pair send from client
     * @param v          socket output PrintWriter
     * @param out        output DataOutputStream
     * @param outEncoder encoder
     */
    private void set(String k, String v, DataOutputStream out, BinaryCoder outEncoder) {
        String m;
        Message setResponse = new Message(false, true, "set");
        if (store.get(k) != null) {

            /* If duplicate key was found in hash map */
            m = String.format("Duplicate key [%s] found in server. Rewrite to [%s]", k, v);
            System.out.println(String.format("Duplicate key [%s] found in server. Rewrite to [%s]", k, v));

        } else {
            m = String.format("Request SET [%s] [%s]. ", k, v);
            System.out.println(String.format("Request SET [%s] [%s]. ", k, v));
        }
        store.put(k, v);
        setResponse.setKey(" ");
        setResponse.setVal(m);
        try {
            byte[] responseBytes = outEncoder.toBinary(setResponse);
            out.writeInt(responseBytes.length);
            out.write(responseBytes);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();

        }
    }

    private void exit(DataOutputStream out, BinaryCoder outEncoder) {
        Message statsResponse = new Message(false, true, "exit");
        statsResponse.setVal("Exit operation received, system offline. ");
        statsResponse.setKey(" ");
        try {
            byte[] responseBytes = outEncoder.toBinary(statsResponse);
            out.writeInt(responseBytes.length);
            out.write(responseBytes);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

/**
 * This server will create a thread that deal with the connection.
 */
public class Server {

    public Server() {
        int cacheCapacity = 1000;
        LRUCache cache = new LRUCache(cacheCapacity);
//        HashMap<String, String> store = new HashMap<>();        // Same map that sharing to both ASCII and binary client
        ServerSocket binarySocket = null;

        boolean listening = true;

        try {
            InetAddress addr = InetAddress.getLocalHost();

            // Get IP Address
            byte[] ipAddr = addr.getAddress();

            // Get hostname
            String hostname = addr.getHostAddress();
            // System.out.println("Server IP = " + hostname);

        } catch (UnknownHostException e) {
            e.printStackTrace();
        }


        try {
            binarySocket = new ServerSocket(5555);
        } catch (IOException e) {
            System.err.println("Could not listen on port: 5555.");
            System.exit(-1);
        }

        System.out.println("Waiting for connections on port 5555...");
        while (listening) {
            try {

                // wait for a connection
                BinaryServerThread binary = new BinaryServerThread(binarySocket.accept(), cache, 0);

                // start a new thread to handle the connection
                Thread binaryThread = new Thread(binary);
                binaryThread.start();

                System.out.println("Listening start. ");

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            binarySocket.close();
            System.out.println("Socket close. ");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Main function to start server.
     *
     * @param args input arguments
     */
    public static void main(String[] args) {
        new Server();
    }
}
