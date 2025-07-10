package com.example.web;

import java.util.List;

public record PaginatedResult<T>(List<T> data, long total) {
}
