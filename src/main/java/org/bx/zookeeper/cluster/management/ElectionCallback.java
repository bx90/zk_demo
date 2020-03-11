package org.bx.zookeeper.cluster.management;

public interface ElectionCallback {

    void onElectedToBeLeader();

    void onWorker();
}
