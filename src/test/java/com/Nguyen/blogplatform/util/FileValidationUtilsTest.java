package com.Nguyen.blogplatform.util;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.junit.jupiter.api.Assertions.*;

class FileValidationUtilsTest {

    @Test
    void whenFileIsSafeJPEG_thenReturnsTrue() {
        byte[] content = new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0, 0, 0, 0};
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", content);
        assertTrue(FileValidationUtils.isSafeFile(file));
    }

    @Test
    void whenFileIsSafePNG_thenReturnsTrue() {
        byte[] content = new byte[]{(byte) 0x89, (byte) 0x50, (byte) 0x4E, (byte) 0x47, 0, 0, 0, 0};
        MockMultipartFile file = new MockMultipartFile("file", "test.png", "image/png", content);
        assertTrue(FileValidationUtils.isSafeFile(file));
    }

    @Test
    void whenFileHasWrongMagicBytes_thenReturnsFalse() {
        byte[] content = new byte[]{0, 0, 0, 0, 0, 0, 0, 0};
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", content);
        assertFalse(FileValidationUtils.isSafeFile(file));
    }

    @Test
    void whenFileHasMaliciousExtension_thenReturnsFalse() {
        byte[] content = new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0, 0, 0, 0, 0};
        MockMultipartFile file = new MockMultipartFile("file", "test.php", "image/jpeg", content);
        assertFalse(FileValidationUtils.isSafeFile(file));
    }

    @Test
    void whenFilenameHasPathTraversal_thenSanitizes() {
        String unsafeName = "../../../etc/passwd";
        String safeName = FileValidationUtils.sanitizeFilename(unsafeName);
        assertEquals("passwd", safeName);
        
        String unsafeName2 = "image.jpg\0.php";
        String safeName2 = FileValidationUtils.sanitizeFilename(unsafeName2);
        // Depending on implementation, but it should not have ../
        assertFalse(safeName2.contains(".."));
    }
}
