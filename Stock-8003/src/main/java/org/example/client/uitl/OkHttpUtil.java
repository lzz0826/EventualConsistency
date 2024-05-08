package org.example.client.uitl;

import com.google.gson.Gson;
import lombok.extern.log4j.Log4j2;
import okhttp3.*;
import org.example.exception.OKHttpException;
import org.example.exception.OrderServerErrorException;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Base64;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Log4j2
public class OkHttpUtil {
  /**
   * 最大連接時間
   */
  public final static int CONNECTION_TIMEOUT = 5;
  /**
   * JSON格式
   */
  public static final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");
  /**
   * OkHTTP線程池最大空閑線程數
   */
  public final static int MAX_IDLE_CONNECTIONS = 100;
  /**
   * OkHTTP線程池空閑線程存活時間
   */
  public final static long KEEP_ALIVE_DURATION = 30L;
  /**
   * GSON格式
   */
  public static final Gson GSON = new Gson();


  /**
   * Base64圖片前綴，用於指示數據為PNG圖片的Base64編碼。
   * 當將Base64編碼的圖片數據直接嵌入到HTML或CSS中時使用此前綴。
   */
  public static String BASE64_PREFIX = "data:image/png;base64,";

  /**
   * client
   * 配置重試
   */
  private final static OkHttpClient HTTP_CLIENT = new OkHttpClient.Builder()
      .connectTimeout(CONNECTION_TIMEOUT, TimeUnit.SECONDS)
      .connectionPool(new ConnectionPool(MAX_IDLE_CONNECTIONS, KEEP_ALIVE_DURATION, TimeUnit.MINUTES))
      .build();


  /**
   * get請求，無需轉換對象
   *
   * @param url     鏈接
   * @param headers 請求頭
   * @return 響應信息
   */
  public static String get(String url, Map<String, String> headers) throws OKHttpException {
    try {
      Request.Builder builder = new Request.Builder();
      buildHeader(builder, headers);
      Request request = builder.url(url).build();
      Response response = HTTP_CLIENT.newCall(request).execute();

      if (response.isSuccessful() && Objects.nonNull(response.body())) {
        String result = response.body().string();
        log.info("執行get請求, url: {} 成功，返回數據: {}", url, result);
        return result;
      }
    } catch (Exception e) {
      log.error("執行get請求，url: {} 失敗!", url, e);
      throw new OKHttpException();
    }
    return null;
  }


  /**
   * Form表單提交
   *
   * @param url    地址
   * @param params form參數
   * @return
   */
  public static String post(String url, Map<String, String> params) {
    try {

      FormBody.Builder builder = new FormBody.Builder();
      if (!CollectionUtils.isEmpty(params)) {
        params.forEach(builder::add);
      }
      FormBody body = builder.build();
      Request request = new Request.Builder().url(url).post(body).build();
      Response response = HTTP_CLIENT.newCall(request).execute();
      //調用成功
      if (response.isSuccessful() && response.body() != null) {
        return response.body().string();
      }
    } catch (Exception e) {
      log.error("", e);
    }
    return null;
  }


  /**
   * 簡單post請求
   *
   * @param url     請求url
   * @param headers 請求頭
   * @param json    請求參數
   * @return
   */
  public static String post(String url, Map<String, String> headers, String json) {
    try {
      RequestBody body = RequestBody.create(MEDIA_TYPE_JSON, json);
      Request.Builder builder = new Request.Builder();
      buildHeader(builder, headers);
      Request request = builder.url(url).post(body).build();
      Response response = HTTP_CLIENT.newCall(request).execute();
      if (response.isSuccessful() && Objects.nonNull(response.body())) {
        String result = response.body().string();
        log.info("執行post請求,url: {}, header: {} ,參數: {} 成功，返回結果: {}", url, headers, json, result);
        return result;
      }
    } catch (Exception e) {
      log.error("執行post請求，url: {},參數: {} 失敗!", url, json, e);
    }
    return null;
  }


  /**
   * 設置請求頭
   *
   * @param builder .
   * @param headers 請求頭
   */
  private static void buildHeader(Request.Builder builder, Map<String, String> headers) {
    if (Objects.nonNull(headers) && headers.size() > 0) {
      headers.forEach((k, v) -> {
        if (Objects.nonNull(k) && Objects.nonNull(v)) {
          builder.addHeader(k, v);
        }
      });
    }
  }


  /**
   * 支持嵌套泛型的post請求。
   * <pre>
   *   Type type = new TypeToken<Results<User>>() {}.getType();
   * <pre/>
   *
   * @param url     鏈接
   * @param headers 請求頭
   * @param json    請求json
   * @param type    嵌套泛型
   * @return 響應對象, 可進行強轉。
   */
  public static <T> T post(String url, Map<String, String> headers, String json, Type type) {
    String result = post(url, headers, json);
    if (Objects.nonNull(result) && Objects.nonNull(type)) {
      return GSON.fromJson(result, type);
    }
    return null;
  }
  /**
   * 讀取流，轉換為Base64
   * 返回base64的照片
   */
  public static String postImg(String url, Map<String, String> headers, String json) {
    RequestBody body = RequestBody.create(MEDIA_TYPE_JSON, json);
    Request.Builder builder = new Request.Builder();
    buildHeader(builder, headers);
    Request request = builder.url(url).post(body).build();
    try {
      Response response = HTTP_CLIENT.newCall(request).execute();
      String contentType = null;
      if (response.body() != null && response.body().contentType() != null) {
        contentType = response.body().contentType().toString();
      }
      if ("image/png".equals(contentType)) {
        //讀取流
        return BASE64_PREFIX + new String(Base64.getEncoder().encode(response.body().bytes()));
      }
    } catch (IOException e) {
      log.error("", e);
    }
    return null;
  }


  /**
   * 異步獲取照片地址
   */
  public static void downloadImg(String url, String json) {
    RequestBody body = RequestBody.create(MEDIA_TYPE_JSON, json);

    Request request = new Request.Builder().url(url).post(body).build();

    Call call = HTTP_CLIENT.newCall(request);

    call.enqueue(new Callback() {
      @Override
      public void onFailure(Call call, IOException e) {
        //打印異常
        log.error("", e);
      }
      @Override
      public void onResponse(Call call, Response response) throws IOException {
        String s = BASE64_PREFIX + new String(Base64.getEncoder().encode(response.body().bytes()));
        log.info(s);
      }
    });

  }
}
