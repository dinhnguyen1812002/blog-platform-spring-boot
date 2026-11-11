package com.Nguyen.blogplatform.shared.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class SlugUtilTest {

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "Đà Nẵng|da-nang",
            "Đường đi|duong-di",
            "Đường đi Đà Nẵng|duong-di-da-nang",
            "Hello World|hello-world",
            "Kinh nghiệm du lịch Huế – Hội An|kinh-nghiem-du-lich-hue-hoi-an",
            "  Spring   Boot  3.x  |spring-boot-3x",
            "C# & .NET / ASP|c-net-asp",
            "Ăn uống ở Sài Gòn|an-uong-o-sai-gon",
    })
    void slugifiesVietnameseAndLatin(String input, String expected) {
        assertThat(SlugUtil.toSlug(input)).isEqualTo(expected);
    }

    @Test
    void returnsEmptyWhenNothingUsable() {
        assertThat(SlugUtil.toSlug("🔥🔥🔥")).isEmpty();
        assertThat(SlugUtil.toSlug(null)).isNull();
    }

    @Test
    void capsLength() {
        String slug = SlugUtil.toSlug("word ".repeat(100));
        assertThat(slug.length()).isLessThanOrEqualTo(SlugUtil.MAX_LENGTH);
        assertThat(slug).doesNotEndWith("-");
    }
}
