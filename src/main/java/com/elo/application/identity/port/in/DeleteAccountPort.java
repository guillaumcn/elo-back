package com.elo.application.identity.port.in;

import java.util.UUID;

public interface DeleteAccountPort {

    void execute(UUID userId);
}
