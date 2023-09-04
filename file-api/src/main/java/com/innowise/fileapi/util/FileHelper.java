package com.innowise.fileapi.util;


import lombok.experimental.UtilityClass;

import java.io.File;

@UtilityClass
public class FileHelper {

    public static String nameWithExtension(String name, String extension) {
        return name.concat(".").concat(extension);
    }
}