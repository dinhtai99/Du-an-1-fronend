package fpoly.haideptrai.duan1.api.services;

import java.util.List;

import fpoly.haideptrai.duan1.api.models.ApiResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface StatisticsService {
    @GET("api/statistics/overview")
    Call<ApiResponse<Object>> getOverview(@Query("startDate") String startDate, @Query("endDate") String endDate);

    @GET("api/statistics/top-products/quantity")
    Call<List<Object>> getTopProductsByQuantity(@Query("limit") Integer limit, @Query("startDate") String startDate, @Query("endDate") String endDate);

    @GET("api/statistics/top-products/revenue")
    Call<List<Object>> getTopProductsByRevenue(@Query("limit") Integer limit, @Query("startDate") String startDate, @Query("endDate") String endDate);

    @GET("api/statistics/revenue/daily")
    Call<List<Object>> getDailyRevenue(@Query("startDate") String startDate, @Query("endDate") String endDate);

    @GET("api/statistics/revenue/monthly")
    Call<List<Object>> getMonthlyRevenue(@Query("year") Integer year);

    @GET("api/statistics/revenue/yearly")
    Call<List<Object>> getYearlyRevenue();

    @GET("api/statistics/low-stock")
    Call<List<Object>> getLowStockProducts();

    @GET("api/statistics/payment-methods")
    Call<ApiResponse<Object>> getPaymentMethodsStats(@Query("startDate") String startDate, @Query("endDate") String endDate);
}

