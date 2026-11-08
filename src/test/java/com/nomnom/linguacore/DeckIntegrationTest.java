package com.nomnom.linguacore;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional   // mỗi test chạy trong một giao dịch, xong thì rollback → DB không giữ dữ liệu test
public class DeckIntegrationTest {
    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;

    // Đăng ký một tài khoản mới rồi đăng nhập, trả về token.
    // Gom về một chỗ để mỗi test chỉ cần đổi email, không chép lại 20 dòng.
    private String dangKyVaDangNhap(String email) throws Exception {
        String taiKhoan = objectMapper.writeValueAsString(Map.of(
                "email", email,
                "password", "matkhau123",
                "displayName", "Test User"
        ));
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON).content(taiKhoan))
                .andExpect(status().isCreated());

        String ketQua = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON).content(taiKhoan))   // dùng lại body: trường displayName thừa bị bỏ qua
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(ketQua).get("token").asText();
    }

    @Test
    void dangKy_roi_dangNhap_nhanDuocToken() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "email", "test1@example.com",
                "password", "matkhau123",
                "displayName", "Test User"
        ));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("test1@example.com"))
                .andExpect(jsonPath("$.passwordHash").doesNotExist());   // hash KHÔNG được lộ
    }

    @Test
    void luongDayDu_dangKy_dangNhap_taoDeck() throws Exception {
        String token = dangKyVaDangNhap("test2@example.com");

        mockMvc.perform(post("/api/decks")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("name", "N5 Test", "targetLang", "ja"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("N5 Test"));
    }

    @Test
    void khongCoToken_thiBiChan() throws Exception {
        // gọi GET /api/decks mà KHÔNG có header Authorization → chưa xác thực → 401
        mockMvc.perform(get("/api/decks"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void userA_khongThayDeckCuaUserB() throws Exception {
        String tokenA = dangKyVaDangNhap("userA@example.com");
        String tokenB = dangKyVaDangNhap("userB@example.com");   // email KHÁC userA

        // userA tạo một deck, giữ lại id
        String deckA = mockMvc.perform(post("/api/decks")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("name", "N5 Test", "targetLang", "ja"))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long deckIdA = objectMapper.readTree(deckA).get("id").asLong();

        // userB liệt kê deck: không thấy deck của A
        mockMvc.perform(get("/api/decks").header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        // userB gọi thẳng id deck của A: 404, không phải 403 → không lộ deck có tồn tại
        mockMvc.perform(get("/api/decks/" + deckIdA).header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isNotFound());
    }
}