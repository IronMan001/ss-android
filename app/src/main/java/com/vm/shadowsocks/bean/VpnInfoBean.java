package com.vm.shadowsocks.bean;

/**
 * Created by Administrator on 2017/10/17 0017.
 */

public class VpnInfoBean {
    /**
     * 健康度
     */
    private int healthe = 0;

    /**
     * IP地址
     */
    private String ipadd = "";

    /**
     * 端口
     */
    private String port = "";

    /**
     * 密码
     */
    private String password = "";

    /**
     * 加密方式
     */
    private String salt_pwd_type = "";

    /**
     * 当前时间
     */
    private String currt_time = "";

    /**
     * 国家地区
     */
    private String Location = "";

    public VpnInfoBean(int healthe, String ipadd, String port, String password, String salt_pwd_type, String currt_time, String location) {
        this.healthe = healthe;
        this.ipadd = ipadd;
        this.port = port;
        this.password = password;
        this.salt_pwd_type = salt_pwd_type;
        this.currt_time = currt_time;
        Location = location;
    }

    public int getHealthe() {
        return healthe;
    }

    public void setHealthe(int healthe) {
        this.healthe = healthe;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getIpadd() {
        return ipadd;
    }

    public void setIpadd(String ipadd) {
        this.ipadd = ipadd;
    }

    public String getLocation() {
        return Location;
    }

    public void setLocation(String location) {
        Location = location;
    }

    public String getCurrt_time() {

        return currt_time;
    }

    public void setCurrt_time(String currt_time) {
        this.currt_time = currt_time;
    }

    public String getSalt_pwd_type() {

        return salt_pwd_type;
    }

    public void setSalt_pwd_type(String salt_pwd_type) {
        this.salt_pwd_type = salt_pwd_type;
    }

    public String getPassword() {

        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
