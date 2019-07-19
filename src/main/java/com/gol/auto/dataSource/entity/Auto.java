package com.gol.auto.dataSource.entity;

import lombok.Data;

import java.util.List;

@Data
public class Auto {
    private boolean pageHelper;
    private boolean jtaTransactionManager;
    private String dataSourceClassName;
    private String basePackages;
    private List<DataBase> databases;
}
