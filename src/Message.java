/**
 * Definition of message body and methods for binary protocol.
 *
 * @author BorisMirage
 * Time: 2018/09/20 10:06
 * Created with IntelliJ IDEA
 */

public class Message {
    private boolean request;
    private boolean response;
    private String op;
    private String key;
    private String val;
    private final int keySize = 64;
    private final int valSzie = 1024;

    /**
     * @param request   if this message is request from server
     * @param response  if this message is response from server
     * @param operation current operation
     *                  if this message is response from server, then operation will be same operation from client
     * @throws IllegalArgumentException if argument is not defined
     */
    public Message(boolean request, boolean response, String operation) throws IllegalArgumentException {

        this.request = request;
        this.response = response;
        this.op = operation.toLowerCase();
        if (!operation.equals("stats") && !operation.equals("set") && !operation.equals("get") && !operation.equals("exit")) {
            throw new IllegalArgumentException("Incorrect operation! ");
        }
    }

    /**
     * If this message is a request.
     *
     * @return true if this message is a request from client
     */
    public boolean isRequest() {
        return request;
    }

    /**
     * If this message is a response.
     *
     * @return true if this message is a response from server
     */
    public boolean isResponse() {
        return response;
    }

    /**
     * Return key in message.
     *
     * @return key in message
     */
    public String getKey() {
        return key;
    }

    /**
     * Return value in message.
     *
     * @return value in message
     */
    public String getVal() {
        return val;
    }

    /**
     * Return operation in message.
     *
     * @return operation in message
     */
    public String getOp() {
        return op;
    }

    /**
     * Set boolean value to specify this message is request from client.
     * This method is used for later modification in message.
     *
     * @param val
     */
    public void setRequest(boolean val) {
        this.request = val;
    }

    /**
     * Set boolean value to specify this message is request from server.
     * This method is used for later modification in message.
     *
     * @param val
     */
    public void setResponse(boolean val) {
        this.response = val;
    }

    /**
     * Set new key.
     *
     * @param key new key
     */
    public void setKey(String key) {
        this.key = key;
    }

    /**
     * Set new value.
     *
     * @param val new value
     */
    public void setVal(String val) {
        this.val = val;
    }

    /**
     * Convert message to string.
     *
     * @return string that contains message opeartion, key, value
     */
    public String msgToString() {
        if (op.equals("stats")) {

            /* STATS */
            return String.format("Operation: STATS. Value: [%s]", val);
        }

        /* Other operations */
        return String.format("Operation: %s. Key: [%s] Value: [%s]", op.toUpperCase(), key, val);
    }
}
