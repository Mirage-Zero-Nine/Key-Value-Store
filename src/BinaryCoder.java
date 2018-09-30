import java.io.*;

/**
 * Binary coder that can encode message class to binary array, and can also convert binary array to message class.
 * This coder at least contains 4 bytes.
 * First byte contains the magic number that for server and client to identify incoming byte stream.
 * The later two bits of first byte contains flag that mark whether this message is send from server or client.
 * Second byte store the operation that client send to server.
 * The specific value that each operation is defined below.
 * The following bytes contains key and value in this message.
 * Note that if message is sent from server, then its "key" will only contain a space, since key is useless for client.
 * For some special operation such as EXIT or STATS, the message will set key and value both for space.
 *
 * @author BorisMirage
 * Time: 2018/09/20 13:47
 * Created with IntelliJ IDEA
 */

public class BinaryCoder implements MessageCoder {

    private final int MIN_LENGTH = 2;       // check message length
    private final int MAGIC = 0x5400;
    private final int MAGIC_MASK = 0xfc00;     // used in bytes-to-string converter to check magic number
    private final int REQUEST = 0x0100;        // if it is request from client
    private final int RESPONSE = 0x0200;        // if it is response from server
    private final int STATS = 0x0008;
    private final int SET = 0x0004;
    private final int GET = 0x0002;
    private final int EXIT = 0x0001;

    /**
     * Encode message to binary array.
     *
     * @param Msg message that will be converted
     * @return byte array that will be transferred to server
     * @throws IOException if the operation field for message is not supported
     */
    public byte[] toBinary(Message Msg) throws IOException {

        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(byteStream);
        byte[] data;

        short finalMagic = MAGIC;
        if (Msg.isResponse()) {
            finalMagic |= RESPONSE;
        }
        if (Msg.isRequest()) {
            finalMagic |= REQUEST;
        }

        /* Set operation to binary that is defined before */
        if (Msg.getOp().equals("stats")) {
            finalMagic |= STATS;
        } else if (Msg.getOp().equals("set")) {
            finalMagic |= SET;
        } else if (Msg.getOp().equals("get")) {
            finalMagic |= GET;
        } else if (Msg.getOp().equals("exit")) {
            finalMagic |= EXIT;
        } else {
            throw new IOException("Error occurred when try to convert operation. ");
        }

        /* Write data */
        out.writeShort(finalMagic);
        out.writeUTF(Msg.getKey());
        out.writeUTF(Msg.getVal());
        out.flush();

        /* Convert to bytes */
        data = byteStream.toByteArray();

        return data;
    }

    /**
     * Convert incoming byte array to Message class.
     *
     * @param input input byte array
     * @return Message
     * @throws IOException if the message length is less that 2 bytes, or magic number is not correct
     */
    public Message toMsg(byte[] input) throws IOException {

        if (input.length < MIN_LENGTH) {
            throw new IOException("Input byte array length is too short! ");
        }
        ByteArrayInputStream byteStream = new ByteArrayInputStream(input);
        DataInputStream in = new DataInputStream(byteStream);
        int readMagic = in.readShort();

        if ((readMagic & MAGIC_MASK) != MAGIC) {
            throw new IOException("Wrong magic number! ");
        }

        /* Obtain operation in binary array */
        boolean request = (readMagic & REQUEST) != 0;
        boolean response = (readMagic & RESPONSE) != 0;
        boolean stats = (readMagic & STATS) != 0;
        boolean get = (readMagic & GET) != 0;
        boolean set = (readMagic & SET) != 0;
        boolean exit = (readMagic & EXIT) != 0;

        String op = null;
        if (stats) {
            op = "stats";
        }
        if (get) {
            op = "get";
        }
        if (set) {
            op = "set";
        }
        if (exit) {
            op = "exit";
        }
        String key = in.readUTF();
        String val = in.readUTF();
        Message convertMessage;
        if (op != null) {
            convertMessage = new Message(request, response, op);
        } else {
            throw new IOException("No operation found! ");
        }

        if (request) {
            convertMessage.setKey(key);
            convertMessage.setVal(val);
        } else if (response) {
            convertMessage.setVal(val);
        } else {
            throw new IOException("Message is not either request or response. ");
        }

        return convertMessage;
    }
}