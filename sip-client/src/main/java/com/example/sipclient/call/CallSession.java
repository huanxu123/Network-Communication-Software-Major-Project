package com.example.sipclient.call;

import com.example.sipclient.media.MediaSession;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents一次点对点呼叫会话的简化状态机。
 */
public final class CallSession {

    public enum State {
        IDLE,
        RINGING,
        ACTIVE,
        TERMINATED
    }

    private final String id;
    private final String remoteUri;
    private final Instant createdAt;
    private final boolean incoming;
    private State state;
    private MediaSession mediaSession;

    public CallSession(String remoteUri, boolean incoming) {
        this.id = UUID.randomUUID().toString();
        this.remoteUri = Objects.requireNonNull(remoteUri, "remoteUri");
        this.createdAt = Instant.now();
        this.incoming = incoming;
        this.state = State.IDLE;
    }

    public String getId() {
        return id;
    }

    public String getRemoteUri() {
        return remoteUri;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public State getState() {
        return state;
    }

    public boolean isIncoming() {
        return incoming;
    }

    public void markRinging() {
        this.state = State.RINGING;
    }

    public void markActive() {
        this.state = State.ACTIVE;
    }

    public void terminate() {
        this.state = State.TERMINATED;
        stopMedia();
    }

    public void startMedia(MediaSession session) {
        if (this.mediaSession != null) {
            return;
        }
        this.mediaSession = session;
        if (mediaSession != null) {
            mediaSession.start();
        }
    }

    public void stopMedia() {
        if (mediaSession != null) {
            mediaSession.stop();
            mediaSession = null;
        }
    }
}
