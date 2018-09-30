import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Client for testing throughput and latency.
 * Command line format:
 * <code>java client [server] [operation]</code>
 *
 * @author BorisMirage
 * Time: 2018/09/10 10:05
 * Created with IntelliJ IDEA
 */

public class TestClient {

    /**
     * Read input arguments and send message to server.
     *
     * @param arr    arguments array
     * @param server server address
     * @param op     operation
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
            int l = in.readInt();
            byte[] data = new byte[l];
            in.readFully(data, 0, l);
            receiveMessage = receiveDecoder.toMsg(data);

//            /* Check if this message is correct response */
//            if (receiveMessage.isResponse()) {
//                System.out.println(receiveMessage.getVal());
//            } else {
//                throw new IOException("Error response! ");
//            }

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
     *             <code>java client [server] [operation]</code>
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

        /* For testing */
        String server = "localhost";
        String key;
        String val;
        int c = 30000;

        /* Test STATS */
        if (op.equals("stats")) {
            key = "key" + 1;
            val = "val" + 1;
            String[] arr = new String[2];
            arr[0] = key;
            arr[1] = val;
            running(arr, args[0].toLowerCase(), "set");


            System.out.println("Set 10 pairs to server as a test sample. ");
            System.out.println("Timer start. ");

            /* Timer */
            double startTime = System.nanoTime();

            /* Loop */
            for (int i = 0; i < c; i++) {
                System.out.println(String.format("%d times approach. ", i + 1));
                running(arr, server, "stats");
            }

            /* Timer end */
            double duration = (System.nanoTime() - startTime) / 1000000000;
            double latency = (duration / c) * 100;
            double throughput = c / duration;
            System.out.println(String.format("Entire time: %.3f s", duration));
            System.out.println(String.format("Latency: %.3f ms each request", latency));
            System.out.println(String.format("Throughput: %.3f requests per second. ", throughput));

        }

        if (op.equals("set")) {
            key = "key" + 11;
            val = "val" + 22;
            String[] arr = new String[2];
            arr[0] = key;
            arr[1] = val;

            System.out.println("Timer start. ");
            /* Timer */
            double startTime = System.nanoTime();

            /* Loop */
            for (int i = 0; i < c; i++) {
                System.out.println(String.format("%d times approach. ", i + 1));
                running(arr, server, "set");
            }

            /* Timer end */
            double duration = (System.nanoTime() - startTime) / 1000000000;
            double latency = (duration / c) * 100;
            double throughput = c / duration;
            System.out.println(String.format("Entire time: %.3f s", duration));
            System.out.println(String.format("Latency: %.3f ms each request", latency));
            System.out.println(String.format("Throughput: %.3f requests per second. ", throughput));
        }

        if (op.equals("get")) {
            key = "key" + 111;
            val = "val" + 222;
            String[] arr = new String[2];
            arr[0] = key;
            arr[1] = val;
            running(arr, args[0].toLowerCase(), "set");
            arr = new String[2];
            arr[0] = key;

            System.out.println("Timer start. ");
            /* Timer */
            double startTime = System.nanoTime();

            /* Loop */
            for (int i = 0; i < c; i++) {
                System.out.println(String.format("%d times approach. ", i + 1));
                running(arr, server, "get");
            }

            /* Timer end */
            double duration = (System.nanoTime() - startTime) / 1000000000;
            double latency = (duration / c) * 100;
            double throughput = c / duration;
            System.out.println(String.format("Entire time: %.3f s", duration));
            System.out.println(String.format("Latency: %.3f ms each request", latency));
            System.out.println(String.format("Throughput: %.3f requests per second. ", throughput));
        }
    }
}
