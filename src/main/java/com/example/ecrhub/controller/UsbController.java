package com.example.ecrhub.controller;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.codepay.register.sdk.ECRHubClient;
import com.codepay.register.sdk.model.response.ECRHubResponse;
import com.example.ecrhub.manager.ECRHubClientManager;
import com.example.ecrhub.util.JSONFormatUtil;
import com.example.ecrhub.util.SwitchButton;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author: yanzx
 * @date: 2023/10/17 10:16
 * @description:
 */
public class UsbController {

    Logger logger =  LoggerFactory.getLogger(UsbController.class);

    public TextArea connect_info;

    private Task<String> task = null;

    @FXML
    private VBox wait_vbox;

    @FXML
    private HBox switch_hbox;

    private SwitchButton switch_button;

    public void initialize() {
        ECRHubClientManager instance = ECRHubClientManager.getInstance();
        switch_button = new SwitchButton(instance.isConnected()) {
            @Override
            public boolean buttonOffAction() {
                return disconnectButtonAction();
            }

            @Override
            public boolean buttonOnAction() {
                return connectButtonAction();
            }
        };
        switch_hbox.getChildren().add(switch_button);
        if (instance.isConnected()) {
            connect_info.setText(instance.getConnect_info() == null ? "Connected!" : JSONFormatUtil.formatJson(instance.getConnect_info()));
        }
    }

    private boolean connectButtonAction() {
        task = new Task<String>() {
            @Override
            protected String call() throws Exception {
                switch_hbox.setDisable(true);

                wait_vbox.setVisible(true);
                wait_vbox.setManaged(true);
                connect_info.setVisible(false);
                connect_info.setManaged(false);
                try {
                    ECRHubClient client = ECRHubClientManager.getInstance().getClient();
                    client.connect();
                    return JSON.toJSONString(new JSONObject());
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                    throw e;
                }
            }
        };

        task.setOnSucceeded(success -> {
            String response_info = task.getValue();
            ECRHubResponse ecrHubResponse = JSONObject.parseObject(response_info, ECRHubResponse.class);
            ECRHubClientManager.getInstance().setConnect_info(ecrHubResponse);

            wait_vbox.setVisible(false);
            wait_vbox.setManaged(false);
            connect_info.setVisible(true);
            connect_info.setManaged(true);

            switch_hbox.setDisable(false);
            connect_info.setText(JSONFormatUtil.formatJson(ecrHubResponse));
        });

        task.setOnFailed(fail -> {
            wait_vbox.setVisible(false);
            wait_vbox.setManaged(false);
            connect_info.setVisible(true);
            connect_info.setManaged(true);

            switch_hbox.setDisable(false);
            switch_button.setButtonOff();
            connect_info.setText("Unconnected! \n Connect error! Please try again!");
        });

        task.setOnCancelled(cancel -> {
            wait_vbox.setVisible(false);
            wait_vbox.setManaged(false);
            connect_info.setVisible(true);
            connect_info.setManaged(true);
            try {
                ECRHubClient client = ECRHubClientManager.getInstance().getClient();
                client.disconnect();
            } catch (Exception e) {

            }

            switch_hbox.setDisable(false);
            switch_button.setButtonOff();
            connect_info.setText("Unconnected!");
        });

        Thread thread = new Thread(task);
        thread.start();
        return true;
    }

    @FXML
    private void cancelAction(ActionEvent event) {
        if (task != null && task.isRunning()) {
            task.cancel();
        }
    }

    private boolean disconnectButtonAction() {
        try {
            ECRHubClientManager instance = ECRHubClientManager.getInstance();
            ECRHubClient client = instance.getClient();
            client.disconnect();
            instance.setConnect_info(null);
        } catch (Exception e) {

        } finally {
            connect_info.setText("Unconnected!");
        }
        return true;
    }
}
