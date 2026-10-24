package com.antenapro.hotelcheck.sitecheck.psi;

import com.antenapro.hotelcheck.sitecheck.model.PsiResult;

import java.net.URI;

/** Google PageSpeed Insights, mobile strategy. Never throws: a failure is an unavailable result with a reason. */
public interface PsiClient {

    PsiResult analyze(URI url);
}
