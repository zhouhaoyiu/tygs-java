/*
 * Copyright (c) 2022. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.zhy.market.domain;

import lombok.Data;

/**
 * @author Administrator
 */
@Data
public class WaterMeterInfo {
    public String waterMeterId;
    public Integer wallId;
    public String paymentNumber;
    public String accountNumber;
    public String accountName;
    public String status;

    public String nature;
    public String updateTime;
    public String waterMeterFrom;
}
