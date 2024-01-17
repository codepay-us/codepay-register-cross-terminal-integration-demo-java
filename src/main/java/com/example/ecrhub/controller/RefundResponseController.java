package com.example.ecrhub.controller;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.codepay.register.sdk.ECRHubClient;
import com.codepay.register.sdk.model.request.RefundRequest;
import com.codepay.register.sdk.model.request.VoidRequest;
import com.codepay.register.sdk.model.response.RefundResponse;
import com.codepay.register.sdk.model.response.VoidResponse;
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

import java.util.*;

/**
 * @author: yanzx
 * @date: 2023/10/12 16:14
 * @description:
 */
public class RefundResponseController {

    public Button refundButton;
    public Button voidButton;
    public TextField orig_merchant_order_no;
    @FXML
    private Label terminal_sn;
    @FXML
    private Button returnButton;
    public TextField trans_amount;

    public TextField merchant_order_no;
    public TextArea response_info;
    @FXML
    private Label wait_label;
    @FXML
    private ProgressIndicator progress_indicator;


    private Task<String> task = null;
    public ChoiceBox<String> terminalBox;

    public void initialize() {
        ECRHubClientManager instance = ECRHubClientManager.getInstance();
        if (1 == instance.getConnectType()) {
            // 串口连接初始化页面
            terminalBox.setVisible(false);
            terminalBox.setManaged(false);
        } else {
            // WLAN 连接初始化页面
            LinkedHashMap<String, ECRHubClientPo> client_list = instance.getClient_list();
            for (String key : client_list.keySet()) {
                ECRHubClientPo client_info = client_list.get(key);
                if (client_info.isIs_connected()) {
                    terminalBox.getItems().add(key);
                }
            }
            terminalBox.setValue(terminalBox.getItems().get(0));
            terminal_sn.setVisible(false);
            terminal_sn.setManaged(false);
        }
        RefundResponse refundResponse = PurchaseManager.getInstance().getRefundResponse();
        VoidResponse voidResponse = PurchaseManager.getInstance().getVoidResponse();
        orig_merchant_order_no.setText(null);
        merchant_order_no.setText(null);
        response_info.setText(null);
        trans_amount.setText(null);
        if (refundResponse != null) {
            trans_amount.setText(refundResponse.getOrder_amount());
            orig_merchant_order_no.setText(refundResponse.getMerchant_order_no());
            response_info.setText(JSONFormatUtil.formatJson(refundResponse));
        } else if (voidResponse != null) {
            trans_amount.setText(voidResponse.getOrder_amount());
            orig_merchant_order_no.setText(voidResponse.getMerchant_order_no());
            response_info.setText(JSONFormatUtil.formatJson(voidResponse));
        }
    }

    @FXML
    private void refundReturnButtonAction(ActionEvent event) {
        if (task != null && task.isRunning()) {
            task.cancel();
        }
        PurchaseManager.getInstance().setRefundResponse(null);
        PurchaseManager.getInstance().setVoidResponse(null);
        SceneManager.getInstance().loadScene("shopping", "/com/example/ecrhub/fxml/shopping.fxml");
        SceneManager.getInstance().switchScene("shopping");
    }

    @FXML
    private void handleRefundButtonAction(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("ERROR!");
        String origMerchantOrderNo = orig_merchant_order_no.getText();
        if (StrUtil.isEmpty(origMerchantOrderNo)) {
            alert.setContentText("Please enter orig_merchant_order_no");
            alert.showAndWait();
            return;
        }

        task = new Task<String>() {
            @Override
            protected String call() throws Exception {
                progress_indicator.setVisible(true);
                wait_label.setVisible(true);
                refundButton.setDisable(true);
                voidButton.setDisable(true);
                PurchaseManager.getInstance().setRefundResponse(refund());
                return "success";
            }
        };

        task.setOnSucceeded(success -> {
            progress_indicator.setVisible(false);
            wait_label.setVisible(false);
            refundButton.setDisable(false);
            voidButton.setDisable(false);
            SceneManager.getInstance().loadScene("refundResponse", "/com/example/ecrhub/fxml/refundResponse.fxml");
            SceneManager.getInstance().switchScene("refundResponse");
        });

        task.setOnFailed(fail -> {
            progress_indicator.setVisible(false);
            wait_label.setVisible(false);
            refundButton.setDisable(false);
            voidButton.setDisable(false);
            alert.setContentText("connect error!");
            alert.showAndWait();
        });

        task.setOnCancelled(cancel -> {
            progress_indicator.setVisible(false);
            wait_label.setVisible(false);
            refundButton.setDisable(false);
            voidButton.setDisable(false);
            PurchaseManager.getInstance().setRefundResponse(null);
            PurchaseManager.getInstance().setVoidResponse(null);
        });

        Thread thread = new Thread(task);
        thread.start();
    }

