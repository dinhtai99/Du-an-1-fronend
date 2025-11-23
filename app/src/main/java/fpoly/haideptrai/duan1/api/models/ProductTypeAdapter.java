package fpoly.haideptrai.duan1.api.models;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

public class ProductTypeAdapter extends TypeAdapter<ReviewResponse.ProductInfo> {
    @Override
    public void write(JsonWriter out, ReviewResponse.ProductInfo value) throws IOException {
        if (value == null) {
            out.nullValue();
            return;
        }
        out.beginObject();
        if (value.get_id() != null) {
            out.name("_id").value(value.get_id());
        }
        if (value.getName() != null) {
            out.name("name").value(value.getName());
        }
        if (value.getImage() != null) {
            out.name("image").value(value.getImage());
        }
        if (value.getPrice() != null) {
            out.name("price").value(value.getPrice());
        }
        out.endObject();
    }

    @Override
    public ReviewResponse.ProductInfo read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
        }

        // Nếu là string (product ID), tạo ProductInfo với chỉ _id
        if (in.peek() == JsonToken.STRING) {
            String productId = in.nextString();
            ReviewResponse.ProductInfo productInfo = new ReviewResponse.ProductInfo();
            productInfo.set_id(productId);
            return productInfo;
        }

        // Nếu là object, parse bình thường
        if (in.peek() == JsonToken.BEGIN_OBJECT) {
            ReviewResponse.ProductInfo productInfo = new ReviewResponse.ProductInfo();
            in.beginObject();
            while (in.hasNext()) {
                String name = in.nextName();
                switch (name) {
                    case "_id":
                        productInfo.set_id(in.nextString());
                        break;
                    case "name":
                        productInfo.setName(in.nextString());
                        break;
                    case "image":
                        productInfo.setImage(in.nextString());
                        break;
                    case "price":
                        if (in.peek() == JsonToken.NULL) {
                            in.nextNull();
                        } else {
                            productInfo.setPrice(in.nextDouble());
                        }
                        break;
                    default:
                        in.skipValue();
                        break;
                }
            }
            in.endObject();
            return productInfo;
        }

        // Nếu không phải string hoặc object, skip
        in.skipValue();
        return null;
    }
}