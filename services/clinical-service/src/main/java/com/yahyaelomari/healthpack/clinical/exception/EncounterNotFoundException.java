package com.yahyaelomari.healthpack.clinical.exception;

import java.util.UUID;

public class EncounterNotFoundException extends RuntimeException {
    public EncounterNotFoundException(UUID uuid) {
        super("No  encounter found with id: " + uuid);
    }
}
