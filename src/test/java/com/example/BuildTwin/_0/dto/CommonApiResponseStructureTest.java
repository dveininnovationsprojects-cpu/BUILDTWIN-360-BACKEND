package com.example.BuildTwin._0.dto;

import com.example.BuildTwin._0.dto.audit.AuditLogResponse;
import com.example.BuildTwin._0.dto.audit.AuditStatsResponse;
import com.example.BuildTwin._0.dto.auth.AuthResponse;
import com.example.BuildTwin._0.dto.auth.UserSummaryDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Quality Assurance Test Suite for Sprint B3-269: Implement Common API Response Structure.
 *
 * Verifies that the existing ApiResponse<T> contract matches the Frontend expectation 100%,
 * ensuring that:
 * 1. The top-level response properties ('status', 'success', 'message', 'data', 'timestamp') remain stable.
 * 2. No changes are required in the Frontend React / mobile application.
 * 3. JSON serialization formats match frontend Axios interceptor requirements across Auth and Audit Log modules.
 */
class CommonApiResponseStructureTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    @DisplayName("ApiResponse.success(data, message) should construct standard 200 HTTP response structure")
    void testSuccessResponseStructure() {
        String testData = "Test Payload";
        String message = "Operation completed successfully";

        ApiResponse<String> response = ApiResponse.success(testData, message);

        assertNotNull(response);
        assertEquals(200, response.getStatus());
        assertTrue(response.isSuccess());
        assertEquals(message, response.getMessage());
        assertEquals(testData, response.getData());
        assertNotNull(response.getTimestamp());
        assertTrue(response.getTimestamp().isBefore(LocalDateTime.now().plusSeconds(1)));
    }

    @Test
    @DisplayName("ApiResponse.created(data, message) should construct standard 201 Created response structure")
    void testCreatedResponseStructure() {
        Map<String, Object> createdEntity = Map.of("id", 101L, "name", "New Project");
        String message = "Resource created successfully";

        ApiResponse<Map<String, Object>> response = ApiResponse.created(createdEntity, message);

        assertNotNull(response);
        assertEquals(201, response.getStatus());
        assertTrue(response.isSuccess());
        assertEquals(message, response.getMessage());
        assertEquals(createdEntity, response.getData());
        assertNotNull(response.getTimestamp());
    }

    @Test
    @DisplayName("ApiResponse JSON serialization must preserve exact keys expected by Frontend (status, success, message, data, timestamp)")
    void testJsonSerializationParityForFrontend() throws JsonProcessingException {
        UserSummaryDto userSummary = UserSummaryDto.builder()
                .id(1L)
                .username("director_rajesh")
                .email("rajesh@buildtwin.com")
                .status("ACTIVE")
                .roles(Set.of("ROLE_DIRECTOR"))
                .build();

        AuthResponse authData = AuthResponse.builder()
                .accessToken("jwt.test.access.token")
                .refreshToken("jwt.test.refresh.token")
                .tokenType("Bearer")
                .expiresIn(86400000L)
                .user(userSummary)
                .build();

        ApiResponse<AuthResponse> apiResponse = ApiResponse.success(authData, "Login successful");

        String json = objectMapper.writeValueAsString(apiResponse);
        JsonNode root = objectMapper.readTree(json);

        // Verify all 5 root keys that frontend Axios interceptors depend upon
        assertTrue(root.has("status"), "Response must have 'status' key");
        assertTrue(root.has("success"), "Response must have 'success' key");
        assertTrue(root.has("message"), "Response must have 'message' key");
        assertTrue(root.has("data"), "Response must have 'data' key");
        assertTrue(root.has("timestamp"), "Response must have 'timestamp' key");

        // Verify values
        assertEquals(200, root.get("status").asInt());
        assertTrue(root.get("success").asBoolean());
        assertEquals("Login successful", root.get("message").asText());

        // Verify data payload fields
        JsonNode dataNode = root.get("data");
        assertEquals("jwt.test.access.token", dataNode.get("accessToken").asText());
        assertEquals("jwt.test.refresh.token", dataNode.get("refreshToken").asText());
        assertEquals("Bearer", dataNode.get("tokenType").asText());
        assertEquals(86400000L, dataNode.get("expiresIn").asLong());

        JsonNode userNode = dataNode.get("user");
        assertNotNull(userNode);
        assertEquals("director_rajesh", userNode.get("username").asText());
        assertEquals(1L, userNode.get("id").asLong());
    }

    @Test
    @DisplayName("ApiResponse with Collections should cleanly serialize array payloads without breaking frontend lists")
    void testListPayloadSerialization() throws JsonProcessingException {
        List<String> items = List.of("Item A", "Item B", "Item C");
        ApiResponse<List<String>> response = ApiResponse.success(items, "Items retrieved");

        String json = objectMapper.writeValueAsString(response);
        JsonNode root = objectMapper.readTree(json);

        assertTrue(root.get("data").isArray());
        assertEquals(3, root.get("data").size());
        assertEquals("Item A", root.get("data").get(0).asText());
    }

    @Test
    @DisplayName("ApiResponse Builder should allow custom status codes while preserving standard structure")
    void testCustomBuilderUsage() {
        ApiResponse<Object> customResponse = ApiResponse.builder()
                .status(202)
                .success(true)
                .message("Request accepted for background processing")
                .data(Map.of("taskId", "job-123"))
                .timestamp(LocalDateTime.now())
                .build();

        assertEquals(202, customResponse.getStatus());
        assertTrue(customResponse.isSuccess());
        assertEquals("Request accepted for background processing", customResponse.getMessage());
        assertNotNull(customResponse.getData());
        assertNotNull(customResponse.getTimestamp());
    }

    @Test
    @DisplayName("ApiResponse<AuditLogResponse> JSON serialization must strictly follow frontend contract")
    void testAuditLogApiResponseSerializationParity() throws JsonProcessingException {
        AuditLogResponse auditLog = AuditLogResponse.builder()
                .id(42L)
                .username("auditor_karthik")
                .action("APPROVE_DPR")
                .entityType("DPR")
                .entityId("888")
                .details("Daily Progress Report approved for Block A")
                .ipAddress("10.0.0.15")
                .createdAt(LocalDateTime.of(2026, 11, 12, 10, 30, 0))
                .build();

        ApiResponse<AuditLogResponse> response = ApiResponse.success(auditLog, "Audit log retrieved");

        String json = objectMapper.writeValueAsString(response);
        JsonNode root = objectMapper.readTree(json);

        // Verify root response structure
        assertTrue(root.has("status"));
        assertTrue(root.has("success"));
        assertTrue(root.has("message"));
        assertTrue(root.has("data"));
        assertTrue(root.has("timestamp"));
        assertEquals(200, root.get("status").asInt());
        assertTrue(root.get("success").asBoolean());
        assertEquals("Audit log retrieved", root.get("message").asText());

        // Verify nested audit payload structure
        JsonNode dataNode = root.get("data");
        assertEquals(42L, dataNode.get("id").asLong());
        assertEquals("auditor_karthik", dataNode.get("username").asText());
        assertEquals("APPROVE_DPR", dataNode.get("action").asText());
        assertEquals("DPR", dataNode.get("entityType").asText());
        assertEquals("888", dataNode.get("entityId").asText());
        assertEquals("Daily Progress Report approved for Block A", dataNode.get("details").asText());
        assertEquals("10.0.0.15", dataNode.get("ipAddress").asText());
    }

    @Test
    @DisplayName("ApiResponse<AuditStatsResponse> JSON serialization must support analytics breakdown for frontend dashboards")
    void testAuditStatsApiResponseSerializationParity() throws JsonProcessingException {
        AuditStatsResponse stats = AuditStatsResponse.builder()
                .totalLogs(1250L)
                .todayLogs(45L)
                .uniqueUsersCount(28L)
                .topActions(Map.of("LOGIN", 500L, "CREATE_PROJECT", 120L))
                .topEntities(Map.of("PROJECT", 300L, "DPR", 250L))
                .recentActivities(List.of())
                .systemHealth("HEALTHY")
                .generatedAt(LocalDateTime.of(2026, 11, 12, 12, 0, 0))
                .build();

        ApiResponse<AuditStatsResponse> response = ApiResponse.success(stats, "Audit stats retrieved");

        String json = objectMapper.writeValueAsString(response);
        JsonNode root = objectMapper.readTree(json);

        assertTrue(root.has("status"));
        assertTrue(root.has("success"));
        assertTrue(root.has("data"));

        JsonNode data = root.get("data");
        assertEquals(1250L, data.get("totalLogs").asLong());
        assertEquals(45L, data.get("todayLogs").asLong());
        assertEquals(28L, data.get("uniqueUsersCount").asLong());
        assertEquals("HEALTHY", data.get("systemHealth").asText());
        assertEquals(500L, data.get("topActions").get("LOGIN").asLong());
        assertEquals(300L, data.get("topEntities").get("PROJECT").asLong());
    }
}
