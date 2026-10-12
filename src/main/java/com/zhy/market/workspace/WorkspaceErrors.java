package com.zhy.market.workspace;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

final class WorkspaceErrors {
    private WorkspaceErrors() {}

    static ResponseStatusException bad(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }

    static ResponseStatusException conflict(String message) {
        return new ResponseStatusException(HttpStatus.CONFLICT, message);
    }
}
