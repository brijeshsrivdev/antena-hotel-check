package com.antenapro.hotelcheck.sitecheck.check;

import com.antenapro.hotelcheck.sitecheck.html.HtmlSupport;
import com.antenapro.hotelcheck.sitecheck.model.CheckStatus;
import com.antenapro.hotelcheck.sitecheck.model.Finding;
import com.antenapro.hotelcheck.sitecheck.model.SiteSnapshot;
import com.antenapro.hotelcheck.sitecheck.model.Stage;
import org.jsoup.nodes.Document;

import java.util.Optional;

/** Carries the catalogue columns (id, title, stage, why, whether Antena provides it) and the finding builders. */
abstract class AbstractSiteCheck implements SiteCheck {

    private final String id;
    private final String title;
    private final Stage stage;
    private final String why;
    private final boolean antenaHandles;

    protected AbstractSiteCheck(String id, String title, Stage stage, String why, boolean antenaHandles) {
        this.id = id;
        this.title = title;
        this.stage = stage;
        this.why = why;
        this.antenaHandles = antenaHandles;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public String title() {
        return title;
    }

    @Override
    public Stage stage() {
        return stage;
    }

    @Override
    public final Finding run(SiteSnapshot snapshot) {
        Optional<Document> home = snapshot.homeDoc();
        if (home.isEmpty()) {
            return unknown("The home page could not be read.", "Make the home page load, then run the check again.");
        }
        return check(snapshot, home.get());
    }

    /** Runs with a readable home page; the unreadable case is handled once in {@link #run}. */
    protected abstract Finding check(SiteSnapshot snapshot, Document home);

    protected Finding pass(String evidence, String fix) {
        return finding(CheckStatus.PASS, evidence, fix);
    }

    protected Finding partial(String evidence, String fix) {
        return finding(CheckStatus.PARTIAL, evidence, fix);
    }

    protected Finding fail(String evidence, String fix) {
        return finding(CheckStatus.FAIL, evidence, fix);
    }

    protected Finding unknown(String evidence, String fix) {
        return finding(CheckStatus.UNKNOWN, evidence, fix);
    }

    private Finding finding(CheckStatus status, String evidence, String fix) {
        return new Finding(id, title, stage, status, HtmlSupport.clip(evidence), fix, why, antenaHandles);
    }
}
