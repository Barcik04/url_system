import { useEffect, useRef, useState } from "react";
import { apiFetch } from "../api";
import "../css/ChatWidget.css";

type SenderType = "USER" | "ASSISTANT";

type ChatMessage = {
  messageId: number;
  conversationId: number;
  message: string;
  senderType: SenderType;
};

type OpenConversationResponse =
  | {
      conversationId: number;
    }
  | ChatMessage[];

const CHAT_STORAGE_KEY = "chatConversationId";
const CHAT_LAST_ACTIVITY_KEY = "chatLastActivity";
const CHAT_EXPIRATION_MS = 30 * 60 * 1000;

function saveChatActivity(conversationId: number) {
  localStorage.setItem(CHAT_STORAGE_KEY, String(conversationId));
  localStorage.setItem(CHAT_LAST_ACTIVITY_KEY, String(Date.now()));
}

function clearStoredConversation() {
  localStorage.removeItem(CHAT_STORAGE_KEY);
  localStorage.removeItem(CHAT_LAST_ACTIVITY_KEY);
}

function ChatWidget() {
  const [isOpen, setIsOpen] = useState(false);
  const [isRendered, setIsRendered] = useState(false);
  const [isVisible, setIsVisible] = useState(false);
  const [conversationId, setConversationId] = useState<number | null>(null);
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [input, setInput] = useState("");
  const [loadingConversation, setLoadingConversation] = useState(false);
  const [sending, setSending] = useState(false);
  const [error, setError] = useState("");

  const messagesEndRef = useRef<HTMLDivElement | null>(null);

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [messages, isOpen]);

  useEffect(() => {
    const savedConversationId = localStorage.getItem(CHAT_STORAGE_KEY);
    const savedLastActivity = localStorage.getItem(CHAT_LAST_ACTIVITY_KEY);

    if (!savedConversationId || !savedLastActivity) {
      clearStoredConversation();
      return;
    }

    const parsedConversationId = Number(savedConversationId);
    const parsedLastActivity = Number(savedLastActivity);

    if (
      Number.isNaN(parsedConversationId) ||
      Number.isNaN(parsedLastActivity)
    ) {
      clearStoredConversation();
      return;
    }

    const isExpired = Date.now() - parsedLastActivity > CHAT_EXPIRATION_MS;

    if (isExpired) {
      clearStoredConversation();
      setConversationId(null);
      setMessages([]);
      return;
    }

    setConversationId(parsedConversationId);
  }, []);

  useEffect(() => {
    if (isOpen) {
      setIsRendered(true);

      const frame = requestAnimationFrame(() => {
        setIsVisible(true);
      });

      return () => cancelAnimationFrame(frame);
    }

    setIsVisible(false);

    const timeout = setTimeout(() => {
      setIsRendered(false);
    }, 260);

    return () => clearTimeout(timeout);
  }, [isOpen]);

  async function ensureConversation(): Promise<number | null> {
    if (conversationId) {
      saveChatActivity(conversationId);
      return conversationId;
    }

    try {
      setLoadingConversation(true);
      setError("");

      const res = await apiFetch("/api/v1/conversations/open", {
        method: "POST",
      });

      if (!res.ok) {
        setError("Could not open chat conversation.");
        return null;
      }

      const data: OpenConversationResponse = await res.json();

      if (Array.isArray(data)) {
        if (data.length === 0) {
          setError("Conversation opened but no conversation ID was returned.");
          return null;
        }

        const id = data[0].conversationId;
        setConversationId(id);
        saveChatActivity(id);
        setMessages(data);
        return id;
      }

      const id = data.conversationId;
      setConversationId(id);
      saveChatActivity(id);
      return id;
    } catch {
      setError("Network error while opening the chat.");
      return null;
    } finally {
      setLoadingConversation(false);
    }
  }

  async function loadMessages(id: number) {
    try {
      setLoadingConversation(true);
      setError("");

      const res = await apiFetch(`/api/v1/conversations/${id}`, {
        method: "GET",
      });

      if (!res.ok) {
        if (res.status === 404) {
          clearStoredConversation();
          setConversationId(null);
          setMessages([]);
          setError("Saved conversation no longer exists. Open the chat again.");
          return;
        }

        setError("Could not load messages.");
        return;
      }

      const data: ChatMessage[] = await res.json();
      setMessages(data);
      saveChatActivity(id);
    } catch {
      setError("Network error while loading messages.");
    } finally {
      setLoadingConversation(false);
    }
  }

  async function handleOpenChat() {
    if (!isOpen) {
      setIsOpen(true);

      let id = conversationId;

      if (!id) {
        id = await ensureConversation();
      }

      if (id) {
        await loadMessages(id);
      }
    } else {
      setIsOpen(false);
    }
  }

  async function handleSendMessage() {
    const trimmed = input.trim();
    if (!trimmed || sending) return;

    let id = conversationId;

    if (!id) {
      id = await ensureConversation();
      if (!id) return;
    }

    saveChatActivity(id);

    const optimisticUserMessage: ChatMessage = {
      messageId: Date.now(),
      conversationId: id,
      message: trimmed,
      senderType: "USER",
    };

    setMessages((prev) => [...prev, optimisticUserMessage]);
    setInput("");
    setSending(true);
    setError("");

    try {
      const res = await apiFetch(`/api/v1/conversations/${id}`, {
        method: "POST",
        body: JSON.stringify(trimmed),
      });

      if (!res.ok) {
        setMessages((prev) =>
          prev.filter((m) => m.messageId !== optimisticUserMessage.messageId)
        );
        setError("Could not send message.");
        return;
      }

      const data = await res.json();

      const assistantText =
        data.assistantMessage ??
        data.message ??
        "No assistant response returned.";

      saveChatActivity(id);
      await loadMessages(id);

      if (!assistantText) {
        setMessages((prev) => [
          ...prev,
          {
            messageId: Date.now() + 1,
            conversationId: id!,
            message: "No assistant response returned.",
            senderType: "ASSISTANT",
          },
        ]);
      }
    } catch {
      setMessages((prev) =>
        prev.filter((m) => m.messageId !== optimisticUserMessage.messageId)
      );
      setError("Network error while sending message.");
    } finally {
      setSending(false);
    }
  }

  function handleKeyDown(e: React.KeyboardEvent<HTMLTextAreaElement>) {
    if (e.key === "Enter" && !e.shiftKey) {
      e.preventDefault();
      void handleSendMessage();
    }
  }

  return (
    <>
      <button
        className={`chat-launcher ${isOpen ? "chat-launcher-open" : ""}`}
        onClick={handleOpenChat}
      >
        {isOpen ? "Close chat" : "AI chat"}
      </button>

      {isRendered && (
        <div
          className={`chat-widget ${
            isVisible ? "chat-widget-open" : "chat-widget-closed"
          }`}
        >
          <div className="chat-header">
            <div>
              <div className="chat-title">AI Assistant</div>
              <div className="chat-subtitle">Helps users navigate your app</div>
            </div>
          </div>

          <div className="chat-messages">
            {loadingConversation ? (
              <div className="chat-system-message">Loading conversation...</div>
            ) : messages.length === 0 ? (
              <div className="chat-system-message">
                Start the conversation. Ask anything about the app.
              </div>
            ) : (
              messages.map((msg) => (
                <div
                  key={`${msg.messageId}-${msg.senderType}`}
                  className={`chat-message-row ${
                    msg.senderType === "USER"
                      ? "chat-message-row-user"
                      : "chat-message-row-assistant"
                  }`}
                >
                  <div
                    className={`chat-bubble ${
                      msg.senderType === "USER"
                        ? "chat-bubble-user"
                        : "chat-bubble-assistant"
                    }`}
                  >
                    {msg.message}
                  </div>
                </div>
              ))
            )}

            {sending && (
              <div className="chat-message-row chat-message-row-assistant">
                <div className="chat-bubble chat-bubble-assistant chat-bubble-thinking">
                  Thinking...
                </div>
              </div>
            )}

            <div ref={messagesEndRef} />
          </div>

          {error && <div className="chat-error">{error}</div>}

          <div className="chat-input-wrap">
            <textarea
              value={input}
              onChange={(e) => setInput(e.target.value)}
              onKeyDown={handleKeyDown}
              placeholder="Write a message..."
              rows={2}
              className="chat-textarea"
              disabled={sending || loadingConversation}
            />
            <button
              onClick={() => void handleSendMessage()}
              disabled={!input.trim() || sending || loadingConversation}
              className="chat-send-button"
            >
              Send
            </button>
          </div>
        </div>
      )}
    </>
  );
}

export default ChatWidget;