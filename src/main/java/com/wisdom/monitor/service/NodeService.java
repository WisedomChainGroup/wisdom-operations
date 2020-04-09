package com.wisdom.monitor.service;

import java.util.List;

public interface NodeService {
    List<String> stop();
    List<String> restart();
}
