package com.example.ecrhub.manager;


import com.codepay.register.sdk.model.request.QueryRequest;
import com.codepay.register.sdk.model.response.CloseResponse;
import com.codepay.register.sdk.model.response.PurchaseResponse;
import com.codepay.register.sdk.model.response.QueryResponse;
import com.codepay.register.sdk.model.response.RefundResponse;
import javafx.scene.control.Label;

/**
 * @author: yanzx
 * @date: 2023/10/12 15:51
 * @description:
 */
public class PurchaseManager {

    private PurchaseManager() {
    }

    private static PurchaseManager instance;

    private PurchaseResponse response;

    private QueryResponse queryResponse;

    private QueryRequest queryRequest;

    private RefundResponse refundResponse;

    private CloseResponse closeResponse;

    private Label trans_amount;

    private Label merchant_order_no;

    public static synchronized PurchaseManager getInstance() {
        if (instance == null) {
            instance = new PurchaseManager();
        }
        return instance;
    }

    public PurchaseResponse getResponse() {
        return response;
    }

    public void setResponse(PurchaseResponse response) {
        this.response = response;
    }

    public RefundResponse getRefundResponse() {
        return refundResponse;
    }

    public void setRefundResponse(RefundResponse refundResponse) {
        this.refundResponse = refundResponse;
    }

    public QueryResponse getQueryResponse() {
        return queryResponse;
    }

    public void setQueryResponse(QueryResponse queryResponse) {
        this.queryResponse = queryResponse;
    }

    public QueryRequest getQueryRequest() {
        return queryRequest;
    }

    public void setQueryRequest(QueryRequest queryRequest) {
        this.queryRequest = queryRequest;
    }

    public CloseResponse getCloseResponse() {
        return closeResponse;
    }

    public void setCloseResponse(CloseResponse closeResponse) {
        this.closeResponse = closeResponse;
    }

    public Label getTrans_amount() {
        return trans_amount;
    }

    public void setTrans_amount(Label trans_amount) {
        this.trans_amount = trans_amount;
    }

    public Label getMerchant_order_no() {
        return merchant_order_no;
    }

    public void setMerchant_order_no(Label merchant_order_no) {
        this.merchant_order_no = merchant_order_no;
    }
}
