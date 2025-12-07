package fpoly.haideptrai.duan1.api.services;

import fpoly.haideptrai.duan1.api.models.MoMoCreateRequest;
import fpoly.haideptrai.duan1.api.models.MoMoCreateResponse;
import fpoly.haideptrai.duan1.api.models.VNPayCreateRequest;
import fpoly.haideptrai.duan1.api.models.VNPayCreateResponse;
import fpoly.haideptrai.duan1.api.models.VNPayStatusResponse;
import fpoly.haideptrai.duan1.api.models.ZaloPayCreateRequest;
import fpoly.haideptrai.duan1.api.models.ZaloPayCreateResponse;
import fpoly.haideptrai.duan1.api.models.ZaloPayStatusResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface PaymentService {
    /**
     * Tạo đơn hàng thanh toán ZaloPay
     * POST /api/payment/zalopay/create
     */
    @POST("api/payment/zalopay/create")
    Call<ZaloPayCreateResponse> createZaloPayOrder(@Body ZaloPayCreateRequest request);

    /**
     * Kiểm tra trạng thái thanh toán ZaloPay
     * GET /api/payment/zalopay/status/:orderId
     */
    @GET("api/payment/zalopay/status/{orderId}")
    Call<ZaloPayStatusResponse> getZaloPayStatus(@Path("orderId") String orderId);

    /**
     * Tạo đơn hàng thanh toán MoMo
     * POST /api/payment/momo/create
     */
    @POST("api/payment/momo/create")
    Call<MoMoCreateResponse> createMoMoOrder(@Body MoMoCreateRequest request);

    /**
     * Tạo đơn hàng thanh toán VNPay
     * POST /api/payment/vnpay/create
     */
    @POST("api/payment/vnpay/create")
    Call<VNPayCreateResponse> createVNPayOrder(@Body VNPayCreateRequest request);

    /**
     * Kiểm tra trạng thái thanh toán VNPay
     * GET /api/payment/vnpay/status/:orderId
     */
    @GET("api/payment/vnpay/status/{orderId}")
    Call<VNPayStatusResponse> getVNPayStatus(@Path("orderId") String orderId);
}

