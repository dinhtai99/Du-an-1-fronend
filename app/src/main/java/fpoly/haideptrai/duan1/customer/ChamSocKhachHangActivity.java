package fpoly.haideptrai.duan1.customer;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import fpoly.haideptrai.duan1.R;
import fpoly.haideptrai.duan1.api.ApiClient;
import fpoly.haideptrai.duan1.api.models.ApiResponse;
import fpoly.haideptrai.duan1.api.models.ChatMessageRequest;
import fpoly.haideptrai.duan1.api.models.ChatMessageResponse;
import fpoly.haideptrai.duan1.api.models.ChatMessagesResponse;
import fpoly.haideptrai.duan1.api.models.ChatSendResponse;
import fpoly.haideptrai.duan1.api.services.ChatService;
import fpoly.haideptrai.duan1.customer.adapters.MessageAdapter;
import fpoly.haideptrai.duan1.customer.models.Message;
import fpoly.haideptrai.duan1.utils.SessionManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChamSocKhachHangActivity extends AppCompatActivity {

    private ImageButton btnBack, btnPhone, btnMenu, btnAttach, btnSend;
    private RecyclerView rvMessages;
    private TextInputEditText edtMessage;
    private ProgressBar progressBar;
    
    private MessageAdapter messageAdapter;
    private List<Message> messages = new ArrayList<>();
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private ChatService chatService;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cham_soc_khach_hang);

        initViews();
        setupRecyclerView();
        loadInitialMessages();
        
        // Auto-refresh messages mỗi 5 giây để nhận tin nhắn từ admin
        startAutoRefresh();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Refresh messages khi quay lại màn hình
        refreshMessages();
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        // Có thể tạm dừng auto-refresh khi không active
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopAutoRefresh();
    }
    
    private android.os.Handler refreshHandler;
    private Runnable refreshRunnable;
    
    /**
     * Bắt đầu auto-refresh để nhận tin nhắn từ admin
     */
    private void startAutoRefresh() {
        refreshHandler = new android.os.Handler();
        refreshRunnable = new Runnable() {
            @Override
            public void run() {
                refreshMessages();
                // Refresh lại sau 5 giây
                if (refreshHandler != null) {
                    refreshHandler.postDelayed(this, 5000);
                }
            }
        };
        // Bắt đầu refresh sau 5 giây
        refreshHandler.postDelayed(refreshRunnable, 5000);
    }
    
    /**
     * Dừng auto-refresh
     */
    private void stopAutoRefresh() {
        if (refreshHandler != null && refreshRunnable != null) {
            refreshHandler.removeCallbacks(refreshRunnable);
        }
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnPhone = findViewById(R.id.btnPhone);
        btnMenu = findViewById(R.id.btnMenu);
        btnAttach = findViewById(R.id.btnAttach);
        btnSend = findViewById(R.id.btnSend);
        rvMessages = findViewById(R.id.rvMessages);
        edtMessage = findViewById(R.id.edtMessage);
        progressBar = findViewById(R.id.progressBar);

        sessionManager = new SessionManager(this);
        chatService = ApiClient.getClient().create(ChatService.class);

        btnBack.setOnClickListener(v -> finish());
        btnPhone.setOnClickListener(v -> {
            // Gọi điện đến hotline
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:1900123456"));
            startActivity(intent);
        });
        btnMenu.setOnClickListener(v -> {
            // Hiển thị menu options
            Toast.makeText(this, "Menu tùy chọn", Toast.LENGTH_SHORT).show();
        });
        btnAttach.setOnClickListener(v -> {
            // Chức năng đính kèm file/image (có thể mở rộng sau)
            Toast.makeText(this, "Chức năng đính kèm đang được phát triển", Toast.LENGTH_SHORT).show();
        });
        btnSend.setOnClickListener(v -> sendMessage());
    }

    private void setupRecyclerView() {
        messageAdapter = new MessageAdapter(messages);
        rvMessages.setLayoutManager(new LinearLayoutManager(this));
        rvMessages.setAdapter(messageAdapter);
    }

    private void loadInitialMessages() {
        progressBar.setVisibility(View.VISIBLE);
        
        // Kiểm tra đăng nhập
        int userId = sessionManager.getUserId();
        if (userId == -1) {
            // Nếu chưa đăng nhập, dùng welcome message
            loadWelcomeMessage();
            return;
        }
        
        // Backend tự lấy userId từ JWT token, không cần gửi trong query
        Call<ChatMessagesResponse> call = chatService.getMessages(50);
        call.enqueue(new Callback<ChatMessagesResponse>() {
            @Override
            public void onResponse(Call<ChatMessagesResponse> call, Response<ChatMessagesResponse> response) {
                progressBar.setVisibility(View.GONE);
                
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<ChatMessageResponse> apiMessages = response.body().getMessages();
                    
                    if (apiMessages != null && !apiMessages.isEmpty()) {
                        messages.clear();
                        for (ChatMessageResponse msgResponse : apiMessages) {
                            Message message = convertToMessage(msgResponse);
                            messages.add(message);
                        }
                        messageAdapter.notifyDataSetChanged();
                        scrollToBottom();
                    } else {
                        // Nếu không có tin nhắn, hiển thị welcome message
                        if (messages.isEmpty()) {
                            loadWelcomeMessage();
                        }
                    }
                } else {
                    // Nếu API lỗi, dùng welcome message nếu chưa có tin nhắn nào
                    if (messages.isEmpty()) {
                        loadWelcomeMessage();
                    }
                }
            }

            @Override
            public void onFailure(Call<ChatMessagesResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                android.util.Log.e("Chat", "Error loading messages: " + t.getMessage(), t);
                // Nếu API không có hoặc lỗi, dùng welcome message nếu chưa có tin nhắn nào
                if (messages.isEmpty()) {
                    loadWelcomeMessage();
                }
            }
        });
    }
    
    private void loadWelcomeMessage() {
        messages.clear();
        messages.add(new Message(
            "Xin chào! Đây là hệ thống chăm sóc khách hàng. Bạn có thể gửi tin nhắn để được hỗ trợ. Chúng tôi sẽ phản hồi sớm nhất có thể!", 
            timeFormat.format(new Date()), 
            false));
        messageAdapter.notifyDataSetChanged();
        scrollToBottom();
    }
    
    private Message convertToMessage(ChatMessageResponse response) {
        String time = timeFormat.format(new Date());
        if (response.getCreatedAt() != null && !response.getCreatedAt().isEmpty()) {
            try {
                // Backend có thể trả về ISO format hoặc format khác
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
                Date date = inputFormat.parse(response.getCreatedAt());
                time = timeFormat.format(date);
            } catch (Exception e) {
                try {
                    // Thử format khác
                    SimpleDateFormat inputFormat2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                    Date date = inputFormat2.parse(response.getCreatedAt());
                    time = timeFormat.format(date);
                } catch (Exception e2) {
                    // Ignore, dùng thời gian hiện tại
                }
            }
        }
        
        // isUser = true nếu senderRole là "customer", false nếu là "admin"
        boolean isUser = "customer".equals(response.getSenderRole());
        return new Message(response.getMessage(), time, isUser);
    }

    private void sendMessage() {
        String messageText = edtMessage.getText().toString().trim();
        if (TextUtils.isEmpty(messageText)) {
            Toast.makeText(this, "Vui lòng nhập tin nhắn", Toast.LENGTH_SHORT).show();
            return;
        }

        // Kiểm tra đăng nhập
        int userId = sessionManager.getUserId();
        if (userId == -1) {
            Toast.makeText(this, "Vui lòng đăng nhập để gửi tin nhắn", Toast.LENGTH_SHORT).show();
            return;
        }

        // Disable send button while sending
        btnSend.setEnabled(false);
        edtMessage.setEnabled(false);

        // Add user message to UI immediately (optimistic update)
        String currentTime = timeFormat.format(new Date());
        Message userMessage = new Message(messageText, currentTime, true);
        messages.add(userMessage);
        messageAdapter.notifyDataSetChanged();
        scrollToBottom();
        edtMessage.setText("");

        // Send to API - gửi tin nhắn cho admin
        // Backend chỉ cần message, userId lấy từ JWT token
        ChatMessageRequest request = new ChatMessageRequest(messageText);
        android.util.Log.d("Chat", "Sending message: " + messageText);
        android.util.Log.d("Chat", "Request JSON: " + new com.google.gson.Gson().toJson(request));
        
        Call<ChatSendResponse> call = chatService.sendMessage(request);
        call.enqueue(new Callback<ChatSendResponse>() {
            @Override
            public void onResponse(Call<ChatSendResponse> call, Response<ChatSendResponse> response) {
                btnSend.setEnabled(true);
                edtMessage.setEnabled(true);
                
                android.util.Log.d("Chat", "Response code: " + response.code());
                android.util.Log.d("Chat", "Response successful: " + response.isSuccessful());
                
                if (response.isSuccessful() && response.body() != null) {
                    ChatSendResponse sendResponse = response.body();
                    android.util.Log.d("Chat", "Response body: " + sendResponse.toString());
                    
                    if (sendResponse.isSuccess()) {
                        // Message sent successfully to admin
                        android.util.Log.d("Chat", "Message sent successfully to admin");
                        
                        // Hiển thị thông báo xác nhận
                        String successMsg = sendResponse.getMessage() != null ? 
                            sendResponse.getMessage() : "Tin nhắn đã được gửi đến bộ phận chăm sóc khách hàng";
                        Toast.makeText(ChamSocKhachHangActivity.this, successMsg, Toast.LENGTH_SHORT).show();
                        
                        // Thêm tin nhắn xác nhận từ hệ thống
                        new android.os.Handler().postDelayed(() -> {
                            messages.add(new Message(
                                "Tin nhắn của bạn đã được gửi đến bộ phận chăm sóc khách hàng. Chúng tôi sẽ phản hồi sớm nhất có thể!", 
                                timeFormat.format(new Date()), 
                                false));
                            messageAdapter.notifyDataSetChanged();
                            scrollToBottom();
                        }, 500);
                        
                        // Reload messages sau 2 giây để nhận tin nhắn từ admin (nếu có)
                        new android.os.Handler().postDelayed(() -> {
                            refreshMessages();
                        }, 2000);
                    } else {
                        // Lỗi từ server
                        String errorMsg = sendResponse.getMessage() != null ? 
                            sendResponse.getMessage() : "Không thể gửi tin nhắn";
                        android.util.Log.e("Chat", "Server error: " + errorMsg);
                        Toast.makeText(ChamSocKhachHangActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                        
                        // Xóa tin nhắn khỏi UI nếu gửi thất bại
                        messages.remove(userMessage);
                        messageAdapter.notifyDataSetChanged();
                    }
                } else {
                    // Parse error response
                    String errorMsg = "Không thể gửi tin nhắn";
                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            android.util.Log.e("Chat", "Error response code: " + response.code());
                            android.util.Log.e("Chat", "Error response body: " + errorBody);
                            
                            // Thử parse JSON error
                            try {
                                com.google.gson.Gson gson = new com.google.gson.Gson();
                                ApiResponse<?> errorResponse = gson.fromJson(errorBody, ApiResponse.class);
                                if (errorResponse != null && errorResponse.getMessage() != null) {
                                    errorMsg = errorResponse.getMessage();
                                }
                            } catch (Exception jsonEx) {
                                // Không phải JSON, kiểm tra nếu là HTML (404, 500, etc)
                                if (errorBody.contains("Cannot POST") || errorBody.contains("<!DOCTYPE html>")) {
                                    errorMsg = "Endpoint không tồn tại. Vui lòng kiểm tra backend server!\n" +
                                              "Đảm bảo route /api/chat/messages đã được đăng ký trong server.js:\n" +
                                              "app.use('/api/chat', chatRoutes);";
                                } else if (errorBody.length() < 200) {
                                    errorMsg = errorBody;
                                }
                            }
                        }
                    } catch (Exception e) {
                        android.util.Log.e("Chat", "Error parsing error body", e);
                    }
                    
                    Toast.makeText(ChamSocKhachHangActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                    
                    // Xóa tin nhắn khỏi UI nếu gửi thất bại
                    messages.remove(userMessage);
                    messageAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<ChatSendResponse> call, Throwable t) {
                btnSend.setEnabled(true);
                edtMessage.setEnabled(true);
                
                android.util.Log.e("Chat", "Network error sending message: " + t.getMessage(), t);
                Toast.makeText(ChamSocKhachHangActivity.this, 
                    "Lỗi kết nối: " + t.getMessage() + "\nVui lòng kiểm tra kết nối mạng và thử lại.", 
                    Toast.LENGTH_LONG).show();
                
                // Xóa tin nhắn khỏi UI nếu gửi thất bại
                messages.remove(userMessage);
                messageAdapter.notifyDataSetChanged();
            }
        });
    }
    
    /**
     * Refresh messages để nhận tin nhắn mới từ admin
     */
    private void refreshMessages() {
        int userId = sessionManager.getUserId();
        if (userId == -1) {
            return;
        }
        
        // Backend tự lấy userId từ JWT token, không cần gửi trong query
        Call<ChatMessagesResponse> call = chatService.getMessages(50);
        call.enqueue(new Callback<ChatMessagesResponse>() {
            @Override
            public void onResponse(Call<ChatMessagesResponse> call, Response<ChatMessagesResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<ChatMessageResponse> apiMessages = response.body().getMessages();
                    
                    if (apiMessages != null && !apiMessages.isEmpty()) {
                        // Cập nhật danh sách tin nhắn
                        messages.clear();
                        for (ChatMessageResponse msgResponse : apiMessages) {
                            Message message = convertToMessage(msgResponse);
                            messages.add(message);
                        }
                        
                        messageAdapter.notifyDataSetChanged();
                        scrollToBottom();
                    }
                }
            }

            @Override
            public void onFailure(Call<ChatMessagesResponse> call, Throwable t) {
                // Silent fail - không làm gián đoạn user
                android.util.Log.e("Chat", "Error refreshing messages: " + t.getMessage());
            }
        });
    }

    private void scrollToBottom() {
        if (messages.size() > 0) {
            rvMessages.smoothScrollToPosition(messages.size() - 1);
        }
    }
}

