package fpoly.haideptrai.duan1.api.services;

import java.util.List;

import fpoly.haideptrai.duan1.api.models.ApiResponse;
import fpoly.haideptrai.duan1.api.models.ProductResponse;
import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface FavoriteService {
    @GET("api/favorites")
    Call<List<ProductResponse>> getFavorites();

    @GET("api/favorites/check/{productId}")
    Call<FavoriteCheckResponse> checkFavorite(@Path("productId") String productId);

    @POST("api/favorites/{productId}")
    Call<FavoriteResponse> addFavorite(@Path("productId") String productId);

    @DELETE("api/favorites/{productId}")
    Call<FavoriteDeleteResponse> removeFavorite(@Path("productId") String productId);

    // Inner classes for response models
    class FavoriteCheckResponse {
        private boolean isFavorite;

        public boolean isFavorite() {
            return isFavorite;
        }

        public void setFavorite(boolean favorite) {
            isFavorite = favorite;
        }
    }

    class FavoriteResponse {
        private String message;
        private Object favorite; // Can be Favorite object from backend

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public Object getFavorite() {
            return favorite;
        }

        public void setFavorite(Object favorite) {
            this.favorite = favorite;
        }
    }

    class FavoriteDeleteResponse {
        private String message;

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}
