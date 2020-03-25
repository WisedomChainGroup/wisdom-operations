package com.wisdom.monitor.service.Impl;

import com.wisdom.monitor.model.AccountDB;
import com.wisdom.monitor.service.TransactionService;
import com.wisdom.monitor.utils.ApiResult.APIResult;
import com.wisdom.monitor.utils.KeystoreUtil;
import com.wisdom.monitor.utils.crypto.RipemdUtility;
import com.wisdom.monitor.utils.crypto.SHA3Utility;
import org.apache.commons.codec.binary.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;
@Service
public class TransactionServiceImpl implements TransactionService {

    @Autowired
    private AccountDB accountDB;

    @Override
    public APIResult getTxrecordFromAddress(String address) {
        try {
            if (KeystoreUtil.verifyAddress(address) == 0) {
                byte[] pubkeyhash = KeystoreUtil.addressToPubkeyHash(address);
                List<Map<String, Object>> list = new ArrayList<>();
                List<Map<String, Object>> tolist = accountDB.selectTranto(pubkeyhash);
                for (Map<String, Object> to : tolist) {
                    Map<String, Object> maps = to;
                    String from = maps.get("from").toString();
                    String fromaddress = KeystoreUtil.pubkeyToAddress(Hex.decodeHex(from.toCharArray()), (byte) 0x00);
                    maps.put("from", fromaddress);
                    String topubkeyhash = maps.get("to").toString();
                    String toaddress = KeystoreUtil.pubkeyHashToAddress(topubkeyhash);
                    maps.put("to", toaddress);
                    long time = Long.valueOf(maps.get("datetime").toString());
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Date dates = new Date(time * 1000);
                    String date = sdf.format(dates);
                    maps.put("datetime", date);
                    maps.put("type", "+");
                    list.add(maps);
                }
                List<Map<String, Object>> fromlist = accountDB.selectTranfrom(pubkeyhash);
                for (Map<String, Object> from : fromlist) {
                    Map<String, Object> maps = from;
                    String froms = maps.get("from").toString();
                    byte[] frompubhash = RipemdUtility.ripemd160(SHA3Utility.keccak256(Hex.decodeHex(froms.toCharArray())));
                    if (Arrays.equals(frompubhash, pubkeyhash)) {
                        String fromaddress = KeystoreUtil.pubkeyHashToAddress(Hex.encodeHexString(frompubhash));
                        maps.put("from", fromaddress);
                        String topubkeyhash = maps.get("to").toString();
                        String toaddress = KeystoreUtil.pubkeyHashToAddress(topubkeyhash);
                        maps.put("to", toaddress);
                        long time = Long.valueOf(maps.get("datetime").toString());
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        Date dates = new Date(time * 1000);
                        String date = sdf.format(dates);
                        maps.put("datetime", date);
                        maps.put("type", "-");
                        list.add(maps);
                    }
                }
                Collections.sort(list, new Comparator<Map<String, Object>>() {
                    public int compare(Map<String, Object> o1, Map<String, Object> o2) {
                        Integer name1 = Integer.valueOf(o1.get("height").toString());
                        Integer name2 = Integer.valueOf(o2.get("height").toString());
                        return name2.compareTo(name1);
                    }
                });
                return APIResult.newSuccess(list);
            } else {
                return APIResult.newFailResult(5000, "Address check error");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return APIResult.newFailResult(5000, "Exception error");
        }
    }
}
