package cl.eventpass.bff.controller;

import cl.eventpass.bff.controller.docs.CategoryControllerDocs;
import cl.eventpass.bff.dto.request.CategoryCreateRequest;
import cl.eventpass.bff.dto.request.CategoryStatusUpdateRequest;
import cl.eventpass.bff.dto.request.CategoryUpdateRequest;
import cl.eventpass.bff.dto.response.CategoryResponse;
import cl.eventpass.bff.dto.response.StandardResponse;

import java.util.Map;
import java.util.UUID;

public class CategoryController implements CategoryControllerDocs {

    @Override
    public StandardResponse<CategoryResponse> create(CategoryCreateRequest request) {
        return null;
    }

    @Override
    public StandardResponse<?> findAll(Map<String, String> queryParams) {
        return null;
    }

    @Override
    public StandardResponse<CategoryResponse> findById(UUID categoryId) {
        return null;
    }

    @Override
    public StandardResponse<CategoryResponse> update(UUID categoryId, CategoryUpdateRequest request) {
        return null;
    }

    @Override
    public StandardResponse<CategoryResponse> updateStatus(UUID categoryId, CategoryStatusUpdateRequest request) {
        return null;
    }

    @Override
    public void delete(UUID categoryId) {

    }
}
