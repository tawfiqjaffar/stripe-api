package com.tawfiqjaffar.listener;

public interface StripeWebhookListener {
    void onStripeError(Exception e);

    void onStripeLog(String message);

    void onStripePaymentStatusUpdated(String id, String type, String receiptEmail);

    void onStripePaymentSucceeded();

}
