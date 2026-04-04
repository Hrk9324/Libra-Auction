package io.github.guennhatking.libra_auction.viewmodels.response;

import java.util.Map;

public record ImageUploadResponse(
        String publicId,
        String originalFilename,
        String format,
        String resourceType,
        String secureUrl,
        Integer width,
        Integer height,
        Long bytes,
        String transformedUrl,
        String transformedImageTag,
        Map<String, Object> details
) {
}