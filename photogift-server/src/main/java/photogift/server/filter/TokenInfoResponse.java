package photogift.server.filter;

/**
 * Rebuilds a token from a JSON response from the OAuth 2.0 token info endpoint.
 */

class TokenInfoResponse {

    String audience;

    String userId;

    String expiresIn;
}