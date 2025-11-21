package com.example.utils;

import com.example.constant.Constants;
import java.util.Random;

public class IdUtils {
    private static final Random RANDOM = new Random();
    private static final String CHAR_POOL = Constants.ID_CHAR_POOL;

    public static String generateDomainId() {
        StringBuilder sb = new StringBuilder("dzd");
        sb.append(RANDOM.nextBoolean() ? '-' : '_'); 
        for (int i = 0; i < Constants.DOMAIN_ID_PAYLOAD_LENGTH; i++) {
            sb.append(CHAR_POOL.charAt(RANDOM.nextInt(CHAR_POOL.length())));
        }
        return sb.toString();
    }

    public static String generateProjectId() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Constants.PROJECT_ID_LENGTH; i++) {
            sb.append(CHAR_POOL.charAt(RANDOM.nextInt(CHAR_POOL.length())));
        }
        return sb.toString();
    }
}
