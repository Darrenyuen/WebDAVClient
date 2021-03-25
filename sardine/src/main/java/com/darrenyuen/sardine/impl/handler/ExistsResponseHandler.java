package com.darrenyuen.sardine.impl.handler;


import com.darrenyuen.sardine.impl.SardineException;

import okhttp3.Response;

/**
 * Created by guillaume on 20/11/2017.
 */

public class ExistsResponseHandler extends ValidatingResponseHandler<Boolean>
{
    @Override
    public Boolean handleResponse(Response response) throws SardineException {
        if (!response.isSuccessful() && response.code() == 404) {
            return false;
        }
        validateResponse(response);
        return true;
    }
}
