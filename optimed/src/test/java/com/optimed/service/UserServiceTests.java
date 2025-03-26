/*
package com.optimed.service;

import com.optimed.dto.*;
import com.optimed.entity.*;
import com.optimed.entity.enums.*;
import com.optimed.repository.*;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class UserServiceTests {
    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User user;
    private DoctorProfile doctorProfile;
    private PatientProfile patientProfile;
    private RegisterRequest registerRequest;
    private DoctorRequest doctorRequest;
    private PatientRequest patientRequest;

    @BeforeEach
    void setUp() {
        when(passwordEncoder.encode(any())).thenAnswer(invocation -> invocation.getArgument(0));

        user = User.builder()
                .id(UUID.randomUUID())
                .username("testuser")
                .password("password")
                .email("test@example.com")
                .enabled(true)
                .role(Role.PATIENT)
                .build();

        doctorProfile = DoctorProfile.builder()
                .id(UUID.randomUUID())
                .user(user)
                .fullName("Dr. Smith")
                .specialization(Specialization.CARDIOLOGY)
                .build();

        patientProfile = PatientProfile.builder()
                .id(UUID.randomUUID())
                .user(user)
                .fullName("John Doe")
                .build();

        registerRequest = new RegisterRequest(
                "newuser",
                "password",
                "new@example.com",
                Role.PATIENT
        );

        patientRequest = new PatientRequest(
                "John Doe",
                null,
                "123 Main St",
                "john@example.com",
                Gender.MALE,
                "+1234567890",
                LocalDate.of(1990, 1, 1),
                "No known conditions",
                null
        );
    }

    @Test
    void testFindByUsername_WhenUserExists_ReturnsUser() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        // When
        Optional<User> result = userService.findByUsername("testuser");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(user);
        verify(userRepository).findByUsername("testuser");
    }

    @Test
    void testFindByUsername_WhenUserDoesNotExist_ReturnsEmpty() {
        // Given
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // When
        Optional<User> result = userService.findByUsername("nonexistent");

        // Then
        assertThat(result).isNotPresent();
        verify(userRepository).findByUsername("nonexistent");
    }

    @Test
    void testRegisterUser_WithPatientRole_CreatesUserAndPatientProfile() {
        // Given
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(patientRepository.save(any(PatientProfile.class))).thenReturn(patientProfile);

        // When
        userService.registerUser(registerRequest);

        // Then
        verify(userRepository).save(any(User.class));
        verify(patientRepository).save(any(PatientProfile.class));
        verify(passwordEncoder).encode(registerRequest.getPassword());
    }

    @Test
    void testRegisterUser_WithDoctorRole_CreatesUserAndDoctorProfile() {
        // Given
        RegisterRequest doctorRequest = new RegisterRequest(
                "docuser",
                "password",
                "doc@example.com",
                Role.DOCTOR);
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(doctorRepository.save(any(DoctorProfile.class))).thenReturn(doctorProfile);

        // When
        userService.registerUser(doctorRequest);

        // Then
        verify(userRepository).save(any(User.class));
        verify(doctorRepository).save(any(DoctorProfile.class));
        verify(passwordEncoder).encode(doctorRequest.getPassword());
    }

    @Test
    void testCountUsers_ReturnsUserCount() {
        // Given
        when(userRepository.count()).thenReturn(5L);

        // When
        long count = userService.countUsers();

        // Then
        assertThat(count).isEqualTo(5L);
        verify(userRepository).count();
    }

    @Test
    void testGetRecentUsers_ReturnsTop10Users() {
        // Given
        when(userRepository.findTop10ByOrderByIdDesc()).thenReturn(Collections.singletonList(user));

        // When
        List<User> recentUsers = userService.getRecentUsers();

        // Then
        assertThat(recentUsers.get(0)).isEqualTo(user);
        verify(userRepository).findTop10ByOrderByIdDesc();
    }

    @Test
    void testGetUserById_WhenUserExists_ReturnsUser() {
        // Given
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        // When
        User result = userService.getUserById(user.getId());

        // Then
        assertThat(result).isEqualTo(user);
        verify(userRepository).findById(user.getId());
    }

    @Test
    void testGetUserById_WhenUserDoesNotExist_ThrowsException() {
        // Given
        when(userRepository.findById(UUID.randomUUID())).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.getUserById(UUID.randomUUID()))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void testGetAllNonAdminUsers_ReturnsPagedUsers() {
        // Given
        Page<User> page = Page.empty();
        when(userRepository.findAllNonAdminUsers(any(Pageable.class))).thenReturn(page);

        // When
        Page<User> result = userService.getAllNonAdminUsers(PageRequest.of(0, 10));

        // Then
        assertThat(result).isNotNull();
        verify(userRepository).findAllNonAdminUsers(any(Pageable.class));
    }

    @Test
    void testUpdateUser_UpdatesUserSuccessfully() {
        // Given
        UserRequest updateRequest = new UserRequest(
                "updateduser",
                "updated@example.com",
                Role.PATIENT,
                "123"
        );
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        // When
        userService.updateUser(user.getId(), updateRequest);

        // Then
        verify(userRepository).findById(user.getId());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testCompleteDoctorProfile_UpdatesProfileSuccessfully() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(doctorRepository.findByUserId(user.getId())).thenReturn(Optional.empty());

        // When
        userService.completeDoctorProfile("testuser", doctorRequest);

        // Then
        verify(userRepository).findByUsername("testuser");
        verify(doctorRepository).findByUserId(user.getId());
        verify(doctorRepository).save(any(DoctorProfile.class));
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testCompletePatientProfile_UpdatesProfileSuccessfully() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(patientRepository.findByUserId(user.getId())).thenReturn(Optional.empty());

        // When
        userService.completePatientProfile("testuser", patientRequest);

        // Then
        verify(userRepository).findByUsername("testuser");
        verify(patientRepository).findByUserId(user.getId());
        verify(patientRepository).save(any(PatientProfile.class));
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testDeleteUser_DeletesUserSuccessfully() {
        // When
        userService.deleteUser(user.getId());

        // Then
        verify(userRepository).deleteById(user.getId());
    }

    @Test
    void testStoreImage_WithValidImage_ReturnsImageUrl() throws IOException {
        // Given
        MultipartFile file = mock(MultipartFile.class);
        when(file.getContentType()).thenReturn("image/jpeg");
        when(file.getOriginalFilename()).thenReturn("test.jpg");
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream("test".getBytes()));

        // When
        String result = userService.storeImage(file);

        // Then
        assertThat(result).startsWith("/dashboard/img/");
        verify(file).getContentType();
        verify(file).getOriginalFilename();
        verify(file).getInputStream();
    }

    @Test
    void testStoreImage_WithEmptyFile_ReturnsDefaultUrl() {
        // Given
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(true);

        // When
        String result = userService.storeImage(file);

        // Then
        assertThat(result).isEqualTo("/dashboard/img/default.png");
        verify(file).isEmpty();
    }

    @Test
    void testStoreImage_WithInvalidContentType_ThrowsException() {
        // Given
        MultipartFile file = mock(MultipartFile.class);
        when(file.getContentType()).thenReturn("text/plain");

        // When & Then
        assertThatThrownBy(() -> userService.storeImage(file))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Only image files are allowed");
    }
}*/
