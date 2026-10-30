package com.brandonkamga.sourcefolio.checkup.application;

/** Whether a site answers a visitor right now. Makes a network call: never inside a transaction. */
public interface SiteReachability {

    boolean isReachable(String url);
}
