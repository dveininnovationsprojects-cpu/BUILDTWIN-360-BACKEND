package com.example.BuildTwin._0.controller;

import com.example.BuildTwin._0.dto.dpr.*;
import com.example.BuildTwin._0.model.BlockerCategory;
import com.example.BuildTwin._0.model.BlockerSeverity;
import com.example.BuildTwin._0.model.BlockerStatus;
import com.example.BuildTwin._0.model.DprStatus;
import com.example.BuildTwin._0.model.WeatherCondition;
import com.example.BuildTwin._0.model.WorkShift;
import com.example.BuildTwin._0.service.DailyProgressReportService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class DailyProgressReportControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private DailyProgressReportService dprService;

    @InjectMocks
    private DailyProgressReportController dprController;

    private DailyProgressReportResponse mockResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(dprController).build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        mockResponse = DailyProgressReportResponse.builder()
                .id(10L)
                .reportCode("DPR-PRJ-SKY-001-20261010-DAY")
                .projectId(1L)
                .projectName("Skyline Towers")
                .projectCode("PRJ-SKY-001")
                .reportDate(LocalDate.of(2026, 10, 10))
                .shift(WorkShift.DAY_SHIFT)
                .weatherCondition(WeatherCondition.SUNNY)
                .temperatureCelsius(32.5)
                .status(DprStatus.DRAFT)
                .siteEngineer("Engr. Rajesh")
                .generalNotes("Concrete casting underway")
                .safetyIncidentsCount(0)
                .totalManpowerCount(12)
                .totalEquipmentCount(2)
                .totalActivitiesLogged(1)
                .build();
    }

    @Test
    @DisplayName("POST /api/v1/projects/{projectId}/dpr - 201 Created")
    void createDpr_ReturnsCreated() throws Exception {
        CreateDprRequest request = CreateDprRequest.builder()
                .reportDate(LocalDate.of(2026, 10, 10))
                .shift(WorkShift.DAY_SHIFT)
                .weatherCondition(WeatherCondition.SUNNY)
                .siteEngineer("Engr. Rajesh")
                .build();

        when(dprService.createDpr(eq(1L), any(CreateDprRequest.class), any())).thenReturn(mockResponse);

        mockMvc.perform(post("/api/v1/projects/1/dpr")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.data.id").value(10))
                .andExpect(jsonPath("$.data.reportCode").value("DPR-PRJ-SKY-001-20261010-DAY"));
    }

    @Test
    @DisplayName("GET /api/v1/projects/{projectId}/dpr - 200 OK")
    void getDprsByProject_ReturnsOk() throws Exception {
        when(dprService.getDprsByProject(eq(1L), any(), any(), any())).thenReturn(List.of(mockResponse));

        mockMvc.perform(get("/api/v1/projects/1/dpr"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data[0].id").value(10));
    }

    @Test
    @DisplayName("GET /api/v1/projects/{projectId}/dpr/summary - 200 OK")
    void getDprSummary_ReturnsOk() throws Exception {
        DprSummaryResponse summary = DprSummaryResponse.builder()
                .projectId(1L)
                .projectName("Skyline Towers")
                .totalReports(15L)
                .approvedReports(12L)
                .draftReports(2L)
                .submittedReports(1L)
                .build();

        when(dprService.getDprSummary(1L)).thenReturn(summary);

        mockMvc.perform(get("/api/v1/projects/1/dpr/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalReports").value(15))
                .andExpect(jsonPath("$.data.approvedReports").value(12));
    }

    @Test
    @DisplayName("GET /api/v1/dpr/{dprId} - 200 OK")
    void getDprById_ReturnsOk() throws Exception {
        when(dprService.getDprById(10L)).thenReturn(mockResponse);

        mockMvc.perform(get("/api/v1/dpr/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(10));
    }

    @Test
    @DisplayName("PUT /api/v1/dpr/{dprId} - 200 OK")
    void updateDpr_ReturnsOk() throws Exception {
        UpdateDprRequest request = UpdateDprRequest.builder()
                .temperatureCelsius(31.0)
                .generalNotes("Updated notes")
                .build();

        when(dprService.updateDpr(eq(10L), any(UpdateDprRequest.class), any())).thenReturn(mockResponse);

        mockMvc.perform(put("/api/v1/dpr/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(10));
    }

    @Test
    @DisplayName("DELETE /api/v1/dpr/{dprId} - 200 OK")
    void deleteDpr_ReturnsOk() throws Exception {
        doNothing().when(dprService).deleteDpr(10L);

        mockMvc.perform(delete("/api/v1/dpr/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("POST /api/v1/dpr/{dprId}/activities - 200 OK")
    void addActivityItem_ReturnsOk() throws Exception {
        DprActivityItemRequest request = DprActivityItemRequest.builder()
                .activityId(101L)
                .quantityToday(25.0)
                .build();

        when(dprService.addActivityItem(eq(10L), any(DprActivityItemRequest.class))).thenReturn(mockResponse);

        mockMvc.perform(post("/api/v1/dpr/10/activities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/v1/dpr/{dprId}/activities - 200 OK")
    void getActivityItems_ReturnsOk() throws Exception {
        DprActivityItemResponse item = DprActivityItemResponse.builder()
                .id(1L)
                .activityId(101L)
                .quantityToday(25.0)
                .build();

        when(dprService.getActivityItems(10L)).thenReturn(List.of(item));

        mockMvc.perform(get("/api/v1/dpr/10/activities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data[0].id").value(1));
    }

    @Test
    @DisplayName("GET /api/v1/dpr/{dprId}/activities/{itemId} - 200 OK")
    void getActivityItemById_ReturnsOk() throws Exception {
        DprActivityItemResponse item = DprActivityItemResponse.builder()
                .id(1L)
                .activityId(101L)
                .quantityToday(25.0)
                .build();

        when(dprService.getActivityItemById(10L, 1L)).thenReturn(item);

        mockMvc.perform(get("/api/v1/dpr/10/activities/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    @DisplayName("PUT /api/v1/dpr/{dprId}/activities/{itemId} - 200 OK")
    void updateActivityItem_ReturnsOk() throws Exception {
        UpdateDprActivityItemRequest request = UpdateDprActivityItemRequest.builder()
                .quantityToday(30.0)
                .remarks("Updated remarks")
                .build();

        DprActivityItemResponse item = DprActivityItemResponse.builder()
                .id(1L)
                .activityId(101L)
                .quantityToday(30.0)
                .build();

        when(dprService.updateActivityItem(eq(10L), eq(1L), any(UpdateDprActivityItemRequest.class))).thenReturn(item);

        mockMvc.perform(put("/api/v1/dpr/10/activities/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.quantityToday").value(30.0));
    }

    @Test
    @DisplayName("DELETE /api/v1/dpr/{dprId}/activities/{itemId} - 200 OK")
    void removeActivityItem_ReturnsOk() throws Exception {
        when(dprService.removeActivityItem(10L, 5L)).thenReturn(mockResponse);

        mockMvc.perform(delete("/api/v1/dpr/10/activities/5"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/v1/activities/{activityId}/dpr-progress - 200 OK")
    void getActivityDprHistory_ReturnsOk() throws Exception {
        DprActivityItemResponse item = DprActivityItemResponse.builder()
                .id(1L)
                .activityId(101L)
                .quantityToday(25.0)
                .build();

        when(dprService.getActivityDprHistory(101L)).thenReturn(List.of(item));

        mockMvc.perform(get("/api/v1/activities/101/dpr-progress"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data[0].id").value(1));
    }

    @Test
    @DisplayName("GET /api/v1/dpr/{dprId}/quantity-tracking - 200 OK")
    void getDprQuantityTracking_ReturnsOk() throws Exception {
        DprQuantityTrackingSummaryResponse summary = DprQuantityTrackingSummaryResponse.builder()
                .dprId(10L)
                .reportCode("DPR-PRJ-SKY-001-20261010-DAY")
                .totalActivitiesLogged(1)
                .build();

        when(dprService.getDprQuantityTracking(10L)).thenReturn(summary);

        mockMvc.perform(get("/api/v1/dpr/10/quantity-tracking"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.dprId").value(10));
    }

    @Test
    @DisplayName("GET /api/v1/activities/{activityId}/quantity-tracking - 200 OK")
    void getActivityQuantityTracking_ReturnsOk() throws Exception {
        ActivityQuantityTrackingResponse tracking = ActivityQuantityTrackingResponse.builder()
                .activityId(101L)
                .activityCode("ACT-CIV-001")
                .plannedQuantity(500.0)
                .cumulativeQuantity(100.0)
                .balanceQuantity(400.0)
                .build();

        when(dprService.getActivityQuantityTracking(101L)).thenReturn(tracking);

        mockMvc.perform(get("/api/v1/activities/101/quantity-tracking"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.activityId").value(101));
    }

    @Test
    @DisplayName("GET /api/v1/projects/{projectId}/activity-quantity-tracking - 200 OK")
    void getProjectQuantityTracking_ReturnsOk() throws Exception {
        ProjectQuantityTrackingResponse projectTracking = ProjectQuantityTrackingResponse.builder()
                .projectId(1L)
                .projectCode("PRJ-SKY-001")
                .totalActivities(5)
                .completedActivities(1)
                .build();

        when(dprService.getProjectQuantityTracking(eq(1L), any(), any())).thenReturn(projectTracking);

        mockMvc.perform(get("/api/v1/projects/1/activity-quantity-tracking"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.projectId").value(1));
    }

    @Test
    @DisplayName("POST /api/v1/dpr/{dprId}/labor - 200 OK")
    void addLaborItem_ReturnsOk() throws Exception {
        DprLaborItemRequest request = DprLaborItemRequest.builder()
                .tradeCategory("Carpenters")
                .headcount(6)
                .build();

        when(dprService.addLaborItem(eq(10L), any(DprLaborItemRequest.class))).thenReturn(mockResponse);

        mockMvc.perform(post("/api/v1/dpr/10/labor")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /api/v1/dpr/{dprId}/labor/{itemId} - 200 OK")
    void removeLaborItem_ReturnsOk() throws Exception {
        when(dprService.removeLaborItem(10L, 6L)).thenReturn(mockResponse);

        mockMvc.perform(delete("/api/v1/dpr/10/labor/6"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/v1/dpr/{dprId}/equipment - 200 OK")
    void addEquipmentItem_ReturnsOk() throws Exception {
        DprEquipmentItemRequest request = DprEquipmentItemRequest.builder()
                .equipmentName("Tower Crane")
                .quantity(1)
                .operatingHours(8.0)
                .build();

        when(dprService.addEquipmentItem(eq(10L), any(DprEquipmentItemRequest.class))).thenReturn(mockResponse);

        mockMvc.perform(post("/api/v1/dpr/10/equipment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /api/v1/dpr/{dprId}/equipment/{itemId} - 200 OK")
    void removeEquipmentItem_ReturnsOk() throws Exception {
        when(dprService.removeEquipmentItem(10L, 7L)).thenReturn(mockResponse);

        mockMvc.perform(delete("/api/v1/dpr/10/equipment/7"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/v1/dpr/{dprId}/materials - 200 OK")
    void addMaterialItem_ReturnsOk() throws Exception {
        DprMaterialItemRequest request = DprMaterialItemRequest.builder()
                .materialName("Cement OPC 53")
                .unitOfMeasure("BAGS")
                .quantityConsumed(50.0)
                .build();

        when(dprService.addMaterialItem(eq(10L), any(DprMaterialItemRequest.class))).thenReturn(mockResponse);

        mockMvc.perform(post("/api/v1/dpr/10/materials")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /api/v1/dpr/{dprId}/materials/{itemId} - 200 OK")
    void removeMaterialItem_ReturnsOk() throws Exception {
        when(dprService.removeMaterialItem(10L, 8L)).thenReturn(mockResponse);

        mockMvc.perform(delete("/api/v1/dpr/10/materials/8"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/v1/dpr/{dprId}/photos - 200 OK")
    void addPhotoAttachment_ReturnsOk() throws Exception {
        DprPhotoAttachmentRequest request = DprPhotoAttachmentRequest.builder()
                .photoUrl("https://storage.example.com/site.jpg")
                .caption("Reinforcement check")
                .build();

        when(dprService.addPhotoAttachment(eq(10L), any(DprPhotoAttachmentRequest.class))).thenReturn(mockResponse);

        mockMvc.perform(post("/api/v1/dpr/10/photos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /api/v1/dpr/{dprId}/photos/{attachmentId} - 200 OK")
    void removePhotoAttachment_ReturnsOk() throws Exception {
        when(dprService.removePhotoAttachment(10L, 9L)).thenReturn(mockResponse);

        mockMvc.perform(delete("/api/v1/dpr/10/photos/9"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/v1/dpr/{dprId}/submit - 200 OK")
    void submitDpr_ReturnsOk() throws Exception {
        mockResponse.setStatus(DprStatus.SUBMITTED);
        when(dprService.submitDpr(eq(10L), any())).thenReturn(mockResponse);

        mockMvc.perform(post("/api/v1/dpr/10/submit"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("SUBMITTED"));
    }

    @Test
    @DisplayName("POST /api/v1/dpr/{dprId}/approve - 200 OK")
    void approveDpr_ReturnsOk() throws Exception {
        mockResponse.setStatus(DprStatus.APPROVED);
        when(dprService.approveDpr(eq(10L), any())).thenReturn(mockResponse);

        mockMvc.perform(post("/api/v1/dpr/10/approve"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("APPROVED"));
    }

    @Test
    @DisplayName("POST /api/v1/dpr/{dprId}/reject - 200 OK")
    void rejectDpr_ReturnsOk() throws Exception {
        RejectDprRequest request = RejectDprRequest.builder()
                .rejectionReason("Incorrect concrete cube count")
                .build();

        mockResponse.setStatus(DprStatus.REJECTED);
        when(dprService.rejectDpr(eq(10L), any(RejectDprRequest.class), any())).thenReturn(mockResponse);

        mockMvc.perform(post("/api/v1/dpr/10/reject")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("REJECTED"));
    }

    // =========================================================================
    // BLOCKER & REMARKS SUPPORT CONTROLLER TESTS
    // =========================================================================

    @Test
    @DisplayName("GET /api/v1/dpr/{dprId}/blockers - 200 OK")
    void getDprBlockers_ReturnsOk() throws Exception {
        DprBlockerItemResponse blocker = DprBlockerItemResponse.builder()
                .itemId(1L)
                .dprId(10L)
                .reportCode("DPR-PRJ-SKY-001-20261010-DAY")
                .activityId(101L)
                .activityCode("ACT-CIV-001")
                .isBlocker(true)
                .blockerCategory(BlockerCategory.EQUIPMENT_BREAKDOWN)
                .blockerReason("Concrete boom placer breakdown")
                .blockerSeverity(BlockerSeverity.HIGH)
                .blockerStatus(BlockerStatus.OPEN)
                .impactHours(2.5)
                .remarks("Technician called to replace hydraulic hose")
                .build();

        when(dprService.getDprBlockers(10L)).thenReturn(List.of(blocker));

        mockMvc.perform(get("/api/v1/dpr/10/blockers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].itemId").value(1))
                .andExpect(jsonPath("$.data[0].isBlocker").value(true))
                .andExpect(jsonPath("$.data[0].blockerCategory").value("EQUIPMENT_BREAKDOWN"))
                .andExpect(jsonPath("$.data[0].blockerReason").value("Concrete boom placer breakdown"))
                .andExpect(jsonPath("$.data[0].remarks").value("Technician called to replace hydraulic hose"));
    }

    @Test
    @DisplayName("GET /api/v1/projects/{projectId}/dpr/blockers - 200 OK with filters")
    void getProjectDprBlockers_ReturnsOk() throws Exception {
        DprBlockerItemResponse blocker = DprBlockerItemResponse.builder()
                .itemId(2L)
                .dprId(10L)
                .activityId(101L)
                .isBlocker(true)
                .blockerCategory(BlockerCategory.MATERIAL_SHORTAGE)
                .blockerStatus(BlockerStatus.OPEN)
                .blockerSeverity(BlockerSeverity.CRITICAL)
                .blockerReason("Rebar supply delayed")
                .remarks("Vendor contact notified")
                .build();

        when(dprService.getProjectDprBlockers(eq(1L), eq(BlockerStatus.OPEN), eq(BlockerCategory.MATERIAL_SHORTAGE), eq(BlockerSeverity.CRITICAL), eq(101L)))
                .thenReturn(List.of(blocker));

        mockMvc.perform(get("/api/v1/projects/1/dpr/blockers")
                        .param("status", "OPEN")
                        .param("category", "MATERIAL_SHORTAGE")
                        .param("severity", "CRITICAL")
                        .param("activityId", "101"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].itemId").value(2))
                .andExpect(jsonPath("$.data[0].blockerCategory").value("MATERIAL_SHORTAGE"));
    }

    @Test
    @DisplayName("GET /api/v1/projects/{projectId}/dpr/blockers/summary - 200 OK")
    void getProjectBlockerSummary_ReturnsOk() throws Exception {
        DprBlockerSummaryResponse summary = DprBlockerSummaryResponse.builder()
                .projectId(1L)
                .projectCode("PRJ-SKY-001")
                .projectName("Skyline Towers")
                .totalBlockersCount(5)
                .openBlockersCount(2)
                .resolvedBlockersCount(3)
                .criticalBlockersCount(1)
                .totalImpactHours(12.5)
                .build();

        when(dprService.getProjectBlockerSummary(1L)).thenReturn(summary);

        mockMvc.perform(get("/api/v1/projects/1/dpr/blockers/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.projectId").value(1))
                .andExpect(jsonPath("$.data.totalBlockersCount").value(5))
                .andExpect(jsonPath("$.data.openBlockersCount").value(2))
                .andExpect(jsonPath("$.data.totalImpactHours").value(12.5));
    }

    @Test
    @DisplayName("POST /api/v1/dpr/{dprId}/activities/{itemId}/resolve-blocker - 200 OK")
    void resolveBlocker_ReturnsOk() throws Exception {
        ResolveDprBlockerRequest request = ResolveDprBlockerRequest.builder()
                .resolutionNotes("Replacement hose fitted and tested")
                .status(BlockerStatus.RESOLVED)
                .remarks("Concrete pour restarted")
                .build();

        DprBlockerItemResponse resolved = DprBlockerItemResponse.builder()
                .itemId(1L)
                .dprId(10L)
                .isBlocker(true)
                .blockerStatus(BlockerStatus.RESOLVED)
                .blockerResolutionNotes("Replacement hose fitted and tested")
                .remarks("Concrete pour restarted")
                .build();

        when(dprService.resolveBlocker(eq(10L), eq(1L), any(ResolveDprBlockerRequest.class), any())).thenReturn(resolved);

        mockMvc.perform(post("/api/v1/dpr/10/activities/1/resolve-blocker")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.blockerStatus").value("RESOLVED"))
                .andExpect(jsonPath("$.data.blockerResolutionNotes").value("Replacement hose fitted and tested"))
                .andExpect(jsonPath("$.data.remarks").value("Concrete pour restarted"));
    }
}
