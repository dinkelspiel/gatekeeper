package dev.keii.keiicore.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ApiLogin {
    @JsonProperty("status")
    private int status;

    @JsonProperty("message")
    private String message;

    @JsonProperty("nickname")
    private String nickname;

    @JsonProperty("session")
    private String session;

    @JsonCreator
    public ApiLogin(@JsonProperty("status") int status, @JsonProperty("message") String message, @JsonProperty("nickname") String nickname, @JsonProperty("session") String session)
    {
        this.nickname = nickname;
        this.status = status;
        this.message = message;
        this.session = session;
    }

    public int getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public String getNickname() {
        return nickname;
    }

    public String getSession() {
        return session;
    }
}