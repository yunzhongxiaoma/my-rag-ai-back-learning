package com.kinghy.rag.common;

public class ApplicationConstant {
    public final static String API_VERSION = "/api/v1";
    public final static String APPLICATION_NAME = "";

    public final static String DEFAULT_BASE_URL = "";
    public final static String DEFAULT_DESCRIBE = "";
    public final static String SYSTEM_PROMPT = """
        Use the information from the DOCUMENTS section to provide accurate answers but act as if you knew this information innately.
        If unsure, simply state that you don't know.
        Another thing you need to note is that your reply must be in Chinese!
        DOCUMENTS:
            {documents}
        """;
}
