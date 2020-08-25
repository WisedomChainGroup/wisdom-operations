package com.wisdom.monitor.service;

import com.wisdom.monitor.model.Nodes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import java.util.Optional;

public interface NodeService{
    Object stop(String ipPort);
    Object restart(String ipPort);
    Object deleteData(long height);
    Nodes searchNode(String ipPort);
    boolean updateNode(Nodes nodes);


}
