package com.wisdom.monitor.service;

import com.wisdom.monitor.utils.ApiResult.APIResult;
import org.springframework.stereotype.Service;


public interface TransactionService {
    APIResult getTxrecordFromAddress(String address);
}
