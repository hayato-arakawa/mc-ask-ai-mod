package com.example.askaimod.api;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ChatCompletionResponse {
    private List<Choice> choices;

    public List<Choice> getChoices() {
        return choices;
    }

    public String getFirstChoiceContent() {
        if (choices == null || choices.isEmpty()) {
            return null;
        }
        ChoiceMessage message = choices.get(0).getMessage();
        return message != null ? message.getContent() : null;
    }

    public static class Choice {
        private ChoiceMessage message;

        public ChoiceMessage getMessage() {
            return message;
        }
    }

    public static class ChoiceMessage {
        private String role;
        private String content;

        public String getRole() {
            return role;
        }

        public String getContent() {
            return content;
        }
    }
}
