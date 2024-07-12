package com.example;

import java.util.UUID;

public record PostSummary(UUID id, String title, Integer countOfComments) {
}
