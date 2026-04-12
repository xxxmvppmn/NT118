package com.example.waviapp.utils;

import android.graphics.Bitmap;
import android.util.Base64;
import java.io.ByteArrayOutputStream;

public class ImageUtils {

    /**
     * Nén ảnh và chuyển sang Base64 để lưu vào Firestore an toàn
     */
    public static String compressAndConvertToBase64(Bitmap bitmap) {
        if (bitmap == null) return "";

        // 1. Resize: Giảm kích thước xuống tối đa 200px (đủ nét cho Avatar)
        int maxWidth = 500;
        int maxHeight = 500;
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        float ratio = (float) width / (float) height;
        if (width > height) {
            width = maxWidth;
            height = (int) (width / ratio);
        } else {
            height = maxHeight;
            width = (int) (height * ratio);
        }

        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);

        // 2. Nén chất lượng xuống còn 85%
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream);

        byte[] byteArray = outputStream.toByteArray();

        // 3. Trả về chuỗi Base64 siêu nhẹ
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }
}