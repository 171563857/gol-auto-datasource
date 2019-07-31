package com.gol.auto.dataSource.entity;

import lombok.Data;

@Data
public class DataBase {
    private String schema;
    private String mapperPackages;
    private ReadDataBase readDataBase;
}
