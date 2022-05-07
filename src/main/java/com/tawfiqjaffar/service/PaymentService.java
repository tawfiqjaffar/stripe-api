package com.tawfiqjaffar.service;

import com.google.gson.JsonSyntaxException;
import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.PaymentIntent;
import com.stripe.model.StripeObject;
import com.stripe.net.Webhook;
import com.stripe.param.PaymentIntentCreateParams;
import com.tawfiqjaffar.domain.CreatePayment;
import com.tawfiqjaffar.listener.StripeEventListener;

public class PaymentService<CreatePaymentResponseType, PaymentHistoryContentType> {

    private final String webhookKey;

    private final StripeEventListener<PaymentHistoryContentType> stripeEventListener;

    public PaymentService(final String apiKey, final String webhookKey, final StripeEventListener<PaymentHistoryContentType> stripeEventListener) {
        this.webhookKey = webhookKey;
        this.stripeEventListener = stripeEventListener;
        Stripe.apiKey = apiKey;

    }

    public void createPaymentIntent(final CreatePayment<PaymentHistoryContentType> createPayment, final Long amount, final String currency) {
        PaymentIntentCreateParams createParams = new PaymentIntentCreateParams.Builder().setCurrency(currency).setAmount(amount).build();
        PaymentIntent intent;
        try {
            intent = PaymentIntent.create(createParams);
            this.stripeEventListener.onStripeIntentCreated(intent.getId(), intent.getStatus(), createPayment.getContent());
        } catch (StripeException e) {
            this.stripeEventListener.onStripeError(e);
        }
    }

    public void webhook(final String payload, final String sigHeader) {
        Event event;

        try {
            event = Webhook.constructEvent(payload, sigHeader, this.webhookKey);
        } catch (JsonSyntaxException | SignatureVerificationException e) {
            this.stripeEventListener.onStripeError(e);
            return;
        }
        PaymentIntent intent = (PaymentIntent) event.getDataObjectDeserializer().getObject().get();
        EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
        StripeObject stripeObject;
        if (dataObjectDeserializer.getObject().isPresent()) {
            stripeObject = dataObjectDeserializer.getObject().get();
            this.stripeEventListener.onStripeLog(stripeObject.toString());
        } else {
            this.stripeEventListener.onStripeError(new Exception("Stripe event deserialization failed"));
            return;
        }
        this.stripeEventListener.onStripePaymentStatusUpdated(intent.getId(), event.getType(), intent.getReceiptEmail());

        if (event.getType().equals("payment_intent.succeeded")) {
            this.stripeEventListener.onStripePaymentSucceeded();
        }
    }
}
