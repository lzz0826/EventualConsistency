package org.example.client.service;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import io.seata.core.context.RootContext;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import lombok.extern.log4j.Log4j2;
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
@Log4j2
public class StockClientService {

  //--------- Stock 服務
  public BaseResp<Stock> getStockById(Long id) throws OkHttpGetException {
    String url = ServiceUrlEnum.Stock.getServiceUrl(ServiceUrlEnum.Stock.name)+"getStockById/"+id;;
    Map<String, String> header = new HashMap<>();
    CheckOutXid(header);
    String rep = OkHttpUtil.get(url, header);
    //使用TypeReference<>{} 隱式函數轉換
    BaseResp<Stock> baseResp = JSON.parseObject(rep, new TypeReference<BaseResp<Stock>>() {});
    return baseResp;
  }

  public BaseResp<Stock> getStockByProductName(String productName) throws OkHttpGetException {
    String url = ServiceUrlEnum.Stock.getServiceUrl(ServiceUrlEnum.Stock.name)+"getStockByProductName/"+productName;;
    Map<String, String> header = new HashMap<>();
    CheckOutXid(header);
    String rep = OkHttpUtil.get(url, header);
    //使用TypeReference<>{} 隱式函數轉換
    BaseResp<Stock> baseResp = JSON.parseObject(rep, new TypeReference<BaseResp<Stock>>() {});
    return baseResp;
  }

  public List<Stock> getStockByProductNames(List<String> productName) {
    String url = ServiceUrlEnum.Stock.getServiceUrl(ServiceUrlEnum.Stock.name)+"getStockByProductNames";
    String jsonProductNames = JSON.toJSONString(productName);
    Map<String, String> header = new HashMap<>();
    String rep = OkHttpUtil.post(url,header,jsonProductNames);
    //使用TypeReference<>{} 隱式函數轉換
    BaseResp<List<Stock>> baseResp = JSON.parseObject(rep, new TypeReference<BaseResp<List<Stock>>>() {});

    if (baseResp != null && baseResp.getStatusCode() == StatusCode.Success.code){
      return baseResp.getData();
    }
    return null;
  }


  /**
   * Seata 扣庫存 強一致
   */
  public boolean deductedStockQuantity(String id, String  quantity) throws NoStockException {
    String url = ServiceUrlEnum.Stock.getServiceUrl(ServiceUrlEnum.Stock.name)+"deductedStockQuantity";
    Map<String, String> params = new HashMap<>();
    Map<String, String> headers = new HashMap<>();
    CheckOutXid(headers);
    params.put("id",id);
    params.put("quantity",quantity);


    Integer statusCode;
    String data;

    try {
      String rep = OkHttpUtil.post(url,headers, JSON.toJSONString(params));
      statusCode = (Integer) JSON.parseObject(rep).get("statusCode");;
      data =  (String) JSON.parseObject(rep).get("data");
    }catch (Exception e){
      log.error("請求失敗 : "+url);
      return false;
    }

    if(statusCode == 3002){
      throw new NoStockException();
    }

    if(statusCode == 0 && data.equals("true")){
      return true;
    }
    return false;
  }


  /**
   * RabbitMq 扣庫存 最終一致性
   */
  public boolean deductedStockQuantityMq(String id,String orderId , String  quantity) throws NoStockException {
    String url = ServiceUrlEnum.Stock.getServiceUrl(ServiceUrlEnum.Stock.name)+"deductedStockQuantityMq";
    Map<String, String> params = new HashMap<>();
    Map<String, String> headers = new HashMap<>();
    params.put("stockId",id);
    params.put("orderId",orderId);
    params.put("quantity",quantity);

    Integer statusCode;
    String data;

    try {
      String rep = OkHttpUtil.post(url,headers, JSON.toJSONString(params));
      statusCode = (Integer) JSON.parseObject(rep).get("statusCode");;
      data =  (String) JSON.parseObject(rep).get("data");
    }catch (Exception e){
      log.error("請求失敗 : "+url);
      return false;
    }

    if(statusCode == 3002){
      throw new NoStockException();
    }

      return statusCode == 0 && data.equals("true");
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

  /**
   * (請求方) : 確認Seata分布式事務ID **因使用OKHTTP需要自行添加Header
   * (被請求方) : 需要使用全局攔截器來接收Xid
   * OkHttp 不自动集成 Seata 的事务传播机制。Seata 通常与 Spring Cloud 集成时，依赖于 Spring Cloud 的通信机制
   * （如 Feign、Ribbon 等）来自动传递 XID。使用 OkHttp 的话，需要手动将 XID 从事务发起方传到被调用方。
   */
  public static Map<String,String> CheckOutXid(Map<String,String> headers){
    String xid = RootContext.getXID();
    if(xid != null){
      headers.put("XID",xid);
    }
    return headers;
  }



}
