package org.example.service;


import java.util.List;
import org.example.entities.Dept;

public interface DeptService {

  public boolean add(Dept dept);

  public int update(Dept dept);

  public Dept get(Long id);

  public List<Dept> list();

  public List<Dept> getCashByList();

}
