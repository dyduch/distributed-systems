package com.edu.agh.ds.map.impl;

import com.edu.agh.ds.map.SimpleStringMap;

import java.util.HashMap;
import java.util.Map;

public class DistributedMap implements SimpleStringMap {

    private final Map<String, Integer> hashMap = new HashMap<>();

    @Override
    public boolean containsKey(String key) {
        return hashMap.containsKey(key);
    }

    @Override
    public Integer get(String key) {
        return hashMap.get(key);
    }

    @Override
    public void put(String key, Integer value) {
        hashMap.put(key, value);
    }

    @Override
    public Integer remove(String key) {
        return hashMap.remove(key);
    }

    public Map<String, Integer> getHashMap() {
        return hashMap;
    }

    public void setState(Map<String, Integer> hashMap) {
        this.hashMap.clear();
        this.hashMap.putAll(hashMap);
    }

    @Override
    public String toString() {
        return "DistributedMap{" +
                "hashMap=" + hashMap.toString() +
                '}';
    }
}
