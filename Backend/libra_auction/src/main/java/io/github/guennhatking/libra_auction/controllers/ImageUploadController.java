package io.github.guennhatking.libra_auction.controllers;

import io.github.guennhatking.libra_auction.services.ImageUploadService;
import io.github.guennhatking.libra_auction.viewmodels.common.ApiResponse;
import io.github.guennhatking.libra_auction.viewmodels.response.ImageUploadResponse;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/images")
public class ImageUploadController {
    private final ImageUploadService imageUploadService;

    public ImageUploadController(ImageUploadService imageUploadService) {
        this.imageUploadService = imageUploadService;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ImageUploadResponse>> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "folder", required = false) String folder
    ) throws Exception {
        ImageUploadResponse response = imageUploadService.uploadImage(file, folder);
        return ResponseEntity.ok(new ApiResponse<>("Image uploaded successfully", response));
    }
}