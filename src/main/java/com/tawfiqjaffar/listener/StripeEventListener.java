package com.tawfiqjaffar.listener;

public interface StripeEventListener<PaymentHistoryContentType> {
    void onStripeError(final Exception e);

    void onStripeLog(final String message);


    void onStripeIntentCreated(String id, String status, PaymentHistoryContentType content);

    void onStripePaymentStatusUpdated(String eventId, String eventType, String receiptEmail);

    void onStripePaymentSucceeded();
}
