package com.faunadb.client.errors;

import com.faunadb.client.HttpResponses;

/**
 * An exception thrown if a FaunaDB response is unknown or unparseable by the client.
 */
public class UnknownException extends FaunaException {
  public UnknownException(String message) {
    super(message);
  }

  public UnknownException(HttpResponses.QueryErrorResponse response) {
    super(response);
  }
}
