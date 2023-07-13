package com.yangfan.core.api.services.user.model;

import lombok.Value;

@Value(staticConstructor = "of")
public class UserResponse {
    String message;
}
