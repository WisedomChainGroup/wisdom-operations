package com.wisdom.monitor.service;

import java.util.List;

public interface NodeService {
    Object stop(String ipPort);
    Object restart(String ipPort);
    Object deleteData(long height);


}
