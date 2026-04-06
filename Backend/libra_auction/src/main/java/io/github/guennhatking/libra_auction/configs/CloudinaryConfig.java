package io.github.guennhatking.libra_auction.configs;

import com.cloudinary.Cloudinary;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CloudinaryConfig {
    private final String cloudinaryUrl;

    public CloudinaryConfig(@Value("${CLOUDINARY_URL:}") String cloudinaryUrl) {
        this.cloudinaryUrl = cloudinaryUrl;
    }

    @Bean
    public Cloudinary cloudinary() {
        if (cloudinaryUrl == null || cloudinaryUrl.isBlank()) {
            throw new IllegalStateException("Missing CLOUDINARY_URL configuration");
        }

        Cloudinary cloudinary = new Cloudinary(cloudinaryUrl);
        cloudinary.config.secure = true;
        return cloudinary;
    }
}