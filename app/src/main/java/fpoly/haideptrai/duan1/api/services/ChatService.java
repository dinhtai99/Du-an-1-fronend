package fpoly.haideptrai.duan1.api.services;

import java.util.List;

import fpoly.haideptrai.duan1.api.models.ApiResponse;
import fpoly.haideptrai.duan1.api.models.ChatMessageRequest;
import fpoly.haideptrai.duan1.api.models.ChatMessageResponse;
import fpoly.haideptrai.duan1.api.models.ChatMessagesResponse;
import fpoly.haideptrai.duan1.api.models.ChatSendResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ChatService {
    @GET("api/chat/messages")
    Call<ChatMessagesResponse> getMessages(
            @Query("limit") Integer limit
    );

    @POST("api/chat/messages")
    Call<ChatSendResponse> sendMessage(@Body ChatMessageRequest request);

    @GET("api/chat/messages/unread")
    Call<ApiResponse<Integer>> getUnreadCount();
}

