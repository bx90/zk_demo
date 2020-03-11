package org.bx.zookeeper.cluster;

import org.apache.zookeeper.data.Stat;
import org.bx.zookeeper.cluster.management.LeaderElection;
import org.bx.zookeeper.cluster.management.ServiceRegistry;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;
import java.util.List;

public class ZookeeperManager implements Watcher {
    private static final String ZOOKEEPER_ADDRESS = "localhost:2181";
    private static final String TARGET_ZNODE = "/service_registry";
    private static final int SESSION_TIMEOUT = 3000;
    private static final int DEFAULT_PORT = 8080;

    private ZooKeeper zooKeeper;

    public ZookeeperManager() throws IOException {
       zooKeeper = this.connectToZookeeper();
    }

    //    public static void main(String[] args) throws IOException, InterruptedException, KeeperException {
    public void register(String[] args) throws IOException, InterruptedException, KeeperException {
        int currentServerPort = args.length == 1 ? Integer.parseInt(args[0]) : DEFAULT_PORT;
        ZookeeperManager application = new ZookeeperManager();
//        ZooKeeper zooKeeper = application.connectToZookeeper();

        ServiceRegistry serviceRegistry = new ServiceRegistry(zooKeeper);

        OnElectionAction onElectionAction = new OnElectionAction(serviceRegistry, currentServerPort);

        LeaderElection leaderElection = new LeaderElection(zooKeeper, onElectionAction);
        leaderElection.volunteerForLeadership();
        leaderElection.reelectLeader();

        application.run();
        application.close();
        System.out.println("Disconnected from Zookeeper, exiting application");
    }

    public String getUrl(int id) throws KeeperException, InterruptedException {

        int mod = id % 2;
        List<String> children = zooKeeper.getChildren(TARGET_ZNODE, this);
        String nodeId = children.get(mod);
        Stat stats = zooKeeper.exists( TARGET_ZNODE + "/" + nodeId, false);
        byte[] datas = zooKeeper.getData(TARGET_ZNODE + "/" + nodeId, false, stats);

        String url = new String(datas);

        System.out.println( url );
        return url;


    }

    public ZooKeeper connectToZookeeper() throws IOException {
        this.zooKeeper = new ZooKeeper(ZOOKEEPER_ADDRESS, SESSION_TIMEOUT, this);
        return zooKeeper;
    }

    public void run() throws InterruptedException {
        synchronized (zooKeeper) {
            zooKeeper.wait();
        }
    }

    public void close() throws InterruptedException {
        zooKeeper.close();
    }

    @Override
    public void process(WatchedEvent event) {
        switch (event.getType()) {
            case None:
                if (event.getState() == Event.KeeperState.SyncConnected) {
                    System.out.println("Successfully connected to Zookeeper");
                } else {
                    synchronized (zooKeeper) {
                        System.out.println("Disconnected from Zookeeper event");
                        zooKeeper.notifyAll();
                    }
                }
        }
    }
}
