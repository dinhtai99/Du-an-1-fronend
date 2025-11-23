package fpoly.haideptrai.duan1.api.models;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

public class UserTypeAdapter extends TypeAdapter<ReviewResponse.UserInfo> {
    @Override
    public void write(JsonWriter out, ReviewResponse.UserInfo value) throws IOException {
        if (value == null) {
            out.nullValue();
            return;
        }
        out.beginObject();
        if (value.get_id() != null) {
            out.name("_id").value(value.get_id());
        }
        if (value.getFullName() != null) {
            out.name("fullName").value(value.getFullName());
        }
        if (value.getAvatar() != null) {
            out.name("avatar").value(value.getAvatar());
        }
        out.endObject();
    }

    @Override
    public ReviewResponse.UserInfo read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
        }

        // Nếu là string (user ID), tạo UserInfo với chỉ _id
        if (in.peek() == JsonToken.STRING) {
            String userId = in.nextString();
            ReviewResponse.UserInfo userInfo = new ReviewResponse.UserInfo();
            userInfo.set_id(userId);
            return userInfo;
        }

        // Nếu là object, parse bình thường
        if (in.peek() == JsonToken.BEGIN_OBJECT) {
            ReviewResponse.UserInfo userInfo = new ReviewResponse.UserInfo();
            in.beginObject();
            while (in.hasNext()) {
                String name = in.nextName();
                switch (name) {
                    case "_id":
                        userInfo.set_id(in.nextString());
                        break;
                    case "fullName":
                        userInfo.setFullName(in.nextString());
                        break;
                    case "avatar":
                        userInfo.setAvatar(in.nextString());
                        break;
                    default:
                        in.skipValue();
                        break;
                }
            }
            in.endObject();
            return userInfo;
        }

        // Nếu không phải string hoặc object, skip
        in.skipValue();
        return null;
    }
}