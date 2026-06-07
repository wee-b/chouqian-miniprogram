package com.quickstart.common.domain;

import lombok.Data;

import java.util.List;

@Data
public class PageResult<T> {

    private Integer curPage;

    private Long Total;

    private List<T> data;
}
