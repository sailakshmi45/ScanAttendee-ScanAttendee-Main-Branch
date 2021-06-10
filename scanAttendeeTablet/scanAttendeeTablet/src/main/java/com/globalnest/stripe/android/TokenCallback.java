package com.globalnest.stripe.android;

import com.globalnest.stripe.android.model.Token;

public abstract class TokenCallback {
    public abstract void onError(Exception error);
    public abstract void onSuccess(Token token);
}
