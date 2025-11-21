package fpoly.haideptrai.duan1.api.models;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

public class CategoryTypeAdapter extends TypeAdapter<CategoryResponse> {
    @Override
    public void write(JsonWriter out, CategoryResponse value) throws IOException {
        if (value == null) {
            out.nullValue();
            return;
        }
        out.beginObject();
        out.name("_id").value(value.get_id());
        out.name("name").value(value.getName());
        if (value.getDescription() != null) {
            out.name("description").value(value.getDescription());
        }
        if (value.getStatus() != null) {
            out.name("status").value(value.getStatus());
        }
        if (value.getImage() != null) {
            out.name("image").value(value.getImage());
        }
        out.endObject();
    }

    @Override
    public CategoryResponse read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
        }
        
        // Nếu là string (category ID), tạo CategoryResponse với chỉ _id
        if (in.peek() == JsonToken.STRING) {
            String categoryId = in.nextString();
            CategoryResponse category = new CategoryResponse();
            category.set_id(categoryId);
            return category;
        }
        
        // Nếu là object, parse bình thường
        if (in.peek() == JsonToken.BEGIN_OBJECT) {
            CategoryResponse category = new CategoryResponse();
            in.beginObject();
            while (in.hasNext()) {
                String name = in.nextName();
                switch (name) {
                    case "_id":
                        category.set_id(in.nextString());
                        break;
                    case "name":
                        category.setName(in.nextString());
                        break;
                    case "description":
                        category.setDescription(in.nextString());
                        break;
                    case "status":
                        if (in.peek() == JsonToken.NULL) {
                            in.nextNull();
                        } else {
                            category.setStatus(in.nextInt());
                        }
                        break;
                    case "image":
                        category.setImage(in.nextString());
                        break;
                    default:
                        in.skipValue();
                        break;
                }
            }
            in.endObject();
            return category;
        }
        
        // Nếu không phải string hoặc object, skip
        in.skipValue();
        return null;
    }
}

