package test;

import javax.annotation.Resource;
import org.example.dao.DeptDao;
import org.example.entities.Dept;
import org.junit.Test;

public class MapperTest extends BastTest{

  @Resource
  private DeptDao dao;

  @Test
  public void testUpdateDept(){


    Dept dept = new Dept();
    dept.setDeptne(1L);
    dept.setDname("人事部");
//    dept.setDb_source();

    int b = dao.updateDept(dept);
    System.out.println(b);

  }

}
