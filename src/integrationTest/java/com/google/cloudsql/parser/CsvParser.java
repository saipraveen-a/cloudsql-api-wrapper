package com.google.cloudsql.parser;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.bean.ColumnPositionMappingStrategy;
import au.com.bytecode.opencsv.bean.CsvToBean;

import com.google.common.collect.Lists;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

@SuppressWarnings("unchecked")
public class CsvParser<T> {

  public List<T> parse(File file, String[] columns, Class<T> type) throws IOException {
    List<T> beanList = Lists.newLinkedList();
    CSVReader reader = null;
    try {
      InputStreamReader inputStreamReader = new InputStreamReader(new FileInputStream(file), "UTF8");
      reader = new CSVReader(inputStreamReader);

      ColumnPositionMappingStrategy strategy = new ColumnPositionMappingStrategy();
      strategy.setType(type);
      strategy.setColumnMapping(columns);

      CsvToBean csv = new CsvToBean();
      beanList = csv.parse(strategy, reader);
    } finally {
      if (reader != null) {
        reader.close();
      }
    }
    return beanList;
  }
}
