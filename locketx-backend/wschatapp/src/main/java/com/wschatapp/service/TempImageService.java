package com.wschatapp.service;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TempImageService {


    private Map<String, byte[]> tempImages = new ConcurrentHashMap<>();


    private Map<Long, List<String>> userImages = new ConcurrentHashMap<>();


    public String storeImage(byte[] data, Long userId) {
        String imageId = UUID.randomUUID().toString();

        tempImages.put(imageId, data);
        userImages.computeIfAbsent(userId, k -> new ArrayList<>()).add(imageId);

        return imageId;
    }

    public byte[] getImage(String id) {
        return tempImages.get(id);
    }

    public void deleteUserImages(Long userId) {

        List<String> images = userImages.get(userId);

        if (images != null) {
            for (String imgId : images) {
                tempImages.remove(imgId);
            }
        }

        userImages.remove(userId);
    }
}