package com.example.utils;

import com.example.constant.Constants;
import java.util.Random;

public class IdUtils {
    private static final Random RANDOM = new Random();
    private static final String CHAR_POOL = Constants.ID_CHAR_POOL;

    /**
     * Generates a Domain ID conforming to the pattern dzd[-_][a-zA-Z0-9_-]{1,36}.
     * @return The generated domain identifier string.
     */
    public static String generateDomainId() {
        StringBuilder sb = new StringBuilder("dzd");
        
        // 1. Append separator: '-' or '_'
        sb.append(RANDOM.nextBoolean() ? '-' : '_'); 
        
        // 2. Append random payload (36 characters)
        for (int i = 0; i < Constants.DOMAIN_ID_PAYLOAD_LENGTH; i++) {
            int index = RANDOM.nextInt(CHAR_POOL.length());
            sb.append(CHAR_POOL.charAt(index));
        }
        
        return sb.toString();
    }

    /**
     * Generates a Project ID conforming to the pattern [a-zA-Z0-9_-]{1,36}.
     * @return The generated project identifier string.
     */
    public static String generateProjectId() {
        StringBuilder sb = new StringBuilder();
        
        // Generate 36 random characters
        for (int i = 0; i < Constants.PROJECT_ID_LENGTH; i++) {
            int index = RANDOM.nextInt(CHAR_POOL.length());
            sb.append(CHAR_POOL.charAt(index));
        }
        
        return sb.toString();
    }
}
