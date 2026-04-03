package io.github.guennhatking.libra_auction.viewmodels.common;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(String message, T result) {
}
