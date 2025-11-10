package fpoly.haideptrai.duan1.api.services;

import fpoly.haideptrai.duan1.api.models.InvoiceListResponse;
import retrofit2.Call;
import retrofit2.http.GET;
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
}
