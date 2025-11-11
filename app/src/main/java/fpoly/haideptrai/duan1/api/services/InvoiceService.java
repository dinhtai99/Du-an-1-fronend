package fpoly.haideptrai.duan1.api.services;

import fpoly.haideptrai.duan1.api.models.ApiResponse;
import fpoly.haideptrai.duan1.api.models.InvoiceListResponse;
import fpoly.haideptrai.duan1.api.models.InvoiceRequest;
import fpoly.haideptrai.duan1.api.models.InvoiceResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface InvoiceService {
    @GET("api/invoices")
    Call<InvoiceListResponse> getInvoices(
            @Query("search") String search,
            @Query("customer") String customer,
            @Query("staff") String staff,
            @Query("status") String status,
            @Query("paymentMethod") String paymentMethod,
            @Query("startDate") String startDate,
            @Query("endDate") String endDate,
            @Query("page") Integer page,
            @Query("limit") Integer limit
    );

    @GET("api/invoices/{id}")
    Call<InvoiceResponse> getById(@Path("id") String id);

    @POST("api/invoices")
    Call<ApiResponse<InvoiceResponse>> createInvoice(@Body InvoiceRequest request);

    @PUT("api/invoices/{id}")
    Call<ApiResponse<InvoiceResponse>> updateInvoice(@Path("id") String id, @Body InvoiceRequest request);

    @PATCH("api/invoices/{id}/status")
    Call<ApiResponse<InvoiceResponse>> updateStatus(@Path("id") String id, @Body InvoiceRequest request);

    @DELETE("api/invoices/{id}")
    Call<ApiResponse<Void>> deleteInvoice(@Path("id") String id);

    @GET("api/invoices/{id}/pdf")
    Call<ApiResponse<InvoiceResponse>> getPdf(@Path("id") String id);
}
