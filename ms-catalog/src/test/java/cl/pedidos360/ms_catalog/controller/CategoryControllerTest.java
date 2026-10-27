
package cl.pedidos360.ms_catalog.controller;

import cl.pedidos360.ms_catalog.dto.request.CategoryCreateRequest;
import cl.pedidos360.ms_catalog.dto.request.CategoryStatusUpdateRequest;
import cl.pedidos360.ms_catalog.dto.request.CategoryUpdateRequest;
import cl.pedidos360.ms_catalog.dto.response.CategoryResponse;
import cl.pedidos360.ms_catalog.service.CategoryService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CategoryControllerTest {

    private static final String BASE_URL = "/api/v1/categories";

    private static final UUID CATEGORY_ID =
            UUID.fromString("a1234567-1234-4234-8234-123456789abc");

    private static final Instant CREATED_AT =
            Instant.parse("2026-01-01T10:00:00Z");

    private static final Instant UPDATED_AT =
            Instant.parse("2026-01-02T10:00:00Z");

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private CategoryController categoryController;

    private MockMvc mockMvc;
    private LocalValidatorFactoryBean validator;

    @BeforeEach
    void setUp() {
        validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders
                .standaloneSetup(categoryController)
                .setValidator(validator)
                .setCustomArgumentResolvers(
                        new PageableHandlerMethodArgumentResolver()
                )
                .build();
    }

    @AfterAll
    static void closeValidationFactory() {
        // La fábrica se libera en cada instancia de prueba
        // mediante el ciclo de vida de JUnit.
    }

    private CategoryResponse createCategoryResponse() {
        return new CategoryResponse(
                CATEGORY_ID,
                "Electrónica",
                "Productos electrónicos",
                true,
                CREATED_AT,
                UPDATED_AT
        );
    }

    @Test
    void createShouldReturnCreatedWhenRequestIsValid() throws Exception {
        CategoryResponse response = createCategoryResponse();

        when(categoryService.create(any(CategoryCreateRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Electrónica",
                                  "description": "Productos electrónicos"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.message")
                        .value("Categoría creada con éxito."))
                .andExpect(jsonPath("$.data.id").value(CATEGORY_ID.toString()))
                .andExpect(jsonPath("$.data.name").value("Electrónica"))
                .andExpect(jsonPath("$.data.active").value(true));

        verify(categoryService).create(any(CategoryCreateRequest.class));
    }

    @Test
    void createShouldReturnBadRequestWhenNameIsBlank() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": " ",
                                  "description": "Descripción"
                                }
                                """))
                .andExpect(status().isBadRequest());

        verify(categoryService, never())
                .create(any(CategoryCreateRequest.class));
    }

    @Test
    void findAllShouldReturnPagedCategories() throws Exception {
        CategoryResponse response = createCategoryResponse();

        Page<CategoryResponse> page = new PageImpl<>(
                List.of(response),
                PageRequest.of(0, 20),
                1
        );

        when(categoryService.findAll(any()))
                .thenReturn(page);

        mockMvc.perform(get(BASE_URL)
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message")
                        .value("Categorías recuperadas con éxito."))
                .andExpect(jsonPath("$.data.content[0].id")
                        .value(CATEGORY_ID.toString()))
                .andExpect(jsonPath("$.data.content[0].name")
                        .value("Electrónica"));

        verify(categoryService).findAll(any());
    }

    @Test
    void findByIdShouldReturnCategory() throws Exception {
        when(categoryService.findById(CATEGORY_ID))
                .thenReturn(createCategoryResponse());

        mockMvc.perform(get(BASE_URL + "/{categoryId}", CATEGORY_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message")
                        .value("Categoría recuperada con éxito."))
                .andExpect(jsonPath("$.data.id").value(CATEGORY_ID.toString()))
                .andExpect(jsonPath("$.data.name").value("Electrónica"));

        verify(categoryService).findById(CATEGORY_ID);
    }

    @Test
    void updateShouldReturnUpdatedCategory() throws Exception {
        when(categoryService.update(
                eq(CATEGORY_ID),
                any(CategoryUpdateRequest.class)
        )).thenReturn(createCategoryResponse());

        mockMvc.perform(put(BASE_URL + "/{categoryId}", CATEGORY_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Electrónica",
                                  "description": "Descripción actualizada"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message")
                        .value("Categoría actualizada con éxito."))
                .andExpect(jsonPath("$.data.id").value(CATEGORY_ID.toString()));

        verify(categoryService).update(
                eq(CATEGORY_ID),
                any(CategoryUpdateRequest.class)
        );
    }

    @Test
    void updateShouldReturnBadRequestWhenNameIsBlank() throws Exception {
        mockMvc.perform(put(BASE_URL + "/{categoryId}", CATEGORY_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "",
                                  "description": "Descripción"
                                }
                                """))
                .andExpect(status().isBadRequest());

        verify(categoryService, never()).update(
                eq(CATEGORY_ID),
                any(CategoryUpdateRequest.class)
        );
    }

    @Test
    void updateStatusShouldReturnUpdatedCategory() throws Exception {
        when(categoryService.updateStatus(
                eq(CATEGORY_ID),
                any(CategoryStatusUpdateRequest.class)
        )).thenReturn(createCategoryResponse());

        mockMvc.perform(patch(BASE_URL + "/{categoryId}", CATEGORY_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "active": false
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message")
                        .value("El estado de la categoría se ha actualizado correctamente."))
                .andExpect(jsonPath("$.data.id").value(CATEGORY_ID.toString()));

        verify(categoryService).updateStatus(
                eq(CATEGORY_ID),
                any(CategoryStatusUpdateRequest.class)
        );
    }

    @Test
    void updateStatusShouldReturnBadRequestWhenActiveIsNull() throws Exception {
        mockMvc.perform(patch(BASE_URL + "/{categoryId}", CATEGORY_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "active": null
                                }
                                """))
                .andExpect(status().isBadRequest());

        verify(categoryService, never()).updateStatus(
                eq(CATEGORY_ID),
                any(CategoryStatusUpdateRequest.class)
        );
    }

    @Test
    void deleteShouldReturnOkWhenCategoryIsDeleted() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/{categoryId}", CATEGORY_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message")
                        .value("La categoría se ha eliminado correctamente."))
                .andExpect(jsonPath("$.data").doesNotExist());

        verify(categoryService).delete(CATEGORY_ID);
    }
}
