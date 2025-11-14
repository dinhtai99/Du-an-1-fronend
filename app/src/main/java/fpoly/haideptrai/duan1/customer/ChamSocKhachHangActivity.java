package fpoly.haideptrai.duan1.customer;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageButton;
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
import fpoly.haideptrai.duan1.customer.adapters.MessageAdapter;
import fpoly.haideptrai.duan1.customer.models.Message;

public class ChamSocKhachHangActivity extends AppCompatActivity {

    private ImageButton btnBack, btnPhone, btnMenu, btnAttach, btnSend;
    private RecyclerView rvMessages;
    private TextInputEditText edtMessage;
    
    private MessageAdapter messageAdapter;
    private List<Message> messages = new ArrayList<>();
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cham_soc_khach_hang);

        initViews();
        setupRecyclerView();
        loadInitialMessages();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnPhone = findViewById(R.id.btnPhone);
        btnMenu = findViewById(R.id.btnMenu);
        btnAttach = findViewById(R.id.btnAttach);
        btnSend = findViewById(R.id.btnSend);
        rvMessages = findViewById(R.id.rvMessages);
        edtMessage = findViewById(R.id.edtMessage);

        btnBack.setOnClickListener(v -> finish());
        btnPhone.setOnClickListener(v -> {
            // TODO: Make phone call
            Toast.makeText(this, "Gọi điện", Toast.LENGTH_SHORT).show();
        });
        btnMenu.setOnClickListener(v -> {
            // TODO: Show menu
            Toast.makeText(this, "Menu", Toast.LENGTH_SHORT).show();
        });
        btnAttach.setOnClickListener(v -> {
            // TODO: Attach file/image
            Toast.makeText(this, "Đính kèm", Toast.LENGTH_SHORT).show();
        });
        btnSend.setOnClickListener(v -> sendMessage());
    }

    private void setupRecyclerView() {
        messageAdapter = new MessageAdapter(messages);
        rvMessages.setLayoutManager(new LinearLayoutManager(this));
        rvMessages.setAdapter(messageAdapter);
    }

    private void loadInitialMessages() {
        // Add welcome message
        messages.add(new Message("Đây là hệ thống chăm sóc khách hàng của cửa hàng. Tôi có thể giúp gì cho bạn hôm nay ?", "10:10", false));
        messageAdapter.notifyDataSetChanged();
        scrollToBottom();
    }

    private void sendMessage() {
        String messageText = edtMessage.getText().toString().trim();
        if (TextUtils.isEmpty(messageText)) {
            return;
        }

        // Add user message
        String currentTime = timeFormat.format(new Date());
        messages.add(new Message(messageText, currentTime, true));
        messageAdapter.notifyDataSetChanged();
        scrollToBottom();
        edtMessage.setText("");

        // Simulate system response
        new android.os.Handler().postDelayed(() -> {
            messages.add(new Message("Hệ thống đang xử lý yêu cầu của bạn", timeFormat.format(new Date()), false));
            messageAdapter.notifyDataSetChanged();
            scrollToBottom();
        }, 1000);
    }

    private void scrollToBottom() {
        if (messages.size() > 0) {
            rvMessages.smoothScrollToPosition(messages.size() - 1);
        }
    }
}

