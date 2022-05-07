package com.tawfiqjaffar.listener;

import com.stripe.exception.StripeException;

public interface StripeIntentListener<PaymentHistoryContentType> {
    void onStripeIntentCreated(String id, String status, PaymentHistoryContentType content);

    void onStripeIntentError(final StripeException message);
}