    private RefundResponse refund() throws Exception {
        ECRHubClientManager instance = ECRHubClientManager.getInstance();
        ECRHubClient client;

        if (1 == instance.getConnectType()) {
            client = instance.getClient();
        } else {
            LinkedHashMap<String, ECRHubClientPo> clientList = instance.getClient_list();
            client = clientList.get(terminalBox.getValue()).getClient();
        }

        String[] origMerchantOrderNumbers = orig_merchant_order_no.getText().split(",");
        System.out.println(Arrays.toString(origMerchantOrderNumbers));

        List<RefundResponse> refundResponses = new ArrayList<>();

        for (String origMerchantOrderNo : origMerchantOrderNumbers) {
            RefundRequest request = createRefundRequest(origMerchantOrderNo);
            System.out.println("Refund request:" + request);
            RefundResponse refundResponse = client.execute(request);
            System.out.println("Refund response:" + refundResponse);
            refundResponses.add(refundResponse);
        }
        // 返回最后一个响应
        return refundResponses.isEmpty() ? null : refundResponses.get(refundResponses.size() - 1);
    }

    private RefundRequest createRefundRequest(String origMerchantOrderNo) {
        String MerchantOrderNo = "C" + origMerchantOrderNo;
        if (merchant_order_no.getText() != null) {
            MerchantOrderNo = merchant_order_no.getText();
        }
        RefundRequest request = new RefundRequest();
        request.setApp_id(CommonConstant.APP_ID);
        if (trans_amount.getText() != null) {
            String amt = trans_amount.getText();
            request.setOrder_amount(amt);
        }
        request.setOrig_merchant_order_no(origMerchantOrderNo);
        request.setMerchant_order_no(MerchantOrderNo);
        return request;
    }

    public void handleVoidButtonAction(ActionEvent actionEvent) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("ERROR!");
        String origMerchantOrderNo = orig_merchant_order_no.getText();
        if (StrUtil.isEmpty(origMerchantOrderNo)) {
            alert.setContentText("Please enter orig_merchant_order_no");
            alert.showAndWait();
            return;
        }

        task = new Task<String>() {
            @Override
            protected String call() throws Exception {
                progress_indicator.setVisible(true);
                wait_label.setVisible(true);
                refundButton.setDisable(true);
                voidButton.setDisable(true);
                PurchaseManager.getInstance().setVoidResponse(Cancel());
                return "success";
            }
        };

        task.setOnSucceeded(success -> {
            progress_indicator.setVisible(false);
            wait_label.setVisible(false);
            refundButton.setDisable(false);
            voidButton.setDisable(false);
            SceneManager.getInstance().loadScene("voidResponse", "/com/example/ecrhub/fxml/refundResponse.fxml");
            SceneManager.getInstance().switchScene("voidResponse");
        });

        task.setOnFailed(fail -> {
            progress_indicator.setVisible(false);
            wait_label.setVisible(false);
            refundButton.setDisable(false);
            voidButton.setDisable(false);
            alert.setContentText("connect error!");
            alert.showAndWait();
        });

        task.setOnCancelled(cancel -> {
            progress_indicator.setVisible(false);
            wait_label.setVisible(false);
            refundButton.setDisable(false);
            voidButton.setDisable(false);
            PurchaseManager.getInstance().setVoidResponse(null);
            PurchaseManager.getInstance().setRefundResponse(null);
        });

        Thread thread = new Thread(task);
        thread.start();
    }

    private VoidResponse Cancel() throws Exception {
        ECRHubClientManager instance = ECRHubClientManager.getInstance();
        ECRHubClient client;

        if (1 == instance.getConnectType()) {
            client = instance.getClient();
        } else {
            LinkedHashMap<String, ECRHubClientPo> clientList = instance.getClient_list();
            client = clientList.get(terminalBox.getValue()).getClient();
        }
        String[] origMerchantOrderNumbers = orig_merchant_order_no.getText().split(",");
        System.out.println(Arrays.toString(origMerchantOrderNumbers));

        List<VoidResponse> voidResponses = new ArrayList<>();

        for (String origMerchantOrderNo : origMerchantOrderNumbers) {
            VoidRequest request = createVoidRequest(origMerchantOrderNo);
            System.out.println("Void request:" + request);
            VoidResponse voidResponse = client.execute(request);
            System.out.println("Void response:" + voidResponse);
            voidResponses.add(voidResponse);
        }
        return voidResponses.isEmpty() ? null : voidResponses.get(voidResponses.size() - 1);

    }

    private VoidRequest createVoidRequest(String origMerchantOrderNo) {
        String MerchantOrderNo = "C" + origMerchantOrderNo;
        if (merchant_order_no.getText() != null) {
            MerchantOrderNo = merchant_order_no.getText();
        }
        VoidRequest request = new VoidRequest();
        request.setApp_id(CommonConstant.APP_ID);
        request.setOrig_merchant_order_no(origMerchantOrderNo);
        request.setMerchant_order_no(MerchantOrderNo);
        return request;
    }
}