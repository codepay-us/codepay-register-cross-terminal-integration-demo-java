package com.example.ecrhub.controller;

import cn.hutool.core.util.StrUtil;
import com.codepay.register.sdk.ECRHubClient;
import com.codepay.register.sdk.model.request.QueryRequest;
import com.codepay.register.sdk.model.response.QueryResponse;
import com.example.ecrhub.constant.CommonConstant;
import com.example.ecrhub.manager.ECRHubClientManager;
import com.example.ecrhub.manager.PurchaseManager;
import com.example.ecrhub.manager.SceneManager;
import com.example.ecrhub.pojo.ECRHubClientPo;
import com.example.ecrhub.util.JSONFormatUtil;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author: yanzx
 * @date: 2023/10/12 16:14
 * @description:
 */
public class QueryResponseController {

    public Button queryButton;
    //    public TextArea request_info;
    @FXML
    private Button returnButton;
    public TextField trans_amount;
    public TextField merchant_order_no;
    public TextArea response_info;
    private Task<String> task = null;
    public ChoiceBox<String> terminalBox;
    @FXML
    private Label wait_label;
    @FXML
    private ProgressIndicator progress_indicator;

    public void initialize() {

        QueryResponse queryResponse = PurchaseManager.getInstance().getQueryResponse();
//        QueryRequest queryRequest = PurchaseManager.getInstance().getQueryRequest();
        if (queryResponse != null){
            trans_amount.setText(queryResponse.getOrder_amount());
            merchant_order_no.setText(queryResponse.getMerchant_order_no());
//            request_info.setText(JSONFormatUtil.formatJson(queryRequest));
            response_info.setText(JSONFormatUtil.formatJson(queryResponse));
        }
    }


    @FXML
    private void queryReturnButtonAction(ActionEvent event) {
        if (task != null && task.isRunning()) {
            task.cancel();
        }
        PurchaseManager.getInstance().setQueryResponse(null);
//        PurchaseManager.getInstance().setQueryRequest(null);
        SceneManager.getInstance().loadScene("shopping", "/com/example/ecrhub/fxml/shopping.fxml");
        SceneManager.getInstance().switchScene("shopping");
    }

    @FXML
    private void handleQueryButtonAction(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("ERROR!");
        String merchantOrderNo = merchant_order_no.getText();
        if (StrUtil.isEmpty(merchantOrderNo)) {
            alert.setContentText("Please enter trans merchant_order_no");
            alert.showAndWait();
            return;
        }
        task = new Task<String>() {
            @Override
            protected String call() throws Exception {
                progress_indicator.setVisible(true);
                wait_label.setVisible(true);
                queryButton.setDisable(true);
                PurchaseManager.getInstance().setQueryResponse(Query());
                return "success";
            }
        };
        task.setOnSucceeded(success -> {
            progress_indicator.setVisible(false);
            wait_label.setVisible(false);
            queryButton.setDisable(false);
            SceneManager.getInstance().loadScene("queryResponse", "/com/example/ecrhub/fxml/queryResponse.fxml");
            SceneManager.getInstance().switchScene("queryResponse");
        });

        task.setOnFailed(fail -> {
            progress_indicator.setVisible(false);
            wait_label.setVisible(false);
            queryButton.setDisable(false);
            alert.setContentText("connect error!");
            alert.showAndWait();
        });

        task.setOnCancelled(cancel -> {
            progress_indicator.setVisible(false);
            wait_label.setVisible(false);
            queryButton.setDisable(false);
            PurchaseManager.getInstance().setQueryResponse(null);
        });

        Thread thread = new Thread(task);
        thread.start();
    }

    private QueryResponse Query() throws Exception {
        ECRHubClientManager instance = ECRHubClientManager.getInstance();
        // 设备选择
        ECRHubClient client;
        if (1 == instance.getConnectType()) {
            client = instance.getClient();
        } else {
            LinkedHashMap<String, ECRHubClientPo> client_list = instance.getClient_list();
            client = client_list.get(terminalBox.getValue()).getClient();
        }
        String[] origMerchantOrderNumbers = merchant_order_no.getText().split(",");
        System.out.println(Arrays.toString(origMerchantOrderNumbers));

        List<QueryResponse> queryResponses = new ArrayList<>();

        for (String origMerchantOrderNo : origMerchantOrderNumbers) {
            QueryRequest request = new QueryRequest();
            request.setApp_id(CommonConstant.APP_ID);
            request.setMerchant_order_no(origMerchantOrderNo);
            System.out.println("Query request:" + request);
            QueryResponse queryResponse = client.execute(request);
//            PurchaseManager.getInstance().setQueryRequest(request);
            System.out.println("Query response:" + queryResponse);
            queryResponses.add(queryResponse);
//            return queryResponse;
        }
        // 返回最后一个响应
        return queryResponses.isEmpty() ? null : queryResponses.get(queryResponses.size() -1);
    }
}
