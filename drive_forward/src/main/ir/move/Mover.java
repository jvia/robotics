package ir.move;

import org.ros.message.MessageListener;
import org.ros.message.geometry_msgs.Twist;
import org.ros.message.p2os_driver.SonarArray;
import org.ros.message.sensor_msgs.LaserScan;
import org.ros.node.DefaultNodeFactory;
import org.ros.node.Node;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMain;
import org.ros.node.topic.Publisher;

/**
 * A very simple control for a robot which causes it to drive forward until it is within some set distance of an
 * obstacle.
 *
 * @author Ishmail Alsahar <>
 * @author Jan Lavendar <>
 * @author Antony Kinnear <>
 * @author Jeremiah Via <jxv911@cs.bham.ac.uk>
 */
public class Mover implements NodeMain {

    private Node node;
    private Publisher<Twist> pub;

    // distance to keep from obstacle
    private static float MIN_DIST = 1.5f;

    /**
     * Create the node and set up publishers and subscribers.
     *
     * @param nc the node configuration
     * @throws Exception something went wrong
     */
    @Override
    public void main(NodeConfiguration nc) throws Exception {
        // create node and publisher
        node = new DefaultNodeFactory().newNode("robot_mover", nc);
        pub = node.newPublisher("cmd_vel", "geometry_msgs/Twist");

        // create callback for laser messages
        node.newSubscriber("base_scan", "sensor_msgs/LaserScan", new MessageListener<LaserScan>() {
            @Override
            public void onNewMessage(LaserScan laserScan) {
                move(laserScan);
            }
        });

        // create callback for sonar messages
        node.newSubscriber("sonar", "p2os_driver/SonarArray", new MessageListener<SonarArray>() {
            @Override
            public void onNewMessage(SonarArray sonarArray) {
                checkBumper(sonarArray);
            }
        });
    }

    /**
     * Move the robot if there is no obstacle within a set distance in front of it.
     *
     * @param message the laser data
     */
    public void move(LaserScan message) {
        if (message.ranges[message.ranges.length / 2] > MIN_DIST) {
            Twist cmd = new Twist();
            cmd.linear.x = 0.5; // Forward
            System.out.printf("go: %.2f meters\n", message.ranges[message.ranges.length / 2]);
            pub.publish(cmd);
        } else {
            Twist cmd = new Twist();
            cmd.angular.z = 0.5; // Forward
            System.out.printf("stop: %.2f meters\n", message.ranges[message.ranges.length / 2]);
            pub.publish(cmd);
        }
    }

    /**
     * Stop the robot if the sonar data says that there is an obstacle within a set distance of the robot.
     *
     * @param message the sonar data
     */
    public void checkBumper(SonarArray message) {
        if (message.ranges[message.ranges.length / 2] < MIN_DIST) {
            Twist cmd = new Twist();
            cmd.linear.x = 0; // Stop
            System.out.println("stop");
            pub.publish(cmd);
        }
    }

    /**
     * Shut down the node.
     */
    @Override
    public void shutdown() {
        node.shutdown();
    }
}