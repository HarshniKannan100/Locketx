package com.wschatapp.controller;

import com.wschatapp.service.TempImageService;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@CrossOrigin("*")
@RestController
@RequestMapping("api/images")
public class ImageController {
    private static final String UPLOAD_DIR=System.getProperty("user.dir")+"uploads/";
    @Autowired
    private TempImageService tempImageService;
    @PostMapping("/upload")
    public ResponseEntity<String> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("userId") Long userId) {

        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("File is empty");
            }

            // store in memory (NOT disk)
            String imageId = tempImageService.storeImage(file.getBytes(), userId);

            // return temp URL
            String url = "http://localhost:8080/api/images/temp/" + imageId;

            return ResponseEntity.ok(url);

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Upload failed");
        }
    }

    @GetMapping("/temp/{id}")
    public ResponseEntity<byte[]> getImage(@PathVariable String id) {

        byte[] image = tempImageService.getImage(id);

        if (image == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .header("Content-Type", "image/jpeg")
                .body(image);
    }

    @PostMapping("/view")
    public ResponseEntity<String> deleteImage(@RequestBody Map<String, String> body) {

        try {
            String imageUrl = body.get("url");

            String fileName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);

            File file = new File(System.getProperty("user.dir") + "/uploads/" + fileName);

            if (file.exists()) {
                file.delete();
            }

            return ResponseEntity.ok("Deleted");

        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error deleting");
        }
    }

    public TempImageService getTempImageService() {
        return tempImageService;
    }

    public void setTempImageService(TempImageService tempImageService) {
        this.tempImageService = tempImageService;
    }
}
