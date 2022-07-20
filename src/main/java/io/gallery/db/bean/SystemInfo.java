package io.gallery.db.bean;

import io.gallery.db.util.DBT;

public class SystemInfo {
    private String osname;
    private String osversion;
    private String osarch;
    private String javahome;
    private String javaversion;
    private String userhome;
    private String username;
    private String userdir;
    private String pid;
    private int cpus;
    private long starttime;
    private String starttimeStr;

    public String getStarttimeStr() {
        return starttimeStr;
    }

    public long getStarttime() {
        return starttime;
    }

    public void setStarttime(long starttime) {
        this.starttime = starttime;
        this.starttimeStr = DBT.format(starttime);
    }

    public String getOsname() {
        return osname;
    }

    public void setOsname(String osname) {
        this.osname = osname;
    }

    public String getOsversion() {
        return osversion;
    }

    public void setOsversion(String osversion) {
        this.osversion = osversion;
    }

    public String getJavahome() {
        return javahome;
    }

    public void setJavahome(String javahome) {
        this.javahome = javahome;
    }

    public String getJavaversion() {
        return javaversion;
    }

    public void setJavaversion(String javaversion) {
        this.javaversion = javaversion;
    }

    public String getUserhome() {
        return userhome;
    }

    public void setUserhome(String userhome) {
        this.userhome = userhome;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserdir() {
        return userdir;
    }

    public void setUserdir(String userdir) {
        this.userdir = userdir;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public int getCpus() {
        return cpus;
    }

    public void setCpus(int cpus) {
        this.cpus = cpus;
    }

    public String getOsarch() {
        return osarch;
    }

    public void setOsarch(String osarch) {
        this.osarch = osarch;
    }
}
