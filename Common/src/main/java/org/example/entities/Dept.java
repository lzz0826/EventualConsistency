package org.example.entities;


import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Accessors(chain = true)//鏈式風格訪問
//微服務必須使用Serializable序列化 或是使用OKHTTP之類的會自動處理序列化
public class Dept implements Serializable {

  private Long deptne;
  private String dname;
  private String db_source; //來自哪個數據庫 因為微服務架構可以一個服務對應一個數據庫 同一個訊息被存到不同數據庫

}
