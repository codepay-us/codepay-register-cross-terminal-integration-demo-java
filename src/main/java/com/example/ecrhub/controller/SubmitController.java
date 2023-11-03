package com.example.ecrhub.controller;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.example.ecrhub.constant.CommonConstant;
import com.example.ecrhub.manager.ECRHubClientManager;
import com.example.ecrhub.manager.PurchaseManager;
import com.example.ecrhub.manager.SceneManager;
import com.example.ecrhub.pojo.ECRHubClientPo;
import com.wiseasy.ecr.hub.sdk.ECRHubClient;
import com.wiseasy.ecr.hub.sdk.model.request.PurchaseRequest;
import com.wiseasy.ecr.hub.sdk.model.response.PurchaseResponse;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.Date;
import java.util.LinkedHashMap;

/**
 * @author: yanzx
 * @date: 2023/10/11 12:38
 * @description:
 */
public class SubmitController {

    @FXML
    private Button submitButton;

    @FXML
    private ProgressIndicator progress_indicator;

    @FXML
    private Label wait_label;

    private Task<String> task = null;

    @FXML
    private Label trans_amount;

    @FXML
    private Label terminal_sn;

    public ChoiceBox<String> terminalBox;

    public void initialize() {
        ECRHubClientManager instance = ECRHubClientManager.getInstance();
        if (1 == instance.getConnectType()) {
            // 串口连接初始化页面
            if (instance.getConnect_info().getDevice_data() != null) {
                terminal_sn.setText(instance.getConnect_info().getDevice_data().getDevice_sn());
            } else {
                terminal_sn.setText("Unknown");
            }
            terminalBox.setVisible(false);
            terminalBox.setManaged(false);
        } else {
            // WLAN 连接初始化页面
            LinkedHashMap<String, ECRHubClientPo> client_list = instance.getClient_list();
            for (String key: client_list.keySet()) {
                ECRHubClientPo client_info = client_list.get(key);
                if (client_info.isIs_connected()) {
                    terminalBox.getItems().add(key);
                }
            }
            terminalBox.setValue(terminalBox.getItems().get(0));
            terminal_sn.setVisible(false);
            terminal_sn.setManaged(false);
        }
        progress_indicator.setDisable(true);
        progress_indicator.setVisible(false);
        wait_label.setVisible(false);

        if (PurchaseManager.getInstance().getTrans_amount() == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("ERROR!");
            alert.setContentText("Amount error!");
            alert.showAndWait();
        } else {
            trans_amount.setText(PurchaseManager.getInstance().getTrans_amount().getText());
        }
    }

    @FXML
    private void handleReturnButtonAction(ActionEvent event) {
        if (task != null && task.isRunning()) {
            task.cancel();
        }
        SceneManager.getInstance().switchScene("shopping");
    }

    @FXML
    private void handleSubmitButtonAction(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("ERROR!");

        String amount_str = trans_amount.getText();
        if (StrUtil.isEmpty(amount_str)) {
            alert.setContentText("Please enter trans amount");
            alert.showAndWait();
            return;
        }

        task = new Task<String>() {
            @Override
            protected String call() throws Exception {
                progress_indicator.setVisible(true);
                wait_label.setVisible(true);
                submitButton.setDisable(true);

                PurchaseManager.getInstance().setResponse(requestToECR(amount_str.replace("$", "")));
                return "success";
            }
        };

        task.setOnSucceeded(success -> {
            SceneManager.getInstance().loadScene("response", "/com/example/ecrhub/fxml/response.fxml");
            SceneManager.getInstance().switchScene("response");
        });

        task.setOnFailed(fail -> {
            progress_indicator.setVisible(false);
            wait_label.setVisible(false);
            submitButton.setDisable(false);

            alert.setContentText("Connect to ECH Hub error!");
            alert.showAndWait();
        });

        task.setOnCancelled(cancel -> {
            progress_indicator.setVisible(false);
            wait_label.setVisible(false);
            trans_amount.setDisable(false);
            submitButton.setDisable(false);
        });

        Thread thread = new Thread(task);
        thread.start();
    }


    private PurchaseResponse requestToECR(String amount_str) throws Exception {
        ECRHubClientManager instance = ECRHubClientManager.getInstance();
        // 设备选择
        ECRHubClient client;
        if (1 == instance.getConnectType()) {
            client = instance.getClient();
        } else {
            LinkedHashMap<String, ECRHubClientPo> client_list = instance.getClient_list();
            client = client_list.get(terminalBox.getValue()).getClient();
        }

        // Purchase
        PurchaseRequest request = new PurchaseRequest();
        request.setApp_id(CommonConstant.APP_ID);
        request.setMerchant_order_no("DEMO" + new Date().getTime() + RandomUtil.randomNumbers(4));
        request.setOrder_amount(amount_str);
        request.setPay_method_category("BANKCARD");

        // Execute purchase request
        PurchaseResponse response = client.execute(request);
        System.out.println("Purchase Response:" + response);
        return response;
    }

}
