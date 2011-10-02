package ir.hello;

import org.ros.node.DefaultNodeFactory;
import org.ros.node.Node;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMain;
import org.ros.node.topic.Publisher;

public class Talker implements NodeMain {

    private Node node;

    @Override
    public void main(NodeConfiguration configuration) throws Exception {
        try {
            node = new DefaultNodeFactory().newNode("talker", configuration);
            Publisher<org.ros.message.std_msgs.String> publisher =
                    node.newPublisher("chatter", "std_msgs/String");
            int seq = 0;
            while (true) {
                org.ros.message.std_msgs.String str = new org.ros.message.std_msgs.String();
                str.data = "Hello world! " + seq++;
                publisher.publish(str);
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void shutdown() {
        node.shutdown();
        node = null;
    }
}
