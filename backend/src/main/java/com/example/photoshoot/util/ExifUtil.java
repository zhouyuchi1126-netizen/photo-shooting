package com.example.photoshoot.util;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/** EXIF 工具：读取图片拍摄时间 */
public class ExifUtil {

    private static final DateTimeFormatter[] FORMATS = {
        DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
        DateTimeFormatter.ofPattern("yyyy:MM:dd"),
    };

    /** 从图片输入流中提取拍摄时间，失败返回 null */
    public static LocalDateTime extractShootDate(InputStream input) {
        if (input == null) return null;
        try {
            Metadata meta = ImageMetadataReader.readMetadata(input);
            ExifSubIFDDirectory dir = meta.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
            if (dir == null) return null;
            String dateStr = dir.getString(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
            if (dateStr == null || dateStr.isBlank()) return null;
            dateStr = dateStr.trim();
            for (DateTimeFormatter fmt : FORMATS) {
                try { return LocalDateTime.parse(dateStr, fmt); } catch (DateTimeParseException ignored) {}
            }
        } catch (Exception ignored) {}
        return null;
    }
}
