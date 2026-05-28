package com.pmp.dto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class LoginRequestTest {

    @Test
    void createsLoginRequestWithUsernameAndPassword() {
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("testpass");

        assertEquals("testuser", request.getUsername());
        assertEquals("testpass", request.getPassword());
    }

    @Test
    void createsLoginRequestWithConstructor() {
        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("admin123");

        assertNotNull(request);
        assertEquals("admin", request.getUsername());
        assertEquals("admin123", request.getPassword());
    }
}
