package org.example.client.service;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import lombok.extern.log4j.Log4j2;
import org.example.client.uitl.OkHttpUtil;
import org.example.common.BaseResp;
import org.example.common.StatusCode;
import org.example.entities.Order;
import org.example.entities.Stock;
import org.example.enums.ServiceUrlEnum;
import org.example.exception.OKHttpException;
import org.example.exception.OrderServerErrorException;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@Log4j2
public class StockClientService {

  //--------- Order 服務
  public BaseResp<Order> getOrderById(Long id) throws OKHttpException {
    String url = ServiceUrlEnum.Order.getServiceUrl(ServiceUrlEnum.Order.name)+"getOrderById/"+id;;
    System.out.println(url);
    Map<String, String> header = new HashMap<>();
    String rep = OkHttpUtil.get(url, header);
    //使用TypeReference<>{} 隱式函數轉換
    BaseResp<Order> baseResp = JSON.parseObject(rep, new TypeReference<BaseResp<Order>>() {});

    return baseResp;
  }

  public static Order RepOrder(BaseResp<Order> baseResp) throws OrderServerErrorException {

    if (baseResp == null){
      return null;
    }
    if(!baseResp.getStatusCode().equals(StatusCode.Success.code)){
      throw new OrderServerErrorException();
    }
    if(baseResp.getData() != null){
      return baseResp.getData();
    }
    return null;
  }

}
