import com.korwe.thecore.api.*;
import com.korwe.thecore.messages.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.Charset;
import java.util.UUID;

/**
 * @author <a href="mailto:nithia.govender@korwe.com>Nithia Govender</a>
 */
public class TestClient implements CoreMessageHandler {

    private static final String SESSION_ID = "test-session-001";
    private Logger log = LoggerFactory.getLogger(this.getClass());
    private CoreSender sender;
    private CoreSubscriber subscriber;
    private CoreSubscriber dataSubscriber;
    private CoreMessageSerializer serializer = new CoreMessageXmlSerializer();

    private static final String MSG_FILE = "/msg.1.xml";

    private void connect() {
        CoreConfig.initialize(this.getClass().getResourceAsStream("/coreconfig.xml"));
        sender = new CoreSender(MessageQueue.ClientToCore);
        subscriber = new CoreSubscriber(MessageQueue.CoreToClient, SESSION_ID);
        subscriber.connect(this);
        dataSubscriber = new CoreSubscriber(MessageQueue.Data, SESSION_ID);
        dataSubscriber.connect(this);
    }

    private void close() {
        subscriber.close();
        dataSubscriber.close();
        sender.close();
    }

    private CoreMessage readMessage() throws IOException {
        String lineSep = System.getProperty("line.separator");
        BufferedReader msgFile = null;
        StringBuilder builder = new StringBuilder();
        try {
            msgFile = new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream(MSG_FILE),
                                                               Charset.forName("UTF-8")));
            String line = msgFile.readLine();

            while (line != null) {
                builder.append(line).append(lineSep);
                line = msgFile.readLine();
            }
        }
        finally {
            if (msgFile != null) {
                msgFile.close();
            }
        }

        String message = builder.toString();
        String guid = UUID.randomUUID().toString();
        return builder.length() > 0 ? serializer.deserialize(String.format(message, guid)) : null;
    }

    private void sendMessage(CoreMessage message) {
        log.debug("Sending message: {}", message);
        sender.sendMessage(message);
    }

    @Override
    public void handleMessage(CoreMessage message) {
        log.debug("Received message: {}", message);
    }

    public static void main(String[] args) throws InterruptedException, IOException {
        TestClient client = new TestClient();
        client.connect();
//        client.sendMessage(new InitiateSessionRequest(SESSION_ID));
//        Thread.sleep(100L);
        client.sendMessage(client.readMessage());
        Thread.sleep(2000L);
//        client.sendMessage(new KillSessionRequest(SESSION_ID));
//        Thread.sleep(100L);
        client.close();
    }
}
