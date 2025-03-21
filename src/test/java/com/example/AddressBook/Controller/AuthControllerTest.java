package com.example.AddressBook.Controller;

import com.example.AddressBook.controller.AuthController;
import com.example.AddressBook.dto.ForgotPasswordDTO;
import com.example.AddressBook.dto.LoginRequestDTO;
import com.example.AddressBook.dto.LoginResponseDTO;
import com.example.AddressBook.dto.UserDTO;
import com.example.AddressBook.services.AuthService;
import com.example.AddressBook.services.EmailService;
import com.example.AddressBook.services.RabbitMQProducer;
import jakarta.mail.MessagingException;
import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;


import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.client.ExpectedCount.times;

@RunWith(MockitoJUnitRunner.class)
public class AuthControllerTest {

    @Mock
    private AuthService authService;

    @Mock
    private EmailService emailService;

    @Mock
    private RabbitMQProducer rabbitMQProducer;

    @InjectMocks
    private AuthController authController;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    /** ✅ Test Registration */
    @Test
    public void testRegisterUser_Success() {
        UserDTO userDTO = new UserDTO("testUser", "test@example.com", "password123");

        when(authService.registerUser(userDTO)).thenReturn("User registered successfully!");
        doNothing().when(emailService).sendRegistrationEmail(userDTO.getEmail());

        ResponseEntity<String> response = authController.registerUser(userDTO);

        assertEquals("User registered successfully!", response.getBody());
        assertTrue(response.getStatusCode().is2xxSuccessful());
    }

    /** ✅ Test Login */
    @Test
    public void testLoginUser_Success() throws MessagingException {
        LoginRequestDTO loginRequest = new LoginRequestDTO("test@example.com", "password123");
        LoginResponseDTO responseDTO = new LoginResponseDTO("Login successful!", "mocked-jwt-token");

        when(authService.loginUser(loginRequest)).thenReturn(responseDTO);

        ResponseEntity<LoginResponseDTO> response = authController.loginUser(loginRequest);

        assertNotNull(response.getBody());
        assertEquals("Login successful!", response.getBody().getMessage());
        assertTrue(response.getStatusCode().is2xxSuccessful());
    }

    /** ✅ Test Login with Token */
    @Test
    public void testLoginWithToken_Success() throws MessagingException {
        when(authService.loginWithToken("validToken")).thenReturn(true);

        ResponseEntity<?> response = authController.loginWithToken("Bearer validToken");

        assertEquals("Login successful with token.", response.getBody());
        assertTrue(response.getStatusCode().is2xxSuccessful());
    }

    /** ✅ Test Login with Invalid Token */
    @Test
    public void testLoginWithToken_Invalid() throws MessagingException {
        when(authService.loginWithToken("invalidToken")).thenThrow(new IllegalArgumentException("Invalid or expired token!"));

        ResponseEntity<?> response = authController.loginWithToken("Bearer invalidToken");

        assertEquals("Invalid or expired token!", response.getBody());
        assertFalse(response.getStatusCode().is2xxSuccessful());
    }

    /** ✅ Test Forgot Password */
    @Test
    public void testForgotPassword_Success() throws MessagingException {
        ForgotPasswordDTO forgotPasswordDTO = new ForgotPasswordDTO("test@example.com", "newPassword");

        when(authService.forgotPassword(forgotPasswordDTO.getEmail(), forgotPasswordDTO.getNewPassword())).thenReturn(true);

        ResponseEntity<String> response = authController.forgotPassword(forgotPasswordDTO);

        assertEquals("Password has been changed successfully!", response.getBody());
        assertTrue(response.getStatusCode().is2xxSuccessful());
    }

    /** ✅ Test Forgot Password - User Not Found */
    @Test
    public void testForgotPassword_UserNotFound() throws MessagingException {
        ForgotPasswordDTO forgotPasswordDTO = new ForgotPasswordDTO("wrong@example.com", "newPassword");

        when(authService.forgotPassword(forgotPasswordDTO.getEmail(), forgotPasswordDTO.getNewPassword())).thenReturn(false);

        ResponseEntity<String> response = authController.forgotPassword(forgotPasswordDTO);

        assertEquals("Sorry! We cannot find the user email: wrong@example.com", response.getBody());
        assertFalse(response.getStatusCode().is2xxSuccessful());
    }

    /** ✅ Test Reset Password */
    @Test
    public void testResetPassword_Success() {
        Map<String, String> request = new HashMap<>();
        request.put("oldPassword", "oldPassword");
        request.put("newPassword", "newPassword");

        when(authService.resetPassword("test@example.com", "oldPassword", "newPassword")).thenReturn(true);

        ResponseEntity<String> response = authController.resetPassword("test@example.com", request);

        assertEquals("Password has been reset successfully!", response.getBody());
        assertTrue(response.getStatusCode().is2xxSuccessful());
    }

    /** ✅ Test Reset Password - Invalid Old Password */
    @Test
    public void testResetPassword_InvalidOldPassword() {
        Map<String, String> request = new HashMap<>();
        request.put("oldPassword", "wrongPassword");
        request.put("newPassword", "newPassword");

        when(authService.resetPassword("test@example.com", "wrongPassword", "newPassword"))
                .thenThrow(new IllegalArgumentException("Old password is incorrect!"));

        ResponseEntity<String> response = authController.resetPassword("test@example.com", request);

        assertEquals("Old password is incorrect!", response.getBody());
        assertFalse(response.getStatusCode().is2xxSuccessful());
    }

    /** ✅ Test Sending Message to RabbitMQ */
    @Test
    public void testSendMessageToRabbitMQ() {
        // Arrange
        String message = "Test Message";
        doNothing().when(rabbitMQProducer).sendMessage(message);

        // Act
        authController.sendMessage(message);

        // Assert
        verify(rabbitMQProducer, times(1)).sendMessage(message);  // ✅ Ensure method was called
    }
}