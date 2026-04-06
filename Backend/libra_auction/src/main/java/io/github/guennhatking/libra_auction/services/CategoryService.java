package io.github.guennhatking.libra_auction.services;

import io.github.guennhatking.libra_auction.repositories.DanhMucRepository;
import io.github.guennhatking.libra_auction.viewmodels.response.CategoryResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoryService {
    private final DanhMucRepository danhMucRepository;

    public CategoryService(DanhMucRepository danhMucRepository) {
        this.danhMucRepository = danhMucRepository;
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getCategories() {
        return danhMucRepository.findAll().stream()
            .map(category -> new CategoryResponse(
                category.getId(),
                category.getHinhAnh(),
                category.getTenDanhMuc(),
                "/auctions/" + category.getId()
            ))
            .toList();
    }
}
