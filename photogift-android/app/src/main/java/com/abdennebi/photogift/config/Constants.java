package com.abdennebi.photogift.config;

import com.google.android.gms.common.Scopes;

public class Constants {


    public static class OAuth2 {

        public static final String SERVER_URL = "https://photo-gift.appspot.com";
//        public static final String SERVER_URL = "https://photo-gift.ngrok.com";

        public static final String OAUTH_SERVER_CLIENT_ID = "13169288993-5f08pj034ji42fndcrvd77bivh2b1j60.apps.googleusercontent.com";

        public static final String[] OAUTH_ACTIONS = {"http://schemas.google.com/AddActivity",
                "http://schemas.google.com/ReviewActivity"};

        public static final String[] OAUTH_SCOPES = {Scopes.PLUS_LOGIN, "email"};
    }

    public class Headers {

        public static final String COOKIE_PREFIX = "JSESSIONID=";

        public static final String HEADER_USER_AGENT = "User-Agent";

        public static final String USER_AGENT = "PhotoGift+Client-Android";

        public static final String HEADER_AUTH = "Authorization";

        public static final String HEADER_BEARER = "Bearer ";


        /**
         * Header from the client that carries the authorization code.
         */
        public static final String HEADER_XOAUTH = "X-OAuth-Code";

        /**
         * Header received from server to ask the client to authenticate;
         *
         * @See http://tools.ietf.org/html/rfc2617#section-3.2.1
         */
        public static final String HEADER_WWWAUTH = "WWW-Authenticate";

        /**
         * Cookie Header from the server.
         */
        public static final String HEADER_SETCOOKIE = "Set-Cookie";

        public static final String HEADER_COOKIE = "Cookie";


    }


}
