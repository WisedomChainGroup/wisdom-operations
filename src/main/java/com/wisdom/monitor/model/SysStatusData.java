package com.wisdom.monitor.model;


import org.hyperic.sigar.*;

public class SysStatusData {
    // cpu信息
    private CpuInfo info;
    private CpuPerc perc;
    private Cpu timer;
    private CpuPerc[] cpuPercList ;

    // 内存信息
    private Mem mem ;

    public SysStatusData() throws SigarException {
        Sigar sigar = new Sigar();
        this.info = sigar.getCpuInfoList()[0];
        this.perc = sigar.getCpuPerc();
        this.cpuPercList = sigar.getCpuPercList();
        this.timer = sigar.getCpu();
        this.mem = sigar.getMem() ;
    }

    public CpuInfo getInfo() {
        return info;
    }

    public void setInfo(CpuInfo info) {
        this.info = info;
    }

    public CpuPerc getPerc() {
        return perc;
    }

    public void setPerc(CpuPerc perc) {
        this.perc = perc;
    }

    public Cpu getTimer() {
        return timer;
    }

    public void setTimer(Cpu timer) {
        this.timer = timer;
    }

    public CpuPerc[] getCpuPercList() {
        return cpuPercList;
    }

    public void setCpuPercList(CpuPerc[] cpuPercList) {
        this.cpuPercList = cpuPercList;
    }

    public Mem getMem() {
        return mem;
    }

    public void setMem(Mem mem) {
        this.mem = mem;
    }
}
