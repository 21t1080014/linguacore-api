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
@Transactional

public class DeckIntegrationTest {
    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;

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
        // 1. Đăng ký
        String dangKy = objectMapper.writeValueAsString(Map.of(
                "email", "test2@example.com",
                "password", "matkhau123",
                "displayName", "Test Hai"
        ));
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON).content(dangKy))
                .andExpect(status().isCreated());

        // 2. Đăng nhập, LẤY token từ response
        String dangNhap = objectMapper.writeValueAsString(Map.of(
                "email", "test2@example.com",
                "password", "matkhau123"
        ));
        String ketQua = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON).content(dangNhap))
                .andExpect(status().isOk())
                .andReturn()                          // ← lấy toàn bộ kết quả
                .getResponse().getContentAsString();  // ← đổi thành chuỗi JSON

        String token = objectMapper.readTree(ketQua).get("token").asText();  // ← bóc trường token

        // 3. Dùng token tạo deck
        String taoDeck = objectMapper.writeValueAsString(Map.of(
                "name", "N5 Test", "targetLang", "ja"
        ));
        mockMvc.perform(post("/api/decks")
                        .header("Authorization", "Bearer " + token)   // ← đính token
                        .contentType(MediaType.APPLICATION_JSON).content(taoDeck))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("N5 Test"));
    }
    @Test
    void khongCoToken_thiBiChan() throws Exception {
        // gọi GET /api/decks mà KHÔNG có header Authorization
        mockMvc.perform(get("/api/decks"))
                .andExpect(status().isForbidden());
        // mong đợi: status().isForbidden()   (403, như bạn thấy trong Postman)
    }
    @Test
    void userA_khongThayDeckCuaUserB() throws Exception {
        // 1. Đăng ký + đăng nhập userA, tạo một deck
        String dangKy1 = objectMapper.writeValueAsString(Map.of(
                "email", "test2@example.com",
                "password", "matkhau123",
                "displayName", "Test Hai"
        ));
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON).content(dangKy1))
                .andExpect(status().isCreated());

        String dangNhap1 = objectMapper.writeValueAsString(Map.of(
                "email", "test2@example.com",
                "password", "matkhau123"
        ));
        String ketQua = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON).content(dangNhap1))
                .andExpect(status().isOk())
                .andReturn()                          // ← lấy toàn bộ kết quả
                .getResponse().getContentAsString();  // ← đổi thành chuỗi JSON

        String token1 = objectMapper.readTree(ketQua).get("token").asText();  // ← bóc trường token

        String taoDeck1 = objectMapper.writeValueAsString(Map.of(
                "name", "N5 Test", "targetLang", "ja"
        ));
        mockMvc.perform(post("/api/decks")
                        .header("Authorization", "Bearer " + token1)   // ← đính token
                        .contentType(MediaType.APPLICATION_JSON).content(taoDeck1))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("N5 Test"));
        // 2. Đăng ký + đăng nhập userB (email khác), lấy token B
        String dangKy2 = objectMapper.writeValueAsString(Map.of(
                "email", "test2@example.com",
                "password", "matkhau123",
                "displayName", "Test Hai"
        ));
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON).content(dangKy2))
                .andExpect(status().isCreated());

        String dangNhap2 = objectMapper.writeValueAsString(Map.of(
                "email", "test1@example.com",
                "password", "matkhau123"
        ));
        String ketQua2 = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON).content(dangNhap2))
                .andExpect(status().isOk())
                .andReturn()                          // ← lấy toàn bộ kết quả
                .getResponse().getContentAsString();  // ← đổi thành chuỗi JSON

        String token2 = objectMapper.readTree(ketQua2).get("token").asText();
        // 3. GET /api/decks với token B → mong đợi jsonPath("$").isEmpty() hoặc $.length()==0
        mockMvc.perform(get("/api/decks")
                .header("Authorization", "Bearer " + token2))
                .andExpect(jsonPath("$.length()").value(0));
    }
}
