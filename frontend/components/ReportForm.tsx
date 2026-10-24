"use client";

import { useState } from "react";
import { emailReport, reportPdfUrl, SiteCheckError } from "@/lib/siteCheck";

/**
 * "Email me the PDF" (plan §4): the label states what will be sent and that Antena may follow up by email; the
 * request carries consent only when the box is ticked. The direct download exists for the developer's machine.
 */
export function ReportForm({ runId }: { runId: string }) {
  const [email, setEmail] = useState("");
  const [consent, setConsent] = useState(false);
  const [state, setState] = useState<{ kind: "idle" } | { kind: "sending" } | { kind: "sent"; note: string } | { kind: "error"; message: string }>({ kind: "idle" });

  async function submit(e: React.FormEvent) {
    e.preventDefault();
    setState({ kind: "sending" });
    try {
      setState({ kind: "sent", note: await emailReport(runId, email.trim(), consent) });
    } catch (err) {
      setState({ kind: "error", message: err instanceof SiteCheckError ? err.message : "The backend did not answer." });
    }
  }

  return (
    <div className="card" style={{ marginTop: 24 }}>
      <h2 style={{ marginTop: 0 }}>Keep this report</h2>
      {state.kind === "sent" ? (
        <p role="status">{state.note}</p>
      ) : (
        <form className="check" onSubmit={submit} aria-label="Email me the PDF" style={{ alignItems: "center" }}>
          <input type="email" name="email" placeholder="your@email.com" value={email} onChange={(e) => setEmail(e.target.value)} required
                 disabled={state.kind === "sending"} aria-label="Email address for the PDF report" />
          <button type="submit" disabled={state.kind === "sending" || !consent || email.trim() === ""}>
            {state.kind === "sending" ? "Sending…" : "Email me the PDF"}
          </button>
          <label style={{ flexBasis: "100%", fontSize: ".9rem", color: "var(--muted)", display: "flex", gap: 8, alignItems: "flex-start" }}>
            <input type="checkbox" checked={consent} onChange={(e) => setConsent(e.target.checked)} style={{ marginTop: 3 }} />
            <span>Send this report as a PDF to the address above. I agree that Antena may follow up by email about building a website and booking engine for my hotel.</span>
          </label>
        </form>
      )}
      {state.kind === "error" && <p className="error" role="alert">{state.message}</p>}
      <p className="muted" style={{ margin: "10px 0 0", fontSize: ".9rem" }}>
        Or <a href={reportPdfUrl(runId)}>download the PDF</a> now.
      </p>
    </div>
  );
}
