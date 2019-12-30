package com.wisdom.monitor.model;

import org.springframework.stereotype.Component;
import java.util.List;


@Component
public class Block implements Comparable<Block>{
   public Integer blockSize;
   public String blockHash;
   public String nVersion;
   public String hashPrevBlock;
   public String hashMerkleRoot;
   public String hashMerkleState;
   public Long nHeight;
   public Long nTime;
   public String nBits;
   public String nNonce;
   public String blockNotice;
   public List<Transaction> body;

    public Integer getBlockSize() {
        return blockSize;
    }

    public void setBlockSize(Integer blockSize) {
        this.blockSize = blockSize;
    }

    public String getBlockHash() {
        return blockHash;
    }

    public void setBlockHash(String blockHash) {
        this.blockHash = blockHash;
    }

    public String getnVersion() {
        return nVersion;
    }

    public void setnVersion(String nVersion) {
        this.nVersion = nVersion;
    }

    public String getHashPrevBlock() {
        return hashPrevBlock;
    }

    public void setHashPrevBlock(String hashPrevBlock) {
        this.hashPrevBlock = hashPrevBlock;
    }

    public String getHashMerkleRoot() {
        return hashMerkleRoot;
    }

    public void setHashMerkleRoot(String hashMerkleRoot) {
        this.hashMerkleRoot = hashMerkleRoot;
    }

    public String getHashMerkleState() {
        return hashMerkleState;
    }

    public void setHashMerkleState(String hashMerkleState) {
        this.hashMerkleState = hashMerkleState;
    }

    public Long getnHeight() {
        return nHeight;
    }

    public void setnHeight(Long nHeight) {
        this.nHeight = nHeight;
    }

    public Long getnTime() {
        return nTime;
    }

    public void setnTime(Long nTime) {
        this.nTime = nTime;
    }

    public String getnBits() {
        return nBits;
    }

    public void setnBits(String nBits) {
        this.nBits = nBits;
    }

    public String getnNonce() {
        return nNonce;
    }

    public void setnNonce(String nNonce) {
        this.nNonce = nNonce;
    }

    public String getBlockNotice() {
        return blockNotice;
    }

    public void setBlockNotice(String blockNotice) {
        this.blockNotice = blockNotice;
    }

    public List<Transaction> getBody() {
        return body;
    }

    public void setBody(List<Transaction> body) {
        this.body = body;
    }

    @Override
    public int compareTo(Block o) {
        int i=+this.getnHeight().compareTo(o.getnHeight());
        return i;
    }
}
