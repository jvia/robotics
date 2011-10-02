package ir.hello;

import org.ros.message.MessageListener;
import org.ros.node.DefaultNodeFactory;
import org.ros.node.Node;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMain;
import org.ros.node.topic.Subscriber;

public class Listener implements NodeMain, MessageListener<org.ros.message.std_msgs.String> {

    private Node node;

    @Override
    public void main(NodeConfiguration configuration) throws Exception {
        node = new DefaultNodeFactory().newNode("listener", configuration);
        Subscriber<org.ros.message.std_msgs.String> subscriber =
                node.newSubscriber("listener", "std_msgs/String", this);

    }

    @Override
    public void shutdown() {
        node.shutdown();
        node = null;
    }

    @Override
    public void onNewMessage(org.ros.message.std_msgs.String string) {
        System.out.println(string);
    }
}
