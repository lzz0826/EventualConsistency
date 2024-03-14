package org.example.client.service;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.example.client.uitl.OkHttpUtil;
import org.example.common.BaseResp;
import org.example.entities.Dept;
import org.example.entities.Stock;
import org.example.enums.ServiceUrlEnum;
import org.example.exception.OkHttpGetException;
import org.springframework.stereotype.Service;

@Service
public class StockClientService {


  //--------- Stock 服務
  public BaseResp<Stock> stockFindStockById(Long id) throws OkHttpGetException {
    String url = ServiceUrlEnum.Stock.getServiceUrl(ServiceUrlEnum.Stock.name)+"findStockById/"+id;;
    Map<String, String> header = new HashMap<>();
    String rep = OkHttpUtil.get(url, header);
    //使用TypeReference<>{} 隱式函數轉換
    BaseResp<Stock> baseResp = JSON.parseObject(rep, new TypeReference<BaseResp<Stock>>() {});
    return baseResp;
  }



}
