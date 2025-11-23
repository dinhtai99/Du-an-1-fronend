package fpoly.haideptrai.duan1.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

import fpoly.haideptrai.duan1.api.ApiClient;
import fpoly.haideptrai.duan1.api.models.ApiResponse;
import fpoly.haideptrai.duan1.api.services.FavoriteService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FavoriteManager {
    private static final String PREF_NAME = "favorite_prefs";
    private static final String KEY_FAVORITES = "favorite_products";

    private SharedPreferences sharedPreferences;
    private Set<String> favorites; // Local cache
    private FavoriteService favoriteService;
    private static FavoriteManager instance;
    private Context context;

    private FavoriteManager(Context context) {
        this.context = context.getApplicationContext();
        sharedPreferences = this.context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        favoriteService = ApiClient.getClient().create(FavoriteService.class);
        loadFavorites();
    }

    public static synchronized FavoriteManager getInstance(Context context) {
        if (instance == null) {
            instance = new FavoriteManager(context);
        }
        return instance;
    }

    private void loadFavorites() {
        // Load from local storage first (for offline support)
        String favoritesJson = sharedPreferences.getString(KEY_FAVORITES, null);
        if (favoritesJson != null && !favoritesJson.isEmpty()) {
            try {
                Gson gson = new Gson();
                Type type = new TypeToken<Set<String>>(){}.getType();
                favorites = gson.fromJson(favoritesJson, type);
                if (favorites == null) {
                    favorites = new HashSet<>();
                }
            } catch (Exception e) {
                Log.e("FavoriteManager", "Error loading favorites", e);
                favorites = new HashSet<>();
            }
        } else {
            favorites = new HashSet<>();
        }
    }

    private void saveFavorites() {
        // Save to local storage
        Gson gson = new Gson();
        String favoritesJson = gson.toJson(favorites);
        sharedPreferences.edit().putString(KEY_FAVORITES, favoritesJson).apply();
    }

    /**
     * Check if product is favorite (from local cache)
     */
    public boolean isFavorite(String productId) {
        return favorites.contains(productId);
    }

    /**
     * Add favorite - Optimistic update + API call
     */
    public void addFavorite(String productId, OnFavoriteCallback callback) {
        if (productId == null || productId.isEmpty()) {
            if (callback != null) callback.onError("Product ID không hợp lệ");
            return;
        }

        // Optimistic update: add to local cache immediately
        favorites.add(productId);
        saveFavorites();
        Log.d("FavoriteManager", "Added favorite locally: " + productId);

        // Call API
        Call<FavoriteService.FavoriteResponse> call = favoriteService.addFavorite(productId);
        call.enqueue(new Callback<FavoriteService.FavoriteResponse>() {
            @Override
            public void onResponse(Call<FavoriteService.FavoriteResponse> call, Response<FavoriteService.FavoriteResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d("FavoriteManager", "Added favorite to server: " + productId);
                    if (callback != null) callback.onSuccess(true);
                } else {
                    // API failed, but keep local change
                    Log.w("FavoriteManager", "Failed to add favorite to server, keeping local change");
                    if (callback != null) callback.onError("Không thể đồng bộ với server");
                }
            }

            @Override
            public void onFailure(Call<FavoriteService.FavoriteResponse> call, Throwable t) {
                // API failed, but keep local change
                Log.w("FavoriteManager", "Error adding favorite to server: " + t.getMessage());
                if (callback != null) callback.onError("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    /**
     * Remove favorite - Optimistic update + API call
     */
    public void removeFavorite(String productId, OnFavoriteCallback callback) {
        if (productId == null || productId.isEmpty()) {
            if (callback != null) callback.onError("Product ID không hợp lệ");
            return;
        }

        // Optimistic update: remove from local cache immediately
        favorites.remove(productId);
        saveFavorites();
        Log.d("FavoriteManager", "Removed favorite locally: " + productId);

        // Call API
        Call<FavoriteService.FavoriteDeleteResponse> call = favoriteService.removeFavorite(productId);
        call.enqueue(new Callback<FavoriteService.FavoriteDeleteResponse>() {
            @Override
            public void onResponse(Call<FavoriteService.FavoriteDeleteResponse> call, Response<FavoriteService.FavoriteDeleteResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d("FavoriteManager", "Removed favorite from server: " + productId + ", Message: " + response.body().getMessage());
                    if (callback != null) callback.onSuccess(false);
                } else {
                    // API failed, but keep local change
                    Log.w("FavoriteManager", "Failed to remove favorite from server. Code: " + response.code() + ", Body is null: " + (response.body() == null));
                    if (callback != null) callback.onError("Không thể đồng bộ với server");
                }
            }

            @Override
            public void onFailure(Call<FavoriteService.FavoriteDeleteResponse> call, Throwable t) {
                // API failed, but keep local change
                Log.w("FavoriteManager", "Error removing favorite from server: " + t.getMessage());
                if (callback != null) callback.onError("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    /**
     * Toggle favorite - Optimistic update + API call
     */
    public void toggleFavorite(String productId, OnFavoriteCallback callback) {
        boolean isCurrentlyFavorite = isFavorite(productId);
        if (isCurrentlyFavorite) {
            removeFavorite(productId, callback);
        } else {
            addFavorite(productId, callback);
        }
    }

    /**
     * Sync favorites from server (load all favorites from API)
     */
    public void syncFromServer(OnSyncCallback callback) {
        Call<java.util.List<fpoly.haideptrai.duan1.api.models.ProductResponse>> call = favoriteService.getFavorites();
        call.enqueue(new Callback<java.util.List<fpoly.haideptrai.duan1.api.models.ProductResponse>>() {
            @Override
            public void onResponse(Call<java.util.List<fpoly.haideptrai.duan1.api.models.ProductResponse>> call,
                                   Response<java.util.List<fpoly.haideptrai.duan1.api.models.ProductResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Update local cache with server data
                    favorites.clear();
                    for (fpoly.haideptrai.duan1.api.models.ProductResponse product : response.body()) {
                        if (product.get_id() != null) {
                            favorites.add(product.get_id());
                        }
                    }
                    saveFavorites();
                    Log.d("FavoriteManager", "Synced " + favorites.size() + " favorites from server");
                    if (callback != null) callback.onSuccess(favorites);
                } else {
                    Log.w("FavoriteManager", "Failed to sync favorites from server. Code: " + response.code());
                    if (callback != null) callback.onError("Không thể tải danh sách yêu thích");
                }
            }

            @Override
            public void onFailure(Call<java.util.List<fpoly.haideptrai.duan1.api.models.ProductResponse>> call, Throwable t) {
                Log.e("FavoriteManager", "Error syncing favorites from server: " + t.getMessage());
                if (callback != null) callback.onError("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    public Set<String> getAllFavorites() {
        return new HashSet<>(favorites);
    }

    public int getFavoriteCount() {
        return favorites.size();
    }

    public void clearAll() {
        favorites.clear();
        saveFavorites();
    }

    // Callback interfaces
    public interface OnFavoriteCallback {
        void onSuccess(boolean isFavorite);
        void onError(String error);
    }

    public interface OnSyncCallback {
        void onSuccess(Set<String> favorites);
        void onError(String error);
    }
}

