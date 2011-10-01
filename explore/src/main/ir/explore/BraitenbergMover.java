package ir.explore;

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
 * This is a simple implementation of a wall-avoidance Breitenberg robot. You can see a simple diagram of its internals
 * below.
 * <p/>
 * +---+
 * |   |
 * +-+-+-------------+
 * | +-----+         |    (*)
 * | |     +---------+-----+
 * | +-[]            |
 * | |     +---------+-----+
 * | +-----+         |    (*)
 * +-+-+-------------+
 * |   |
 * +---+
 *
 * @author Jeremiah Via <jxv911@cs.bham.ac.uk>
 * @author Ishmail Alsahar <>
 * @author Jan Lavendar <>
 * @author Antony Kinnear <>
 */
public class BraitenbergMover implements NodeMain {

    private Node node;
    private Publisher<Twist> pub;

    private static final double FORWARD_SPEED = 0.1;
    private static final double TURNING_SPEED = 0.5;

    @Override
    public void main(NodeConfiguration nodeConfiguration) throws Exception {
        node = new DefaultNodeFactory().newNode("braitenberg", nodeConfiguration);
        pub = node.newPublisher("cmd_vel", "geometry_msgs/Twist");

        node.newSubscriber("base_scan", "sensor_msgs/LaserScan", new MessageListener<LaserScan>() {
            @Override
            public void onNewMessage(LaserScan laserScan) {
                move(laserScan);
            }
        });

        node.newSubscriber("sonar", "p2os_driver/SonarArray", new MessageListener<SonarArray>() {
            @Override
            public void onNewMessage(SonarArray sonarArray) {
                checkBumper(sonarArray);
            }
        });
    }

    private void checkBumper(SonarArray sonarArray) {
        // TODO: put a sanity check to ensure we don't hit obstacles
    }

    private void move(LaserScan laserScan) {
        // uncomment if you want to see detailed laser data
        // System.out.printf("> angle_min: %.2f\n  angle_max: %.2f\n  angle_increment: %.2f\n  range_min: %.2f\n  range_max: %.2f\n  scan_time: %.2f\n",
        //                   laserScan.angle_min, laserScan.angle_max, laserScan.angle_increment, laserScan.range_min, laserScan.range_max, laserScan.scan_time);
        Twist cmd = new Twist();
        cmd.linear.x = FORWARD_SPEED;

        double firstHalf = 0.0;
        double secondHalf = 0.0;
        for (int i = 0; i < laserScan.ranges.length; i++) {
            double range = (laserScan.ranges[i] == 0.0) ? laserScan.range_max : laserScan.ranges[i];
            if (i < laserScan.ranges.length / 2)
                firstHalf += range;
            else
                secondHalf += range;
        }

        double multiplier = (secondHalf - firstHalf) / (laserScan.ranges.length);
        System.out.printf("1st: %.2f   2nd: %.2f   =>    %.2f\n", firstHalf, secondHalf, multiplier);
        cmd.angular.z = TURNING_SPEED * multiplier;

        pub.publish(cmd);
    }

    @Override
    public void shutdown() {
        node.shutdown();
        node = null;
    }
}
