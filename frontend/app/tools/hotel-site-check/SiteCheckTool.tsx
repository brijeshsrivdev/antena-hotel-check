"use client";

import { useEffect, useRef, useState } from "react";
import { FindingItem } from "@/components/FindingItem";
import { ScoreDial } from "@/components/ScoreDial";
import { StageRows } from "@/components/StageRows";
import { ReportForm } from "@/components/ReportForm";
import { SiteCheckError, STATUS_ORDER, startSiteCheck, waitForSiteCheck, type SiteCheckResponse } from "@/lib/siteCheck";

type Phase = { kind: "idle" } | { kind: "starting" } | { kind: "running"; run: SiteCheckResponse | null; since: number } | { kind: "done"; run: SiteCheckResponse } | { kind: "error"; message: string };

export function SiteCheckTool() {
  const [url, setUrl] = useState("");
  const [phase, setPhase] = useState<Phase>({ kind: "idle" });
  const [elapsed, setElapsed] = useState(0);
  const abort = useRef<AbortController | null>(null);

  useEffect(() => {
    if (phase.kind !== "running") return;
    const id = setInterval(() => setElapsed(Math.round((Date.now() - phase.since) / 1000)), 500);
    return () => clearInterval(id);
  }, [phase]);

  async function submit(e: React.FormEvent) {
    e.preventDefault();
    abort.current?.abort();
    abort.current = new AbortController();
    setPhase({ kind: "starting" });
    try {
      const runId = await startSiteCheck(url.trim());
      setPhase({ kind: "running", run: null, since: Date.now() });
      const run = await waitForSiteCheck(runId, (r) => setPhase({ kind: "running", run: r, since: Date.parse(r.startedAt) || Date.now() }), abort.current.signal);
      setPhase(run.status === "FAILED" ? { kind: "error", message: run.failure ?? "The analysis failed." } : { kind: "done", run });
    } catch (err) {
      setPhase({ kind: "error", message: err instanceof SiteCheckError ? err.message : "The backend did not answer. Is it running with the local profile?" });
    }
  }

  const busy = phase.kind === "starting" || phase.kind === "running";
  const run = phase.kind === "done" ? phase.run : null;

  return (
    <>
      <div className="card">
        <form className="check" onSubmit={submit} aria-label="Check a hotel website">
          <input type="text" name="url" inputMode="url" placeholder="hotel website, e.g. lexisinnbeachresort.com" value={url}
                 onChange={(e) => setUrl(e.target.value)} required disabled={busy} aria-label="Hotel website address" />
          <button type="submit" disabled={busy || url.trim() === ""}>{busy ? "Checking…" : "Check the site"}</button>
        </form>
        {phase.kind === "error" && <p className="error" role="alert">{phase.message}</p>}
        {phase.kind === "starting" && <p className="progress">Starting…</p>}
        {phase.kind === "running" && (
          <p className="progress" aria-live="polite">Reading up to five pages, robots.txt, the sitemap and PageSpeed Insights… {elapsed} s (up to 25 s).</p>
        )}
      </div>

      {run && (
        <section aria-label="Result">
          <div className="summary card">
            <ScoreDial score={run.score} />
            <div>
              <p className="muted" style={{ margin: "0 0 10px" }}>
                {run.finalUrl ?? run.requestedUrl} · {run.findings.filter((f) => f.status === "FAIL").length} to fix,{" "}
                {run.findings.filter((f) => f.status === "PARTIAL").length} partly, {run.findings.filter((f) => f.status === "PASS").length} pass
                {run.findings.some((f) => f.status === "UNKNOWN") ? `, ${run.findings.filter((f) => f.status === "UNKNOWN").length} unknown (not scored)` : ""}
              </p>
              <StageRows stages={run.stages} />
            </div>
          </div>

          {run.limitations.length > 0 && (
            <div className="limitations" style={{ marginTop: 16 }}>
              <strong>What we could not check</strong>
              <ul>{run.limitations.map((l, i) => <li key={i}>{l}</li>)}</ul>
            </div>
          )}

          <h2>Findings, what to fix first</h2>
          <ul className="findings">
            {STATUS_ORDER.flatMap((s) => run.findings.filter((f) => f.status === s)).map((f) => <FindingItem key={f.check} finding={f} />)}
          </ul>

          <ReportForm runId={run.runId} />

          <h2>Pages read</h2>
          <table className="fetched">
            <thead><tr><th>Page</th><th>Role</th><th>Status</th><th>Time</th></tr></thead>
            <tbody>
              {run.fetched.map((p, i) => (
                <tr key={i}><td>{p.finalUrl || p.url}</td><td>{p.role}</td><td>{p.error ?? `HTTP ${p.status}`}</td><td>{p.ms} ms</td></tr>
              ))}
            </tbody>
          </table>
          <p className="muted" style={{ marginTop: 16 }}>
            Every finding is what we observed on the pages listed above; nothing here claims a booking succeeded or failed. "Antena does this
            for you" appears only where an Antena storefront provides the item out of the box.
          </p>
        </section>
      )}
    </>
  );
}
