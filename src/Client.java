import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * TCP Client for Key-Value store.
 * Each socket connection issues a single request.
 * <p>
 * Command format:
 * <code>java Client [server] [operation] [key] [value]</code>
 * <code>[server]</code> is the host name or IP of the server (possibly localhost for testing)
 * <code>[operation]</code> can be GET, SET, or STATS
 * <code>[key]</code> and <code>[value]</code> are strings used in GET and SET.
 * MULTIGET & MULTIGET Support:
 * <code>java client [server] [operation] [key] [value] [key 2] [value 2] ... [key n] [value n]</code>
 *
 * @author BorisMirage
 * Time: 2018/09/10 10:05
 * Created with IntelliJ IDEA
 */

public class Client {

    /**
     * Read input arguments and send message to server.
     * The client use message class (see in Message.java) to construct message body.
     * And then coded into binary array (see in BinaryCoder.java) and send it to server.
     *
     * @param arr    input arguments array
     * @param server server address
     * @param op     operation that client will request to server
     */
    private static void running(String[] arr, String server, String op) {

        try {

            /* Modify server and port num here, default port num is 5555 */
            Socket s = new Socket(server, 5555);
            DataOutputStream out = new DataOutputStream(s.getOutputStream());
            DataInputStream in = new DataInputStream(s.getInputStream());
            Message request = new Message(true, false, op);

            if (op.equals("get")) {

                /* GET */
                request.setKey(arr[0]);
                request.setVal(" ");
            } else if (op.equals("set")) {

                /* SET */
                request.setKey(arr[0]);
                request.setVal(arr[1]);
            } else if (op.equals("stats") || op.equals("exit")) {

                /* STATS & EXIT */
                request.setKey(" ");
                request.setVal(" ");
            }

            /* Encode message to binary and send to server */
            MessageCoder encode = new BinaryCoder();
            byte[] requestBytes = encode.toBinary(request);
            out.writeByte((byte) -1);
            out.writeByte((byte) requestBytes.length);
            out.write(requestBytes);
            out.flush();

            /* Obtain server's response */
            BinaryCoder receiveDecoder = new BinaryCoder();
            Message receiveMessage;
            int length = in.readInt();
            byte[] data = new byte[length];
            in.readFully(data, 0, length);
            receiveMessage = receiveDecoder.toMsg(data);

            /* Check if this message is correct response */
            if (receiveMessage.isResponse()) {
                System.out.println(receiveMessage.getVal());
            } else {
                throw new IOException("Error response! ");
            }

            /* Close sockets */
            out.close();
            in.close();
            s.close();

        } catch (UnknownHostException e) {

            /* if the IP address of the host could not be determined */
            System.err.println("Error: Unknown Host! ");

        } catch (IOException e) {

            /* if an I/O error occurs when creating the socket */
            e.printStackTrace();
        }
    }

    /**
     * Main client function.
     * Run this client with correct arguments.
     *
     * @param args arguments
     *             <code>java client [server] [operation] [key] [value] [key 2] [value 2] ... [key n] [value n]</code>
     */
    public static void main(String[] args) {

        /* Check input arguments */
        if (args.length < 2) {

            /* Avoid incorrect command line arguments length */
            System.out.println("Arguments num error! ");
            System.exit(-1);
        }
        String op = args[1].toLowerCase();

        if (!op.equals("exit") && !op.equals("stats") && !op.equals("get") && !op.equals("set") && !op.equals("multiget") && !op.equals("multiset")) {

            /* Avoid error */
            throw new IllegalArgumentException("Arguments error! ");
        }

        /* Add key-value pairs to array list */
        String[] arr = new String[2];       // key-value pair
        if (op.equals("get") || op.equals("multiget")) {
            for (int i = 2; i < args.length; i++) {
                arr = new String[2];
                arr[0] = args[i];
                running(arr, args[0].toLowerCase(), "get");
            }
        } else if (op.equals("set") || op.equals("multiset")) {
            for (int i = 2; i < args.length; i += 2) {
                arr = new String[2];
                arr[0] = args[i];

                /* Avoid if value is not filled in arguments */
                try {
                    arr[1] = args[i + 1];
                } catch (IndexOutOfBoundsException e) {
                    System.out.println("Do not find last key's correspond value! ");
                    System.exit(-1);
                }
                running(arr, args[0].toLowerCase(), "set");
            }
        } else if (op.equals("stats") || op.equals("exit")) {
            running(arr, args[0].toLowerCase(), args[1].toLowerCase());
        } else {

            /* Avoid error */
            throw new IllegalArgumentException("Arguments error! ");
        }
    }
}
