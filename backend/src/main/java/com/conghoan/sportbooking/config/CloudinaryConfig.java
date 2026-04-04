package com.conghoan.sportbooking.config;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CloudinaryConfig {

    @Bean
    public Cloudinary cloudinary() {
        return new Cloudinary(ObjectUtils.asMap(
                "cloud_name", "daytrfyrg",
                "api_key", "784438178628159",
                "api_secret", "DHKWrW5-kS_ItxG1TibCZNEnGgM"
        ));
    }
}
