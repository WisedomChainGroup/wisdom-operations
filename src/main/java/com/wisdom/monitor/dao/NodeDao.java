package com.wisdom.monitor.dao;


import com.wisdom.monitor.model.Nodes;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

public interface NodeDao extends JpaRepository<Nodes, Long> {

    Optional<Nodes> findNodesById(Long id);


    @Override
    <S extends Nodes> S save(S entity);

    Optional<Nodes> findNodesByNodeIPAndNodePort(String ip, String port);

    @Transactional
    void deleteByNodeIPAndNodePort(String ip, String port);

}
