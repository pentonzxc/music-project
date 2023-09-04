package com.innowise.auth.web.util;


import io.jsonwebtoken.lang.Collections;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.experimental.UtilityClass;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.List;


@UtilityClass
public class CookieUtil {

    private static final String CookieMultiValueSeparator = "&";

    public static Cookie cookieByName(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        return Arrays.stream(cookies)
                .filter(cookie -> cookie.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("WHERE IS COOKIE???"));
    }


    @SuppressWarnings("unchecked")
    public static List<String> getCookieMultiValue(Cookie cookie) {
        return (List<String>) CollectionUtils.arrayToList(cookie.getValue().split("&"));
    }
}
