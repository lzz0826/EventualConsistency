package org.example.client.service;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.example.client.uitl.OkHttpUtil;
import org.example.common.BaseResp;
import org.example.common.StatusCode;
import org.example.entities.Stock;
import org.example.enums.ServiceUrlEnum;
import org.example.exception.NoStockException;
import org.example.exception.OkHttpGetException;
import org.example.exception.StockServerErrorException;
import org.springframework.stereotype.Service;

@Service
public class StockClientService {


  //--------- Stock 服務
  public BaseResp<Stock> getStockById(Long id) throws OkHttpGetException {
    String url = ServiceUrlEnum.Stock.getServiceUrl(ServiceUrlEnum.Stock.name)+"getStockById/"+id;;
    Map<String, String> header = new HashMap<>();
    String rep = OkHttpUtil.get(url, header);
    //使用TypeReference<>{} 隱式函數轉換
    BaseResp<Stock> baseResp = JSON.parseObject(rep, new TypeReference<BaseResp<Stock>>() {});
    return baseResp;
  }

  public BaseResp<Stock> getStockByProductName(String productName) throws OkHttpGetException {
    String url = ServiceUrlEnum.Stock.getServiceUrl(ServiceUrlEnum.Stock.name)+"getStockByProductName/"+productName;;
    Map<String, String> header = new HashMap<>();
    String rep = OkHttpUtil.get(url, header);
    //使用TypeReference<>{} 隱式函數轉換
    BaseResp<Stock> baseResp = JSON.parseObject(rep, new TypeReference<BaseResp<Stock>>() {});
    return baseResp;
  }

  public boolean deductedStockQuantity(String id, String  quantity) {
    String url = ServiceUrlEnum.Stock.getServiceUrl(ServiceUrlEnum.Stock.name)+"deductedStockQuantity";
    Map<String, String> params = new HashMap<>();
    params.put("id",id);
    params.put("quantity",quantity);

    try {
      String rep = OkHttpUtil.post(url,null, JSON.toJSONString(params));

      Integer statusCode = (Integer) JSON.parseObject(rep).get("statusCode");;

      String data =  (String) JSON.parseObject(rep).get("data");

      if(statusCode == 3002){
        throw new NoStockException();
      }

      if(statusCode == 0 && data.equals("true")){
        return true;
      }

    }catch (Exception e){
      return false;
    }

    return false;
  }

  public static Stock RepStock(BaseResp<Stock> baseResp){
    if(!baseResp.getStatusCode().equals(StatusCode.Success.code)){
      throw new StockServerErrorException();
    }
    if(baseResp.getData() != null){
      return baseResp.getData();
    }
    return null;
  }



}
