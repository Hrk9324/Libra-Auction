package io.github.guennhatking.libra_auction.services;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;
import io.github.guennhatking.libra_auction.viewmodels.response.ImageUploadResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class ImageUploadService {
    private final Cloudinary cloudinary;

    public ImageUploadService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    public ImageUploadResponse uploadImage(MultipartFile file, String folder) throws Exception {
        validateImage(file);

        Map<String, Object> uploadOptions = new LinkedHashMap<>(ObjectUtils.asMap(
                "resource_type", "image",
                "use_filename", true,
                "unique_filename", false,
                "overwrite", true
        ));

        if (folder != null && !folder.isBlank()) {
            uploadOptions.put("folder", normalizeFolder(folder));
        }

        Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), uploadOptions);
        String publicId = String.valueOf(uploadResult.get("public_id"));

        Map<?, ?> assetDetails = cloudinary.api().resource(publicId, ObjectUtils.asMap(
                "quality_analysis", true
        ));

        Transformation transformation = new Transformation()
                .crop("pad")
                .width(300)
                .height(400)
                .background("auto:predominant");

        String transformedUrl = cloudinary.url()
                .transformation(transformation)
                .generate(publicId);

        String transformedImageTag = cloudinary.url()
                .transformation(transformation)
                .imageTag(publicId);

        return new ImageUploadResponse(
                publicId,
                file.getOriginalFilename(),
                stringValue(uploadResult.get("format")),
                stringValue(uploadResult.get("resource_type")),
                stringValue(uploadResult.get("secure_url")),
                integerValue(uploadResult.get("width")),
                integerValue(uploadResult.get("height")),
                longValue(uploadResult.get("bytes")),
                transformedUrl,
                transformedImageTag,
                toStringObjectMap(assetDetails)
        );
    }

    private void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Image file is required");
        }

        String contentType = file.getContentType();
        if (contentType != null && contentType.startsWith("image/")) {
            return;
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename != null) {
            String normalizedFilename = originalFilename.trim().toLowerCase();
            if (normalizedFilename.endsWith(".png")
                    || normalizedFilename.endsWith(".jpg")
                    || normalizedFilename.endsWith(".jpeg")
                    || normalizedFilename.endsWith(".gif")
                    || normalizedFilename.endsWith(".webp")
                    || normalizedFilename.endsWith(".bmp")) {
                return;
            }
        }

        if ("application/octet-stream".equalsIgnoreCase(contentType)) {
            throw new IllegalArgumentException("Unsupported image file extension");
        }

        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Only image files are supported");
        }
    }

    private String normalizeFolder(String folder) {
        return folder.trim().replace('\\', '/');
    }

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private Integer integerValue(Object value) {
        return value instanceof Number number ? number.intValue() : null;
    }

    private Long longValue(Object value) {
        return value instanceof Number number ? number.longValue() : null;
    }

    private Map<String, Object> toStringObjectMap(Map<?, ?> source) {
        Map<String, Object> result = new LinkedHashMap<>();
        for (Map.Entry<?, ?> entry : source.entrySet()) {
            result.put(String.valueOf(entry.getKey()), entry.getValue());
        }
        return result;
    }
}