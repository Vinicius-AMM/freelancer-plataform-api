package com.manager.freelancer_management_api.utils.common;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Component
public class PaginationHelper {
    public static final int DEFAULT_PAGE_SIZE = 10;

    public Pageable preparePageRequest(Pageable pageable, String defaultSort){
        int pageSize = pageable.getPageSize() > 0 ? pageable.getPageSize() : DEFAULT_PAGE_SIZE;
        Sort sort = pageable.getSort().isSorted() ? pageable.getSort() : Sort.by(Sort.Direction.DESC, defaultSort);
        return PageRequest.of(pageable.getPageNumber(), pageSize, sort);
    }
}