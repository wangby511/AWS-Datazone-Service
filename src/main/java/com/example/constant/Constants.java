package com.example.constant;

public class Constants {
    public static final String GSI_BY_DOMAIN = "by-domain";
    public static final String IAM_ROLE_ARN_REGEX = "^arn:aws[^:]*:iam::\\d{12}:(role|role/service-role)/[\\w+=,.@-]*$";
    public static final String ID_CHAR_POOL = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789_-";
    
    public static final String DOMAIN_ID_RULE_REFERENCE = "^dzd[-_][a-zA-Z0-9_-]{1,36}$";
    public static final int DOMAIN_ID_PAYLOAD_LENGTH = 36;
    public static final String PROJECT_ID_REGEX = "^[a-zA-Z0-9_-]{1,36}$";
    public static final int PROJECT_ID_LENGTH = 36;
}
