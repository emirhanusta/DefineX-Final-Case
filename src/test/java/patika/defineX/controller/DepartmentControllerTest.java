package patika.defineX.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import patika.defineX.dto.request.DepartmentRequest;
import patika.defineX.dto.response.DepartmentResponse;
import patika.defineX.service.DepartmentService;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class DepartmentControllerTest {

    @Mock
    private DepartmentService departmentService;

    @InjectMocks
    private DepartmentController departmentController;

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void testFindAll() throws Exception {
        DepartmentResponse departmentResponse = new DepartmentResponse(UUID.randomUUID(), "IT Department");
        List<DepartmentResponse> departmentResponses = List.of(departmentResponse);

        when(departmentService.listAll()).thenReturn(departmentResponses);

        mockMvc = MockMvcBuilders.standaloneSetup(departmentController).build();

        mockMvc.perform(get("/api/departments/v1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].name").value("IT Department"));
    }

    @Test
    void testFindById() throws Exception {
        UUID id = UUID.randomUUID();
        DepartmentResponse departmentResponse = new DepartmentResponse(id, "IT Department");

        when(departmentService.getById(id)).thenReturn(departmentResponse);

        mockMvc = MockMvcBuilders.standaloneSetup(departmentController).build();

        mockMvc.perform(get("/api/departments/v1/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.name").value("IT Department"));
    }

    @Test
    @WithMockUser(authorities = "PROJECT_MANAGER")
    void testSave() throws Exception {
        DepartmentRequest departmentRequest = new DepartmentRequest("IT Department");
        DepartmentResponse departmentResponse = new DepartmentResponse(UUID.randomUUID(), "IT Department");

        when(departmentService.save(any(DepartmentRequest.class))).thenReturn(departmentResponse);

        mockMvc = MockMvcBuilders.standaloneSetup(departmentController).build();

        mockMvc.perform(post("/api/departments/v1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(departmentRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("IT Department"));
    }

    @Test
    @WithMockUser(authorities = "PROJECT_MANAGER")
    void testUpdate() throws Exception {
        UUID id = UUID.randomUUID();
        DepartmentRequest departmentRequest = new DepartmentRequest("Updated IT Department");
        DepartmentResponse departmentResponse = new DepartmentResponse(id, "Updated IT Department");

        when(departmentService.update(any(UUID.class), any(DepartmentRequest.class))).thenReturn(departmentResponse);

        mockMvc = MockMvcBuilders.standaloneSetup(departmentController).build();

        mockMvc.perform(put("/api/departments/v1/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(departmentRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.name").value("Updated IT Department"));
    }

    @Test
    @WithMockUser(authorities = "PROJECT_MANAGER")
    void testDelete() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc = MockMvcBuilders.standaloneSetup(departmentController).build();

        mockMvc.perform(delete("/api/departments/v1/{id}", id))
                .andExpect(status().isNoContent());
    }

}