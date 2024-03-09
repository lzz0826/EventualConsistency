package org.example.dao;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.example.entities.Dept;

@Mapper
public interface DeptDao {

  public boolean addDept(Dept dept);

  public Dept findById(Long id);

  public List<Dept> findAll();

  public int updateDept(Dept dept);




}
