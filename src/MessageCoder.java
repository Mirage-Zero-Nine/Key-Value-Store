import java.io.IOException;

/**
 * Interface for binary message encoding/decoding.
 *
 * @author BorisMirage
 * Time: 2018/09/20 10:06
 * Created with IntelliJ IDEA
 */

public interface MessageCoder {
    byte[] toBinary(Message newMessage) throws IOException;

    Message toMsg(byte[] input) throws IOException;
}
