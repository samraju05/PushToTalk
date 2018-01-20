package com.example.user.pushtotalktest.servers;


import com.morlunk.jumble.model.Server;

public class PublicServer extends Server {

    private String mCA;
    private String mContinentCode;
    private String mCountry;
    private String mCountryCode;
    private String mRegion;
    private String mUrl;

    public PublicServer(String name, String ca, String continentCode, String country, String countryCode, String ip, Integer port, String region, String url) {
        super(-1, name, ip, port, "", "");
        mCA = ca;
        mContinentCode = continentCode;
        mCountry = country;
        mCountryCode = countryCode;
        mRegion = region;
        mUrl = url;
    }

    public String getCountry() {
        return mCountry;
    }

    public String getCountryCode() {
        return mCountryCode;
    }

    public String getUrl() {
        return mUrl;
    }
}
