package org.example.enums;


import org.apache.commons.lang3.StringUtils;

public enum ServiceUrlEnum {

  Stock("Stock","127.0.0.1","8003");

  public final String name;
  public final String ip;

  public final String port;

  ServiceUrlEnum(String name,String ip,String port) {
    this.name = name;
    this.ip = ip;
    this.port = port;
  }


  public String getName() {
    return name;
  }


  public String getIp() {
    return ip;
  }

  public String getPort() {
    return port;
  }



  public static ServiceUrlEnum parse(String name) {
    if(!StringUtils.isBlank(name)){
      for(ServiceUrlEnum info : values()){
        if(info.name.equals(name)){
          return info;
        }
      }
    }
    return null;
  }


  public String getServiceUrl(String serviceName){
    ServiceUrlEnum parse = parse(serviceName);
    return "http://"+parse.ip+":"+parse.port+"/";

  }

}
