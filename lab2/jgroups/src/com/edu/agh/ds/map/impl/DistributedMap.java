package com.edu.agh.ds.map.impl;

import com.edu.agh.ds.map.SimpleStringMap;

public class DistributedMap implements SimpleStringMap {

    @Override
    public boolean containsKey(String key) {
        return false;
    }

    @Override
    public Integer get(String key) {
        return null;
    }

    @Override
    public void put(String key, Integer value) {}

    @Override
    public Integer remove(String key) {
        return null;
    }
}