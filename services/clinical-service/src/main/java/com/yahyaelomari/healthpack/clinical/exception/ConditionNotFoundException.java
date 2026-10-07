package com.yahyaelomari.healthpack.clinical.exception;

import java.util.UUID;

public class ConditionNotFoundException extends RuntimeException {
    public ConditionNotFoundException(UUID uuid) {
        super("No condition found with id: " + uuid);
    }
}
