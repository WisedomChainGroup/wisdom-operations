package com.wisdom.monitor.service;

import com.wisdom.monitor.model.Nodes;

public interface NodeService {
    Object stop(String ipPort);
    Object restart(String ipPort);
    Object deleteData(long height);
    Nodes searchNode(String ipPort);
    boolean updateNode(Nodes nodes);


}
